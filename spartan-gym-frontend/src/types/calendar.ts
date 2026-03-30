export interface CalendarEvent {
  id: string;
  userId: string;
  eventType: 'workout' | 'class' | 'trainer_session' | 'nutrition_reminder' | 'custom';
  referenceId?: string;
  title: string;
  startsAt: string;
  endsAt: string;
  reminderMinutes: number;
}
