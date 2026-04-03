export interface TrainerClient {
  id: string;
  name: string;
  email: string;
  profilePhotoUrl?: string;
  fitnessGoals?: Record<string, unknown>;
  assignedAt: string;
}
