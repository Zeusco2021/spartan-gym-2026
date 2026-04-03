/**
 * Unit tests for OfflineDataManager
 * Validates: Requirements 9.1, 9.2, 9.3
 */

import { OfflineStorage, type IKeyValueStore } from './storage';
import { OfflineDataManager } from './offlineDataManager';
import type { TrainingPlan, WorkoutSession, WorkoutSet, ScheduledRoutine } from '../types/training';

function createMockStore(): IKeyValueStore {
  const data = new Map<string, string>();
  return {
    getItem: async (key) => data.get(key) ?? null,
    setItem: async (key, value) => { data.set(key, value); },
    removeItem: async (key) => { data.delete(key); },
    getAllKeys: async () => Array.from(data.keys()),
    multiGet: async (keys) => keys.map((k) => [k, data.get(k) ?? null] as [string, string | null]),
    multiRemove: async (keys) => keys.forEach((k) => data.delete(k)),
  };
}

function makePlan(overrides?: Partial<TrainingPlan>): TrainingPlan {
  return {
    id: 'plan-1',
    userId: 'user-1',
    name: 'Test Plan',
    aiGenerated: false,
    status: 'active',
    routines: [],
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
    ...overrides,
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

function makeSet(overrides?: Partial<WorkoutSet>): WorkoutSet {
  return {
    id: 'set-1',
    sessionId: 'session-1',
    exerciseId: 'ex-1',
    weight: 60,
    reps: 10,
    timestamp: new Date().toISOString(),
    syncStatus: 'synced',
    ...overrides,
  };
}

describe('OfflineDataManager', () => {
  let manager: OfflineDataManager;

  beforeEach(() => {
    const store = createMockStore();
    const storage = new OfflineStorage(store);
    manager = new OfflineDataManager(storage);
  });

  // ─── Req 9.1: Cache active plan ──────────────────────────────

  describe('Active Training Plan (Req 9.1)', () => {
    it('should cache and retrieve the active training plan', async () => {
      const plan = makePlan({ name: 'Fuerza Semana 1' });
      await manager.cacheActivePlan(plan);

      const retrieved = await manager.getActivePlan();
      expect(retrieved).not.toBeNull();
      expect(retrieved!.id).toBe(plan.id);
      expect(retrieved!.name).toBe('Fuerza Semana 1');
    });

    it('should return null when no plan is cached', async () => {
      const result = await manager.getActivePlan();
      expect(result).toBeNull();
    });
  });

  // ─── Req 9.1: Cache scheduled routines ───────────────────────

  describe('Scheduled Routines (Req 9.1)', () => {
    it('should cache and retrieve scheduled routines', async () => {
      const routines: ScheduledRoutine[] = [
        { id: 'sr-1', routineId: 'r-1', routine: { id: 'r-1', planId: 'plan-1', name: 'Piernas', sortOrder: 0, exercises: [] }, scheduledDate: '2024-01-15', completed: false },
        { id: 'sr-2', routineId: 'r-2', routine: { id: 'r-2', planId: 'plan-1', name: 'Pecho', sortOrder: 1, exercises: [] }, scheduledDate: '2024-01-16', completed: false },
      ];
      await manager.cacheScheduledRoutines(routines);

      const retrieved = await manager.getScheduledRoutines();
      expect(retrieved).toHaveLength(2);
      expect(retrieved[0].routine.name).toBe('Piernas');
    });

    it('should return empty array when no routines cached', async () => {
      const result = await manager.getScheduledRoutines();
      expect(result).toEqual([]);
    });
  });

  // ─── Req 9.1: Cache 7-day workout history ────────────────────

  describe('Workout History (Req 9.1)', () => {
    it('should cache workout history and filter to last 7 days', async () => {
      const recent = makeSession({ id: 's-recent', startedAt: new Date().toISOString() });
      const old = makeSession({
        id: 's-old',
        startedAt: new Date(Date.now() - 10 * 24 * 60 * 60 * 1000).toISOString(),
      });

      await manager.cacheWorkoutHistory([recent, old]);
      const history = await manager.getWorkoutHistory();

      expect(history).toHaveLength(1);
      expect(history[0].id).toBe('s-recent');
    });
  });

  // ─── Req 9.2: Offline workout recording ──────────────────────

  describe('Offline Workout Recording (Req 9.2)', () => {
    it('should record a workout session offline with pending sync status', async () => {
      const session = makeSession({ id: 'offline-session' });
      await manager.recordOfflineWorkout(session);

      const history = await manager.getWorkoutHistory();
      expect(history).toHaveLength(1);
      expect(history[0].syncStatus).toBe('pending');

      const ops = await manager.getPendingOperations();
      expect(ops).toHaveLength(1);
      expect(ops[0].entity).toBe('workout_session');
      expect(ops[0].entityId).toBe('offline-session');
    });

    it('should record a workout set offline and add to session', async () => {
      const session = makeSession({ id: 'session-1' });
      await manager.recordOfflineWorkout(session);

      const set = makeSet({ id: 'set-offline', sessionId: 'session-1', weight: 80, reps: 8 });
      await manager.recordOfflineSet(set);

      const history = await manager.getWorkoutHistory();
      expect(history[0].sets).toHaveLength(1);
      expect(history[0].sets[0].weight).toBe(80);
      expect(history[0].sets[0].syncStatus).toBe('pending');

      const ops = await manager.getPendingOperations();
      expect(ops).toHaveLength(2); // session + set
    });
  });

  // ─── Req 9.3: Pending operations queue ────────────────────────

  describe('Pending Operations (Req 9.3)', () => {
    it('should return pending operations in chronological order', async () => {
      const session1 = makeSession({ id: 's1' });
      const session2 = makeSession({ id: 's2' });

      await manager.recordOfflineWorkout(session1);
      // Small delay to ensure different timestamps
      await manager.recordOfflineWorkout(session2);

      const ops = await manager.getPendingOperations();
      expect(ops).toHaveLength(2);
      expect(ops[0].timestamp <= ops[1].timestamp).toBe(true);
    });

    it('should remove a pending operation by id', async () => {
      await manager.recordOfflineWorkout(makeSession({ id: 's1' }));
      const ops = await manager.getPendingOperations();
      expect(ops).toHaveLength(1);

      await manager.removePendingOperation(ops[0].id);
      const remaining = await manager.getPendingOperations();
      expect(remaining).toHaveLength(0);
    });

    it('should clear all pending operations', async () => {
      await manager.recordOfflineWorkout(makeSession({ id: 's1' }));
      await manager.recordOfflineWorkout(makeSession({ id: 's2' }));

      await manager.clearPendingOperations();
      const ops = await manager.getPendingOperations();
      expect(ops).toHaveLength(0);
    });
  });

  // ─── Req 9.3: 72-hour offline window ─────────────────────────

  describe('72-hour Offline Window (Req 9.3)', () => {
    it('should not exceed offline period when just started', async () => {
      await manager.recordOfflineStart();
      const exceeded = await manager.isOfflinePeriodExceeded();
      expect(exceeded).toBe(false);
    });

    it('should report hours remaining', async () => {
      await manager.recordOfflineStart();
      const remaining = await manager.getOfflineHoursRemaining();
      // Should be close to 72 hours (within 1 hour tolerance)
      expect(remaining).toBeGreaterThan(71);
      expect(remaining).toBeLessThanOrEqual(72);
    });

    it('should report full hours when not offline', async () => {
      const remaining = await manager.getOfflineHoursRemaining();
      expect(remaining).toBe(72);
    });

    it('should reset offline state on sync', async () => {
      await manager.recordOfflineStart();
      await manager.recordSync();

      const metadata = await manager.getMetadata();
      expect(metadata.offlineSince).toBeUndefined();
    });
  });

  // ─── Metadata ─────────────────────────────────────────────────

  describe('Metadata', () => {
    it('should track pending operations count', async () => {
      await manager.recordOfflineWorkout(makeSession({ id: 's1' }));
      await manager.recordOfflineWorkout(makeSession({ id: 's2' }));

      const metadata = await manager.getMetadata();
      expect(metadata.pendingOperationsCount).toBe(2);
    });

    it('should update last sync timestamp', async () => {
      const before = new Date().toISOString();
      await manager.recordSync();
      const metadata = await manager.getMetadata();
      expect(metadata.lastSyncTimestamp >= before).toBe(true);
    });
  });
});
