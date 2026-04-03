/**
 * Sync Manager
 *
 * Processes pending offline operations in chronological order when
 * connectivity is restored. Implements last-write-wins conflict resolution.
 *
 * Validates: Requirement 9.4
 */

import type { PendingOperation } from '../types/offline';
import type { OfflineDataManager } from './offlineDataManager';

/** Server response for a sync operation */
export interface SyncResponse {
  success: boolean;
  serverUpdatedAt?: string;
  error?: string;
}

/** Abstracted API client for sending sync operations to the backend */
export interface IApiClient {
  sendOperation(operation: PendingOperation): Promise<SyncResponse>;
}

/** Result of syncing a single operation */
export interface SyncOperationResult {
  operationId: string;
  status: 'synced' | 'conflict_local_wins' | 'conflict_server_wins' | 'failed';
  error?: string;
}

/** Result of a full sync run */
export interface SyncResult {
  totalProcessed: number;
  succeeded: number;
  failed: number;
  results: SyncOperationResult[];
}

export class SyncManager {
  constructor(
    private offlineDataManager: OfflineDataManager,
    private apiClient: IApiClient,
  ) {}

  /**
   * Sync all pending operations in chronological order.
   * Operations that succeed are removed from the queue.
   * Operations that fail have their retryCount incremented.
   */
  async syncAll(): Promise<SyncResult> {
    const operations = await this.offlineDataManager.getPendingOperations();
    const results: SyncOperationResult[] = [];
    let succeeded = 0;
    let failed = 0;

    for (const op of operations) {
      const result = await this.syncOne(op.id);
      results.push(result);
      if (result.status === 'failed') {
        failed++;
      } else {
        succeeded++;
      }
    }

    if (succeeded > 0 && failed === 0) {
      await this.offlineDataManager.recordSync();
    }

    return {
      totalProcessed: operations.length,
      succeeded,
      failed,
      results,
    };
  }

  /**
   * Sync a single pending operation by its ID.
   * Sends to backend, resolves conflicts with last-write-wins, and
   * removes from queue on success.
   */
  async syncOne(operationId: string): Promise<SyncOperationResult> {
    const operations = await this.offlineDataManager.getPendingOperations();
    const operation = operations.find((op) => op.id === operationId);

    if (!operation) {
      return { operationId, status: 'failed', error: 'Operation not found' };
    }

    try {
      const response = await this.apiClient.sendOperation(operation);

      if (response.success) {
        await this.offlineDataManager.removePendingOperation(operationId);
        return { operationId, status: 'synced' };
      }

      // Conflict: apply last-write-wins
      if (response.serverUpdatedAt) {
        return this.resolveConflict(operation, response.serverUpdatedAt);
      }

      // Generic failure — increment retry count
      return this.handleFailure(operation, response.error);
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Unknown error';
      return this.handleFailure(operation, message);
    }
  }

  /**
   * Last-write-wins conflict resolution.
   * Compares local operation timestamp with server updatedAt.
   * The newer timestamp wins.
   */
  private async resolveConflict(
    operation: PendingOperation,
    serverUpdatedAt: string,
  ): Promise<SyncOperationResult> {
    const localTime = new Date(operation.timestamp).getTime();
    const serverTime = new Date(serverUpdatedAt).getTime();

    if (localTime >= serverTime) {
      // Local wins — remove from queue (data already sent, server should accept)
      await this.offlineDataManager.removePendingOperation(operation.id);
      return { operationId: operation.id, status: 'conflict_local_wins' };
    }

    // Server wins — discard local change
    await this.offlineDataManager.removePendingOperation(operation.id);
    return { operationId: operation.id, status: 'conflict_server_wins' };
  }

  /**
   * Handle a failed sync attempt by incrementing the retry count.
   */
  private async handleFailure(
    operation: PendingOperation,
    error?: string,
  ): Promise<SyncOperationResult> {
    // Increment retry count by re-adding with updated retryCount
    await this.offlineDataManager.removePendingOperation(operation.id);
    await this.offlineDataManager.addPendingOperation({
      ...operation,
      retryCount: operation.retryCount + 1,
    });

    return {
      operationId: operation.id,
      status: 'failed',
      error: error ?? 'Sync failed',
    };
  }
}
