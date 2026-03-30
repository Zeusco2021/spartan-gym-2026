export interface NotificationPreferences {
  email: boolean;
  push: boolean;
  sms: boolean;
  categories: Record<string, { email: boolean; push: boolean; sms: boolean }>;
}

export interface AppNotification {
  id: string;
  userId: string;
  type: string;
  title: string;
  message: string;
  read: boolean;
  createdAt: string;
}
