import {
  Box,
  Card,
  CardContent,
  Typography,
  CircularProgress,
  Alert,
  Grid2 as Grid,
  LinearProgress,
} from '@mui/material';
import { useTranslation } from 'react-i18next';
import { useGetDashboardMetricsQuery } from '@/api/analyticsApi';
import { useGetNearbyGymsQuery } from '@/api/gymsApi';

export default function AdminDashboard() {
  const { t } = useTranslation('dashboard');

  const {
    data: metrics,
    isLoading: metricsLoading,
    isError: metricsError,
  } = useGetDashboardMetricsQuery({}, { refetchOnMountOrArgChange: true });

  const {
    data: gyms,
    isLoading: gymsLoading,
    isError: gymsError,
  } = useGetNearbyGymsQuery(
    { lat: 0, lng: 0, radius: 100 },
    { refetchOnMountOrArgChange: true },
  );

  return (
    <Grid container spacing={3}>
      {/* Business Metrics */}
      <Grid size={{ xs: 12 }}>
        <Card>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              {t('businessMetrics')}
            </Typography>
            {metricsLoading && <CircularProgress size={24} />}
            {metricsError && <Alert severity="error">{t('error')}</Alert>}
            {metrics && (
              <Grid container spacing={2}>
                {[
                  { label: t('totalUsers'), value: metrics.totalUsers },
                  { label: t('activeSubscriptions'), value: metrics.activeSubscriptions },
                  { label: t('monthlyRevenue'), value: `$${metrics.monthlyRevenue.toLocaleString()}` },
                  { label: t('retentionRate'), value: `${metrics.retentionRate}%` },
                ].map(({ label, value }) => (
                  <Grid size={{ xs: 6, md: 3 }} key={label}>
                    <Box sx={{ textAlign: 'center' }}>
                      <Typography variant="h4" color="primary">
                        {value}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        {label}
                      </Typography>
                    </Box>
                  </Grid>
                ))}
              </Grid>
            )}
          </CardContent>
        </Card>
      </Grid>

      {/* Gym Occupancy */}
      <Grid size={{ xs: 12, md: 6 }}>
        <Card>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              {t('gymOccupancy')}
            </Typography>
            {gymsLoading && <CircularProgress size={24} />}
            {gymsError && <Alert severity="error">{t('error')}</Alert>}
            {gyms && (
              <Box>
                {gyms.map((gym) => (
                  <Box key={gym.id} sx={{ mt: 1 }}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                      <Typography variant="body2">{gym.name}</Typography>
                      <Typography variant="body2" color="text.secondary">
                        {gym.currentOccupancy ?? 0} / {gym.maxCapacity}
                      </Typography>
                    </Box>
                    <LinearProgress
                      variant="determinate"
                      value={Math.min(
                        ((gym.currentOccupancy ?? 0) / gym.maxCapacity) * 100,
                        100,
                      )}
                      sx={{ mt: 0.5 }}
                    />
                  </Box>
                ))}
                {gyms.length === 0 && (
                  <Typography variant="body2" color="text.secondary">
                    {t('noData')}
                  </Typography>
                )}
              </Box>
            )}
          </CardContent>
        </Card>
      </Grid>

      {/* Revenue */}
      <Grid size={{ xs: 12, md: 6 }}>
        <Card>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              {t('revenue')}
            </Typography>
            {metricsLoading && <CircularProgress size={24} />}
            {metricsError && <Alert severity="error">{t('error')}</Alert>}
            {metrics && (
              <Box sx={{ textAlign: 'center', py: 2 }}>
                <Typography variant="h3" color="primary">
                  ${metrics.monthlyRevenue.toLocaleString()}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  {t('monthlyRevenue')}
                </Typography>
              </Box>
            )}
          </CardContent>
        </Card>
      </Grid>
    </Grid>
  );
}
