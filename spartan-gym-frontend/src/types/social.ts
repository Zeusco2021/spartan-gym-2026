export interface Challenge {
  id: string;
  name: string;
  description: string;
  type: 'weekly' | 'monthly';
  metric: 'distance' | 'weight_lifted' | 'workouts_completed';
  targetValue: number;
  startDate: string;
  endDate: string;
  participantCount: number;
}

export interface Achievement {
  id: string;
  type: string;
  name: string;
  description: string;
  earnedAt: string;
  iconUrl: string;
}

export interface RankingEntry {
  userId: string;
  userName: string;
  profilePhotoUrl?: string;
  score: number;
  rank: number;
}

export interface SocialGroup {
  id: string;
  name: string;
  description: string;
  memberCount: number;
  createdAt: string;
}
