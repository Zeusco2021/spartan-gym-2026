/**
 * Unit tests for OfflineStorage
 * Validates: Requirements 9.1, 9.3
 */

import { OfflineStorage, type IKeyValueStore } from './storage';

/** In-memory mock of AsyncStorage for testing */
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

describe('OfflineStorage', () => {
  let store: IKeyValueStore;
  let storage: OfflineStorage;

  beforeEach(() => {
    store = createMockStore();
    storage = new OfflineStorage(store);
  });

  it('should store and retrieve data', async () => {
    const testData = { name: 'Plan A', exercises: [1, 2, 3] };
    await storage.set('test:key', testData, 60_000);

    const result = await storage.get<typeof testData>('test:key');
    expect(result).not.toBeNull();
    expect(result!.data).toEqual(testData);
  });

  it('should return null for non-existent keys', async () => {
    const result = await storage.get('nonexistent');
    expect(result).toBeNull();
  });

  it('should remove data', async () => {
    await storage.set('test:key', 'value', 60_000);
    await storage.remove('test:key');
    const result = await storage.get('test:key');
    expect(result).toBeNull();
  });

  it('should expire data after TTL', async () => {
    // Set with a TTL that's already expired (negative TTL)
    await storage.set('test:expired', 'old', -1);
    const result = await storage.get('test:expired');
    expect(result).toBeNull();
  });

  it('should clear keys by prefix', async () => {
    await storage.set('offline:a', 1, 60_000);
    await storage.set('offline:b', 2, 60_000);
    await storage.set('other:c', 3, 60_000);

    await storage.clear('offline:');

    expect(await storage.get('offline:a')).toBeNull();
    expect(await storage.get('offline:b')).toBeNull();
    expect(await storage.get('other:c')).not.toBeNull();
  });

  it('should include cachedAt and expiresAt metadata', async () => {
    const before = new Date().toISOString();
    await storage.set('test:meta', { x: 1 }, 3_600_000);
    const after = new Date().toISOString();

    const result = await storage.get<{ x: number }>('test:meta');
    expect(result).not.toBeNull();
    expect(result!.cachedAt >= before).toBe(true);
    expect(result!.cachedAt <= after).toBe(true);
    expect(result!.expiresAt > result!.cachedAt).toBe(true);
  });
});
