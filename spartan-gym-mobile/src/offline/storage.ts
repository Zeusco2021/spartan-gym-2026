/**
 * Offline Storage Layer
 *
 * Provides a unified interface for local data persistence using AsyncStorage
 * for simple key-value data and SQLite for structured/queryable data.
 *
 * Validates: Requirements 9.1, 9.2, 9.3
 * - 9.1: Store active training plan, scheduled routines, 7-day history locally
 * - 9.2: Allow offline workout recording (sets, reps)
 * - 9.3: Support 72-hour offline operation without data loss
 */

import type { CachedData } from '../types/offline';

// AsyncStorage interface (abstracted for testability)
export interface IKeyValueStore {
  getItem(key: string): Promise<string | null>;
  setItem(key: string, value: string): Promise<void>;
  removeItem(key: string): Promise<void>;
  getAllKeys(): Promise<string[]>;
  multiGet(keys: string[]): Promise<[string, string | null][]>;
  multiRemove(keys: string[]): Promise<void>;
}

// SQLite interface (abstracted for testability)
export interface ISQLiteDatabase {
  executeSql(sql: string, params?: unknown[]): Promise<SQLiteResult>;
  transaction(fn: (tx: ISQLiteTransaction) => void): Promise<void>;
}

export interface ISQLiteTransaction {
  executeSql(sql: string, params?: unknown[]): void;
}

export interface SQLiteResult {
  rows: { length: number; item(index: number): Record<string, unknown> };
  insertId?: number;
  rowsAffected: number;
}

const STORAGE_KEYS = {
  ACTIVE_PLAN: 'offline:active_plan',
  SCHEDULED_ROUTINES: 'offline:scheduled_routines',
  WORKOUT_HISTORY: 'offline:workout_history',
  PENDING_OPERATIONS: 'offline:pending_operations',
  OFFLINE_METADATA: 'offline:metadata',
  LAST_SYNC: 'offline:last_sync',
} as const;

export { STORAGE_KEYS };

/**
 * OfflineStorage wraps AsyncStorage with typed get/set and TTL support.
 */
export class OfflineStorage {
  constructor(private store: IKeyValueStore) {}

  async get<T>(key: string): Promise<CachedData<T> | null> {
    const raw = await this.store.getItem(key);
    if (!raw) return null;

    const cached: CachedData<T> = JSON.parse(raw);
    const now = new Date().toISOString();

    if (cached.expiresAt && cached.expiresAt < now) {
      await this.store.removeItem(key);
      return null;
    }

    return cached;
  }

  async set<T>(key: string, data: T, ttlMs: number): Promise<void> {
    const now = new Date();
    const cached: CachedData<T> = {
      data,
      cachedAt: now.toISOString(),
      expiresAt: new Date(now.getTime() + ttlMs).toISOString(),
      version: 1,
    };
    await this.store.setItem(key, JSON.stringify(cached));
  }

  async remove(key: string): Promise<void> {
    await this.store.removeItem(key);
  }

  async clear(prefix: string): Promise<void> {
    const allKeys = await this.store.getAllKeys();
    const keysToRemove = allKeys.filter((k) => k.startsWith(prefix));
    if (keysToRemove.length > 0) {
      await this.store.multiRemove(keysToRemove);
    }
  }
}
