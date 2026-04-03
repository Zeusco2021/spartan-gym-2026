/**
 * Types for the offline storage and sync system.
 * Supports Req 9.1, 9.2, 9.3: offline operation up to 72 hours.
 */

export interface OfflineMetadata {
  lastSyncTimestamp: string;
  offlineSince?: string;
  pendingOperationsCount: number;
  storageUsedBytes: number;
}

export interface PendingOperation {
  id: string;
  type: 'create' | 'update' | 'delete';
  entity: 'workout_session' | 'workout_set' | 'meal_log';
  entityId: string;
  payload: unknown;
  timestamp: string;
  retryCount: number;
}

export interface CachedData<T> {
  data: T;
  cachedAt: string;
  expiresAt: string;
  version: number;
}

export const OFFLINE_CONFIG = {
  /** Maximum offline operation period in hours (Req 9.3) */
  MAX_OFFLINE_HOURS: 72,
  /** Number of days of workout history to cache (Req 9.1) */
  HISTORY_DAYS: 7,
  /** Maximum pending operations before warning user */
  MAX_PENDING_OPERATIONS: 1000,
  /** Cache TTL in milliseconds (72 hours) */
  CACHE_TTL_MS: 72 * 60 * 60 * 1000,
} as const;
