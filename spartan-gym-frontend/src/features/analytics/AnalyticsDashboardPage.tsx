import {
  Box,
  Typography,
  CircularProgress,
  Alert,
  Card,
  CardContent,
  Grid2 as Grid,
} from '@mui/material';
import {
  BarChart,
  Bar,
  LineChart,
  Line,
  PieChart,
  Pie,
  Cell,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Legend,
} from 'recharts';
import { useTranslation } from 'react-i18next';
import { useGetDashboardMetricsQuery } from '@/api/analyticsApi';

const PIE_COLORS = ['#1976d2', '#e0e0e0'];

const MOCK_REVENUE_DATA = [
  { month: 'Jan', revenue: 42000 },
  { month: 'Feb', revenue: 45000 },
  { month: 'Mar', revenue: 48000 },
  { month: 'Apr', revenue: 51000 },
  { month: 'May', revenue: 47000 },
  { month: 'Jun', revenue: 53000 },
];

const MOCK_RETENTION_DATA = [
  { month: 'Jan', rate: 82 },
  { month: 'Feb', rate: 84 },
  { month: 'Mar', rate: 81 },
  { month: 'Apr', rate: 86 },
  { month: 'May', rate: 88 },
  { month: 'Jun', rate: 87 },
];

export default function AnalyticsDashboardPage() {
  const { t } = useTranslation('analytics');

  const { data: metrics, isLoading, isError } = useGetDashboardMetricsQuery({});

  if (isLoading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
        <CircularProgress aria-label={t('loading')} />
      </Box>
    );
  }

  if (isError) {
    return <Alert severity="error">{t('loadError')}</Alert>;
  }

  const occupancyData = [
    { name: t('occupied'), value: metrics?.averageOccupancy ?? 0 },
    { name: t('available'), value: 100 - (metrics?.averageOccupancy ?? 0) },
  ];

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        {t('dashboardTitle')}
      </Typography>

      {/* Metric cards */}
      <Grid container spacing={2} sx={{ mb: 3 }}>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <MetricCard
            title={t('totalUsers')}
            value={metrics?.totalUsers ?? 0}
            format="number"
          />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <MetricCard
            title={t('retentionRate')}
            value={metrics?.retentionRate ?? 0}
            format="percent"
          />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <MetricCard
            title={t('monthlyRevenue')}
            value={metrics?.monthlyRevenue ?? 0}
            format="currency"
          />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <MetricCard
            title={t('workoutsThisMonth')}
            value={metrics?.workoutsThisMonth ?? 0}
            format="number"
          />
        </Grid>
      </Grid>

      {/* Charts */}
      <Grid container spacing={2}>
        {/* Revenue BarChart */}
        <Grid size={{ xs: 12, md: 6 }}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                {t('revenueChart')}
              </Typography>
              <Box aria-label={t('revenueChart')} role="img" sx={{ height: 300 }}>
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={MOCK_REVENUE_DATA}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="month" />
                    <YAxis />
                    <Tooltip />
                    <Legend />
                    <Bar dataKey="revenue" fill="#1976d2" name={t('revenue')} />
                  </BarChart>
                </ResponsiveContainer>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* Retention LineChart */}
        <Grid size={{ xs: 12, md: 6 }}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                {t('retentionChart')}
              </Typography>
              <Box aria-label={t('retentionChart')} role="img" sx={{ height: 300 }}>
                <ResponsiveContainer width="100%" height="100%">
                  <LineChart data={MOCK_RETENTION_DATA}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="month" />
                    <YAxis domain={[70, 100]} />
                    <Tooltip />
                    <Legend />
                    <Line
                      type="monotone"
                      dataKey="rate"
                      stroke="#4caf50"
                      strokeWidth={2}
                      name={t('retentionRate')}
                    />
                  </LineChart>
                </ResponsiveContainer>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* Occupancy PieChart */}
        <Grid size={{ xs: 12, md: 6 }}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                {t('occupancyChart')}
              </Typography>
              <Box aria-label={t('occupancyChart')} role="img" sx={{ height: 300 }}>
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={occupancyData}
                      cx="50%"
                      cy="50%"
                      innerRadius={60}
                      outerRadius={100}
                      dataKey="value"
                      label={({ name, value }) => `${name}: ${value}%`}
                    >
                      {occupancyData.map((_, index) => (
                        <Cell key={`cell-${index}`} fill={PIE_COLORS[index]} />
                      ))}
                    </Pie>
                    <Tooltip />
                    <Legend />
                  </PieChart>
                </ResponsiveContainer>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
}

interface MetricCardProps {
  title: string;
  value: number;
  format: 'number' | 'percent' | 'currency';
}

function MetricCard({ title, value, format }: MetricCardProps) {
  const formatted =
    format === 'currency'
      ? `$${value.toLocaleString()}`
      : format === 'percent'
        ? `${value}%`
        : value.toLocaleString();

  return (
    <Card>
      <CardContent>
        <Typography variant="body2" color="text.secondary">
          {title}
        </Typography>
        <Typography variant="h4" aria-label={`${title}: ${formatted}`}>
          {formatted}
        </Typography>
      </CardContent>
    </Card>
  );
}
