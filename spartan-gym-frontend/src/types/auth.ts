export interface FitnessGoals {
  primaryGoal: string;
  weeklyWorkouts: number;
  targetWeight?: number;
}

export interface User {
  id: string;
  email: string;
  name: string;
  dateOfBirth: string;
  role: 'client' | 'trainer' | 'admin';
  locale: string;
  mfaEnabled: boolean;
  onboardingCompleted: boolean;
  profilePhotoUrl?: string;
  fitnessGoals?: FitnessGoals;
  medicalConditions?: string[];
}

export interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  mfaPending: boolean;
  mfaSessionToken: string | null;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  user: User;
  mfaRequired?: boolean;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  dateOfBirth: string;
}

export interface OnboardingState {
  currentStep: number;
  completed: boolean;
  data: Partial<OnboardingData>;
}

export interface OnboardingData {
  fitnessLevel: string;
  goals: string[];
  medicalConditions?: string[];
  availableEquipment?: string[];
}
