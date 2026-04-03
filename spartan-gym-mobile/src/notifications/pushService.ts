/**
 * Push Notification Service — Firebase Cloud Messaging wrapper
 *
 * Handles permission requests, token management, foreground/background messages,
 * and token registration with the backend.
 *
 * Validates: Requirement 9.5
 */

import messaging, { FirebaseMessagingTypes } from '@react-native-firebase/messaging';

const API_BASE_URL = 'https://api.spartangoldengym.com';

export type NotificationHandler = (message: FirebaseMessagingTypes.RemoteMessage) => void;

export interface PushService {
  requestPermission(): Promise<boolean>;
  getToken(): Promise<string | null>;
  onMessage(handler: NotificationHandler): () => void;
  onBackgroundMessage(handler: NotificationHandler): void;
  registerTokenWithBackend(token: string): Promise<boolean>;
}

/**
 * Request push notification permission from the user.
 * Returns true if permission was granted.
 */
export async function requestPermission(): Promise<boolean> {
  const authStatus = await messaging().requestPermission();
  return (
    authStatus === messaging.AuthorizationStatus.AUTHORIZED ||
    authStatus === messaging.AuthorizationStatus.PROVISIONAL
  );
}

/**
 * Get the FCM device token for this device.
 */
export async function getToken(): Promise<string | null> {
  try {
    return await messaging().getToken();
  } catch {
    return null;
  }
}

/**
 * Subscribe to foreground messages. Returns an unsubscribe function.
 */
export function onMessage(handler: NotificationHandler): () => void {
  return messaging().onMessage(handler);
}

/**
 * Register a handler for background/quit-state messages.
 * Must be called outside of any component (e.g., in index.js).
 */
export function onBackgroundMessage(handler: NotificationHandler): void {
  messaging().setBackgroundMessageHandler(async (remoteMessage) => {
    handler(remoteMessage);
  });
}

/**
 * Register the FCM token with the backend so the server can send targeted pushes.
 */
export async function registerTokenWithBackend(token: string): Promise<boolean> {
  try {
    const response = await fetch(`${API_BASE_URL}/api/notifications/register-device`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ token, platform: require('react-native').Platform.OS }),
    });
    return response.ok;
  } catch {
    return false;
  }
}

/**
 * Initialize push notifications: request permission, get token, register with backend.
 * Call this once during app startup.
 */
export async function initializePushNotifications(): Promise<void> {
  const granted = await requestPermission();
  if (!granted) return;

  const token = await getToken();
  if (token) {
    await registerTokenWithBackend(token);
  }

  // Listen for token refresh
  messaging().onTokenRefresh(async (newToken) => {
    await registerTokenWithBackend(newToken);
  });
}
