export interface WorkoutSet {
  id: string;
  exerciseId: string;
  weight: number;
  reps: number;
  restSeconds: number;
  timestamp: string;
}

export interface WorkoutSession {
  id: string;
  userId: string;
  startedAt: string;
  completedAt?: string;
  exercises: WorkoutSet[];
  totalDuration?: number;
  caloriesBurned?: number;
  status: 'active' | 'completed' | 'cancelled';
}

export interface ProgressDataPoint {
  date: string;
  volume: number;
  duration: number;
  caloriesBurned: number;
}

export interface WorkoutProgress {
  period: 'day' | 'week' | 'month' | 'year' | 'custom';
  data: ProgressDataPoint[];
}
