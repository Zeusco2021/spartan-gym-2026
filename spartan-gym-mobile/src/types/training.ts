/**
 * Core training domain types used across the mobile app.
 * Mirrors backend Servicio_Entrenamiento models.
 */

export interface TrainingPlan {
  id: string;
  userId: string;
  trainerId?: string;
  name: string;
  description?: string;
  aiGenerated: boolean;
  status: 'active' | 'paused' | 'completed' | 'archived';
  routines: Routine[];
  createdAt: string;
  updatedAt: string;
}

export interface Routine {
  id: string;
  planId: string;
  name: string;
  dayOfWeek?: number; // 0=Sunday, 6=Saturday
  sortOrder: number;
  exercises: RoutineExercise[];
}

export interface RoutineExercise {
  id: string;
  routineId: string;
  exerciseId: string;
  exercise: Exercise;
  sets: number;
  reps: string; // e.g. "8-12" or "10"
  restSeconds: number;
  sortOrder: number;
}

export interface Exercise {
  id: string;
  name: string;
  muscleGroups: string[];
  equipmentRequired?: string[];
  difficulty?: 'beginner' | 'intermediate' | 'advanced';
  videoUrl?: string;
  instructions?: string;
}

export interface WorkoutSession {
  id: string;
  userId: string;
  planId?: string;
  routineId?: string;
  startedAt: string;
  completedAt?: string;
  sets: WorkoutSet[];
  totalDuration?: number; // seconds
  caloriesBurned?: number;
  status: 'active' | 'completed' | 'cancelled';
  syncStatus: SyncStatus;
}

export interface WorkoutSet {
  id: string;
  sessionId: string;
  exerciseId: string;
  weight: number;
  reps: number;
  restSeconds?: number;
  timestamp: string;
  syncStatus: SyncStatus;
}

export type SyncStatus = 'synced' | 'pending' | 'failed';

export interface ScheduledRoutine {
  id: string;
  routineId: string;
  routine: Routine;
  scheduledDate: string;
  completed: boolean;
}
