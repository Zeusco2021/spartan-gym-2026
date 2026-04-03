/**
 * Unit tests for SyncManager and NetworkMonitor
 * Validates: Requirement 9.4
 */

import { OfflineStorage, type IKeyValueStore } from './storage';
import { OfflineDataManager } from './offlineDataManager';
import {
  SyncManager,
  type IApiClient,
  type SyncResponse,
} from './syncManager';
import {
  NetworkMonitor,
  type INetworkStateProvider,
} from './networkMonitor';
import type { PendingOperation } from '../types/offline';
import type { WorkoutSession } from '../types/training';

// ─── Helpers ────────────────────────────────────────────────────

function createMockStore(): IKeyValueStore {
  const data = new Map<string, string>();
  return {
    getItem: async (key) => data.get(key) ?? null,
    setItem: async (key, value) => { data.set(key, value); },
    removeItem: async (key) => { data.delete(key); },
    getAllKeys: async () => Array.from(data.keys()),
    multiGet: async (keys) =>
      keys.map((k) => [k, data.get(k) ?? null] as [string, string | null]),
    multiRemove: async (keys) => keys.forEach((k) => data.delete(k)),
  };
}

function makeSession(overrides?: Partial<WorkoutSession>): WorkoutSession {
  return {
    id: 'session-1',
    userId: 'user-1',
    startedAt: new Date().toISOString(),
    sets: [],
    status: 'active',
    syncStatus: 'synced',
    ...overrides,
  };
}

function createSetup(apiResponses?: Map<string, SyncResponse>) {
  const store = createMockStore();
  const storage = new OfflineStorage(store);
  const manager = new OfflineDataManager(storage);

  const sentOperations: PendingOperation[] = [];
  const apiClient: IApiClient = {
    sendOperation: async (op) => {
      sentOperations.push(op);
      if (apiResponses?.has(op.id)) {
        return apiResponses.get(op.id)!;
      }
      return { success: true };
    },
  };

  const syncManager = new SyncManager(manager, apiClient);
  return { manager, syncManager, apiClient, sentOperations };
}

// ─── SyncManager Tests ──────────────────────────────────────────

describe('SyncManager', () => {
  describe('syncAll — chronological order (Req 9.4)', () => {
    it('should process pending operations in chronological order', async () => {
      const { manager, syncManager, sentOperations } = createSetup();

      // Record sessions with distinct timestamps
      const s1 = makeSession({ id: 's1' });
      const s2 = makeSession({ id: 's2' });
      const s3 = makeSession({ id: 's3' });

      await manager.recordOfflineWorkout(s1);
      await manager.recordOfflineWorkout(s2);
      await manager.recordOfflineWorkout(s3);

      const result = await syncManager.syncAll();

      expect(result.totalProcessed).toBe(3);
      expect(result.succeeded).toBe(3);
      expect(result.failed).toBe(0);

      // Verify chronological order
      for (let i = 1; i < sentOperations.length; i++) {
        expect(sentOperations[i].timestamp >= sentOperations[i - 1].timestamp).toBe(true);
      }
    });

    it('should remove successfully synced operations from queue', async () => {
      const { manager, syncManager } = createSetup();

      await manager.recordOfflineWorkout(makeSession({ id: 's1' }));
      await manager.recordOfflineWorkout(makeSession({ id: 's2' }));

      await syncManager.syncAll();

      const remaining = await manager.getPendingOperations();
      expect(remaining).toHaveLength(0);
    });

    it('should call recordSync when all operations succeed', async () => {
      const { manager, syncManager } = createSetup();

      await manager.recordOfflineStart();
      await manager.recordOfflineWorkout(makeSession({ id: 's1' }));

      await syncManager.syncAll();

      const metadata = await manager.getMetadata();
      expect(metadata.offlineSince).toBeUndefined();
    });
  });

  describe('last-write-wins conflict resolution (Req 9.4)', () => {
    it('should keep local data when local timestamp is newer', async () => {
      const responses = new Map<string, SyncResponse>();
      const { manager, syncManager } = createSetup(responses);

      await manager.recordOfflineWorkout(makeSession({ id: 's1' }));
      const ops = await manager.getPendingOperations();

      // Server has older data
      const olderDate = new Date(Date.now() - 60000).toISOString();
      responses.set(ops[0].id, {
        success: false,
        serverUpdatedAt: olderDate,
      });

      const result = await syncManager.syncOne(ops[0].id);

      expect(result.status).toBe('conflict_local_wins');
      const remaining = await manager.getPendingOperations();
      expect(remaining).toHaveLength(0);
    });

    it('should discard local data when server timestamp is newer', async () => {
      const responses = new Map<string, SyncResponse>();
      const { manager, syncManager } = createSetup(responses);

      await manager.recordOfflineWorkout(makeSession({ id: 's1' }));
      const ops = await manager.getPendingOperations();

      // Server has newer data
      const newerDate = new Date(Date.now() + 60000).toISOString();
      responses.set(ops[0].id, {
        success: false,
        serverUpdatedAt: newerDate,
      });

      const result = await syncManager.syncOne(ops[0].id);

      expect(result.status).toBe('conflict_server_wins');
      const remaining = await manager.getPendingOperations();
      expect(remaining).toHaveLength(0);
    });
  });

  describe('retry logic on failure', () => {
    it('should increment retryCount when sync fails', async () => {
      const responses = new Map<string, SyncResponse>();
      const { manager, syncManager } = createSetup(responses);

      await manager.recordOfflineWorkout(makeSession({ id: 's1' }));
      const ops = await manager.getPendingOperations();

      responses.set(ops[0].id, { success: false, error: 'Server error' });

      const result = await syncManager.syncOne(ops[0].id);

      expect(result.status).toBe('failed');
      expect(result.error).toBe('Server error');

      const remaining = await manager.getPendingOperations();
      expect(remaining).toHaveLength(1);
      expect(remaining[0].retryCount).toBe(1);
    });

    it('should increment retryCount on network exception', async () => {
      const store = createMockStore();
      const storage = new OfflineStorage(store);
      const mgr = new OfflineDataManager(storage);

      const failingClient: IApiClient = {
        sendOperation: async () => { throw new Error('Network timeout'); },
      };

      const sync = new SyncManager(mgr, failingClient);

      await mgr.recordOfflineWorkout(makeSession({ id: 's1' }));
      const ops = await mgr.getPendingOperations();

      const result = await sync.syncOne(ops[0].id);

      expect(result.status).toBe('failed');
      expect(result.error).toBe('Network timeout');

      const remaining = await mgr.getPendingOperations();
      expect(remaining[0].retryCount).toBe(1);
    });
  });

  describe('syncOne', () => {
    it('should return failed for non-existent operation', async () => {
      const { syncManager } = createSetup();
      const result = await syncManager.syncOne('non-existent');
      expect(result.status).toBe('failed');
      expect(result.error).toBe('Operation not found');
    });
  });

  describe('syncAll with partial failures', () => {
    it('should not call recordSync when some operations fail', async () => {
      const responses = new Map<string, SyncResponse>();
      const { manager, syncManager } = createSetup(responses);

      await manager.recordOfflineStart();
      await manager.recordOfflineWorkout(makeSession({ id: 's1' }));
      await manager.recordOfflineWorkout(makeSession({ id: 's2' }));

      const ops = await manager.getPendingOperations();
      // First succeeds, second fails
      responses.set(ops[1].id, { success: false, error: 'Server error' });

      const result = await syncManager.syncAll();

      expect(result.succeeded).toBe(1);
      expect(result.failed).toBe(1);

      const metadata = await manager.getMetadata();
      // offlineSince should still be set since not all ops succeeded
      expect(metadata.offlineSince).toBeDefined();
    });
  });
});

// ─── NetworkMonitor Tests ───────────────────────────────────────

describe('NetworkMonitor', () => {
  function createNetworkSetup() {
    const store = createMockStore();
    const storage = new OfflineStorage(store);
    const offlineManager = new OfflineDataManager(storage);

    const apiClient: IApiClient = {
      sendOperation: async () => ({ success: true }),
    };
    const syncManager = new SyncManager(offlineManager, apiClient);

    let listener: ((isOnline: boolean) => void) | null = null;
    let currentOnline = true;

    const networkProvider: INetworkStateProvider = {
      isOnline: () => currentOnline,
      addListener: (cb) => {
        listener = cb;
        return () => { listener = null; };
      },
    };

    const monitor = new NetworkMonitor(
      networkProvider,
      syncManager,
      offlineManager,
    );

    const simulateNetworkChange = (online: boolean) => {
      currentOnline = online;
      if (listener) listener(online);
    };

    return { monitor, offlineManager, simulateNetworkChange, networkProvider };
  }

  it('should trigger sync when transitioning from offline to online', async () => {
    const { monitor, offlineManager, simulateNetworkChange } = createNetworkSetup();

    await offlineManager.recordOfflineWorkout(makeSession({ id: 's1' }));

    let syncResult: any = null;
    monitor.onSync((result) => { syncResult = result; });
    monitor.start();

    // Go offline then back online
    simulateNetworkChange(false);
    // Allow async recordOfflineStart to complete
    await new Promise<void>((r) => setTimeout(r, 10));
    simulateNetworkChange(true);
    await new Promise<void>((r) => setTimeout(r, 10));

    expect(syncResult).not.toBeNull();
    expect(syncResult.succeeded).toBe(1);

    monitor.stop();
  });

  it('should record offline start when going offline', async () => {
    const { monitor, offlineManager, simulateNetworkChange } = createNetworkSetup();

    monitor.start();
    simulateNetworkChange(false);
    await new Promise<void>((r) => setTimeout(r, 10));

    const metadata = await offlineManager.getMetadata();
    expect(metadata.offlineSince).toBeDefined();

    monitor.stop();
  });

  it('should not trigger sync when already online', async () => {
    const { monitor, simulateNetworkChange } = createNetworkSetup();

    let syncCalled = false;
    monitor.onSync(() => { syncCalled = true; });
    monitor.start();

    // Already online, staying online
    simulateNetworkChange(true);
    await new Promise<void>((r) => setTimeout(r, 10));

    expect(syncCalled).toBe(false);

    monitor.stop();
  });

  it('should stop listening after stop() is called', async () => {
    const { monitor, offlineManager, simulateNetworkChange } = createNetworkSetup();

    await offlineManager.recordOfflineWorkout(makeSession({ id: 's1' }));

    let syncCalled = false;
    monitor.onSync(() => { syncCalled = true; });
    monitor.start();
    monitor.stop();

    simulateNetworkChange(false);
    await new Promise<void>((r) => setTimeout(r, 10));
    simulateNetworkChange(true);
    await new Promise<void>((r) => setTimeout(r, 10));

    expect(syncCalled).toBe(false);
  });
});
