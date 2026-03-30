export interface Exercise {
  id: string;
  name: string;
  muscleGroups: string[];
  equipmentRequired?: string[];
  difficulty: 'beginner' | 'intermediate' | 'advanced';
  videoUrl?: string;
  instructions?: string;
}

export interface RoutineExercise {
  id: string;
  exercise: Exercise;
  sets: number;
  reps: string;
  restSeconds: number;
  sortOrder: number;
}

export interface Routine {
  id: string;
  planId: string;
  name: string;
  dayOfWeek?: number;
  sortOrder: number;
  exercises: RoutineExercise[];
}

export interface TrainingPlan {
  id: string;
  userId: string;
  trainerId?: string;
  name: string;
  description?: string;
  aiGenerated: boolean;
  status: 'active' | 'completed' | 'paused';
  routines: Routine[];
  createdAt: string;
}

export interface ExerciseRecommendation {
  exerciseId: string;
  exercise: Exercise;
  reason: string;
  confidence: number;
}

export interface OvertrainingResult {
  isOvertraining: boolean;
  fatigueScore: number;
  recommendation: string;
  suggestedRestDays: number;
}
