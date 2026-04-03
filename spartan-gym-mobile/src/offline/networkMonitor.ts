/**
 * Network Monitor
 *
 * Provides an abstracted interface for network state and triggers
 * sync when transitioning from offline to online.
 *
 * Validates: Requirement 9.4
 */

import type { OfflineDataManager } from './offlineDataManager';
import type { SyncManager, SyncResult } from './syncManager';

/** Abstracted network state provider */
export interface INetworkStateProvider {
  isOnline(): boolean;
  addListener(callback: (isOnline: boolean) => void): () => void;
}

export type NetworkStatusCallback = (isOnline: boolean) => void;
export type SyncCompleteCallback = (result: SyncResult) => void;

export class NetworkMonitor {
  private wasOnline: boolean;
  private unsubscribe: (() => void) | null = null;
  private onSyncComplete: SyncCompleteCallback | null = null;

  constructor(
    private networkProvider: INetworkStateProvider,
    private syncManager: SyncManager,
    private offlineDataManager: OfflineDataManager,
  ) {
    this.wasOnline = networkProvider.isOnline();
  }

  /** Start monitoring network changes. */
  start(): void {
    this.unsubscribe = this.networkProvider.addListener((isOnline) => {
      this.handleNetworkChange(isOnline);
    });
  }

  /** Stop monitoring network changes. */
  stop(): void {
    if (this.unsubscribe) {
      this.unsubscribe();
      this.unsubscribe = null;
    }
  }

  /** Register a callback for when sync completes after reconnection. */
  onSync(callback: SyncCompleteCallback): void {
    this.onSyncComplete = callback;
  }

  /** Handle a network state transition. */
  private async handleNetworkChange(isOnline: boolean): Promise<void> {
    const wasOffline = !this.wasOnline;
    this.wasOnline = isOnline;

    if (!isOnline) {
      // Going offline — record the start time
      await this.offlineDataManager.recordOfflineStart();
      return;
    }

    if (wasOffline && isOnline) {
      // Transitioning from offline to online — trigger sync
      const result = await this.syncManager.syncAll();
      if (this.onSyncComplete) {
        this.onSyncComplete(result);
      }
    }
  }
}
