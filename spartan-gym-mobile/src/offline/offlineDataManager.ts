/**
 * Offline Data Manager
 *
 * Manages caching of training plans, scheduled routines, and workout history
 * for offline use. Handles the 72-hour offline window and pending operations queue.
 *
 * Validates: Requirements 9.1, 9.2, 9.3
 */

import { OFFLINE_CONFIG, type OfflineMetadata, type PendingOperation } from '../types/offline';
import type {
  TrainingPlan,
  WorkoutSession,
  WorkoutSet,
  ScheduledRoutine,
} from '../types/training';
import { OfflineStorage, STORAGE_KEYS } from './storage';

export class OfflineDataManager {
  private storage: OfflineStorage;

  constructor(storage: OfflineStorage) {
    this.storage = storage;
  }

  // ─── Training Plan Cache (Req 9.1) ───────────────────────────

  /**
   * Cache the user's active training plan for offline access.
   */
  async cacheActivePlan(plan: TrainingPlan): Promise<void> {
    await this.storage.set(
      STORAGE_KEYS.ACTIVE_PLAN,
      plan,
      OFFLINE_CONFIG.CACHE_TTL_MS
    );
  }

  /**
   * Retrieve the cached active training plan.
   */
  async getActivePlan(): Promise<TrainingPlan | null> {
    const cached = await this.storage.get<TrainingPlan>(STORAGE_KEYS.ACTIVE_PLAN);
    return cached?.data ?? null;
  }

  // ─── Scheduled Routines Cache (Req 9.1) ──────────────────────

  /**
   * Cache scheduled routines for offline access.
   */
  async cacheScheduledRoutines(routines: ScheduledRoutine[]): Promise<void> {
    await this.storage.set(
      STORAGE_KEYS.SCHEDULED_ROUTINES,
      routines,
      OFFLINE_CONFIG.CACHE_TTL_MS
    );
  }

  /**
   * Retrieve cached scheduled routines.
   */
  async getScheduledRoutines(): Promise<ScheduledRoutine[]> {
    const cached = await this.storage.get<ScheduledRoutine[]>(
      STORAGE_KEYS.SCHEDULED_ROUTINES
    );
    return cached?.data ?? [];
  }

  // ─── Workout History Cache (Req 9.1) ─────────────────────────

  /**
   * Cache the last 7 days of workout history.
   */
  async cacheWorkoutHistory(sessions: WorkoutSession[]): Promise<void> {
    const sevenDaysAgo = new Date();
    sevenDaysAgo.setDate(sevenDaysAgo.getDate() - OFFLINE_CONFIG.HISTORY_DAYS);
    const cutoff = sevenDaysAgo.toISOString();

    // Only keep sessions from the last 7 days
    const recentSessions = sessions.filter((s) => s.startedAt >= cutoff);

    await this.storage.set(
      STORAGE_KEYS.WORKOUT_HISTORY,
      recentSessions,
      OFFLINE_CONFIG.CACHE_TTL_MS
    );
  }

  /**
   * Retrieve cached workout history (last 7 days).
   */
  async getWorkoutHistory(): Promise<WorkoutSession[]> {
    const cached = await this.storage.get<WorkoutSession[]>(
      STORAGE_KEYS.WORKOUT_HISTORY
    );
    return cached?.data ?? [];
  }

  // ─── Offline Workout Recording (Req 9.2) ─────────────────────

  /**
   * Record a new workout session locally while offline.
   * The session is stored in history and a pending sync operation is queued.
   */
  async recordOfflineWorkout(session: WorkoutSession): Promise<void> {
    const markedSession: WorkoutSession = {
      ...session,
      syncStatus: 'pending',
    };

    // Add to local history
    const history = await this.getWorkoutHistory();
    history.push(markedSession);
    await this.storage.set(
      STORAGE_KEYS.WORKOUT_HISTORY,
      history,
      OFFLINE_CONFIG.CACHE_TTL_MS
    );

    // Queue sync operation
    await this.addPendingOperation({
      id: `op_${Date.now()}_${session.id}`,
      type: 'create',
      entity: 'workout_session',
      entityId: session.id,
      payload: markedSession,
      timestamp: new Date().toISOString(),
      retryCount: 0,
    });
  }

  /**
   * Record a workout set locally while offline.
   */
  async recordOfflineSet(set: WorkoutSet): Promise<void> {
    const markedSet: WorkoutSet = {
      ...set,
      syncStatus: 'pending',
    };

    // Update the session in history with the new set
    const history = await this.getWorkoutHistory();
    const sessionIndex = history.findIndex((s) => s.id === set.sessionId);
    if (sessionIndex >= 0) {
      history[sessionIndex].sets.push(markedSet);
      await this.storage.set(
        STORAGE_KEYS.WORKOUT_HISTORY,
        history,
        OFFLINE_CONFIG.CACHE_TTL_MS
      );
    }

    // Queue sync operation
    await this.addPendingOperation({
      id: `op_${Date.now()}_${set.id}`,
      type: 'create',
      entity: 'workout_set',
      entityId: set.id,
      payload: markedSet,
      timestamp: new Date().toISOString(),
      retryCount: 0,
    });
  }

  // ─── Pending Operations Queue (Req 9.3) ──────────────────────

  /**
   * Add a pending operation to the sync queue.
   */
  async addPendingOperation(operation: PendingOperation): Promise<void> {
    const operations = await this.getPendingOperations();
    operations.push(operation);
    await this.storage.set(
      STORAGE_KEYS.PENDING_OPERATIONS,
      operations,
      OFFLINE_CONFIG.CACHE_TTL_MS
    );
    await this.updateMetadata();
  }

  /**
   * Get all pending operations sorted by timestamp (chronological order).
   */
  async getPendingOperations(): Promise<PendingOperation[]> {
    const cached = await this.storage.get<PendingOperation[]>(
      STORAGE_KEYS.PENDING_OPERATIONS
    );
    const ops = cached?.data ?? [];
    return ops.sort((a, b) => a.timestamp.localeCompare(b.timestamp));
  }

  /**
   * Remove a pending operation after successful sync.
   */
  async removePendingOperation(operationId: string): Promise<void> {
    const operations = await this.getPendingOperations();
    const filtered = operations.filter((op) => op.id !== operationId);
    await this.storage.set(
      STORAGE_KEYS.PENDING_OPERATIONS,
      filtered,
      OFFLINE_CONFIG.CACHE_TTL_MS
    );
    await this.updateMetadata();
  }

  /**
   * Clear all pending operations (after full sync).
   */
  async clearPendingOperations(): Promise<void> {
    await this.storage.remove(STORAGE_KEYS.PENDING_OPERATIONS);
    await this.updateMetadata();
  }

  // ─── Metadata & Status ────────────────────────────────────────

  /**
   * Get offline metadata (sync status, pending count, etc.).
   */
  async getMetadata(): Promise<OfflineMetadata> {
    const cached = await this.storage.get<OfflineMetadata>(
      STORAGE_KEYS.OFFLINE_METADATA
    );
    return (
      cached?.data ?? {
        lastSyncTimestamp: new Date().toISOString(),
        pendingOperationsCount: 0,
        storageUsedBytes: 0,
      }
    );
  }

  /**
   * Record the timestamp of the last successful sync.
   */
  async recordSync(): Promise<void> {
    const metadata = await this.getMetadata();
    metadata.lastSyncTimestamp = new Date().toISOString();
    metadata.offlineSince = undefined;
    metadata.pendingOperationsCount = 0;
    await this.storage.set(
      STORAGE_KEYS.OFFLINE_METADATA,
      metadata,
      OFFLINE_CONFIG.CACHE_TTL_MS
    );
  }

  /**
   * Record when the device goes offline.
   */
  async recordOfflineStart(): Promise<void> {
    const metadata = await this.getMetadata();
    if (!metadata.offlineSince) {
      metadata.offlineSince = new Date().toISOString();
    }
    await this.storage.set(
      STORAGE_KEYS.OFFLINE_METADATA,
      metadata,
      OFFLINE_CONFIG.CACHE_TTL_MS
    );
  }

  /**
   * Check if the offline period has exceeded the 72-hour limit (Req 9.3).
   */
  async isOfflinePeriodExceeded(): Promise<boolean> {
    const metadata = await this.getMetadata();
    if (!metadata.offlineSince) return false;

    const offlineSince = new Date(metadata.offlineSince).getTime();
    const now = Date.now();
    const maxOfflineMs = OFFLINE_CONFIG.MAX_OFFLINE_HOURS * 60 * 60 * 1000;

    return now - offlineSince > maxOfflineMs;
  }

  /**
   * Get the number of hours remaining in the offline window.
   */
  async getOfflineHoursRemaining(): Promise<number> {
    const metadata = await this.getMetadata();
    if (!metadata.offlineSince) return OFFLINE_CONFIG.MAX_OFFLINE_HOURS;

    const offlineSince = new Date(metadata.offlineSince).getTime();
    const now = Date.now();
    const elapsedHours = (now - offlineSince) / (60 * 60 * 1000);

    return Math.max(0, OFFLINE_CONFIG.MAX_OFFLINE_HOURS - elapsedHours);
  }

  private async updateMetadata(): Promise<void> {
    const operations = await this.getPendingOperations();
    const metadata = await this.getMetadata();
    metadata.pendingOperationsCount = operations.length;
    await this.storage.set(
      STORAGE_KEYS.OFFLINE_METADATA,
      metadata,
      OFFLINE_CONFIG.CACHE_TTL_MS
    );
  }
}
