export interface DashboardMetrics {
  totalUsers: number;
  activeSubscriptions: number;
  monthlyRevenue: number;
  averageOccupancy: number;
  retentionRate: number;
  workoutsThisMonth: number;
}

export interface AnalyticsReport {
  id: string;
  type: 'weekly' | 'monthly';
  generatedAt: string;
  metrics: Record<string, number>;
  downloadUrl: string;
}
