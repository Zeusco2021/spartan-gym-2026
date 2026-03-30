export interface GroupClass {
  id: string;
  gymId: string;
  instructorId: string;
  instructorName: string;
  name: string;
  room?: string;
  maxCapacity: number;
  currentCapacity: number;
  difficultyLevel: 'beginner' | 'intermediate' | 'advanced';
  scheduledAt: string;
  durationMinutes: number;
}

export interface ClassReservation {
  id: string;
  classId: string;
  userId: string;
  status: 'confirmed' | 'cancelled' | 'waitlisted';
  reservedAt: string;
}

export interface TrainerAvailability {
  trainerId: string;
  date: string;
  slots: { startTime: string; endTime: string; available: boolean }[];
}
