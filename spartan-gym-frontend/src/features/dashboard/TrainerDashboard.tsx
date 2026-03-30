import {
  Box,
  Card,
  CardContent,
  Typography,
  CircularProgress,
  Alert,
  Grid2 as Grid,
} from '@mui/material';
import { useTranslation } from 'react-i18next';
import { useGetPlansQuery } from '@/api/trainingApi';
import { useGetClassesQuery } from '@/api/bookingsApi';
import { useGetWorkoutHistoryQuery } from '@/api/workoutsApi';

export default function TrainerDashboard() {
  const { t } = useTranslation('dashboard');
  const today = new Date().toISOString().split('T')[0];

  const {
    data: plansData,
    isLoading: plansLoading,
    isError: plansError,
  } = useGetPlansQuery({ page: 0 }, { refetchOnMountOrArgChange: true });

  const {
    data: classesData,
    isLoading: classesLoading,
    isError: classesError,
  } = useGetClassesQuery({ from: today, to: today }, { refetchOnMountOrArgChange: true });

  const {
    data: workoutData,
    isLoading: workoutsLoading,
    isError: workoutsError,
  } = useGetWorkoutHistoryQuery({ page: 0 }, { refetchOnMountOrArgChange: true });

  return (
    <Grid container spacing={3}>
      {/* Assigned Clients (via Training Plans) */}
      <Grid size={{ xs: 12, md: 4 }}>
        <Card>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              {t('assignedClients')}
            </Typography>
            {plansLoading && <CircularProgress size={24} />}
            {plansError && <Alert severity="error">{t('error')}</Alert>}
            {plansData && (
              <Box>
                <Typography variant="h4" color="primary">
                  {plansData.totalElements}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  {t('plans')}
                </Typography>
                {plansData.content.slice(0, 5).map((plan) => (
                  <Box key={plan.id} sx={{ mt: 1 }}>
                    <Typography variant="body2" fontWeight="bold">
                      {plan.name}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      {plan.status}
                    </Typography>
                  </Box>
                ))}
                {plansData.totalElements === 0 && (
                  <Typography variant="body2" color="text.secondary">
                    {t('noData')}
                  </Typography>
                )}
              </Box>
            )}
          </CardContent>
        </Card>
      </Grid>

      {/* Today's Sessions */}
      <Grid size={{ xs: 12, md: 4 }}>
        <Card>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              {t('todaySessions')}
            </Typography>
            {classesLoading && <CircularProgress size={24} />}
            {classesError && <Alert severity="error">{t('error')}</Alert>}
            {classesData && (
              <Box>
                {classesData.content.map((cls) => (
                  <Box key={cls.id} sx={{ mt: 1 }}>
                    <Typography variant="body2" fontWeight="bold">
                      {cls.name}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      {new Date(cls.scheduledAt).toLocaleTimeString()} — {cls.room ?? ''} ({cls.currentCapacity}/{cls.maxCapacity})
                    </Typography>
                  </Box>
                ))}
                {classesData.content.length === 0 && (
                  <Typography variant="body2" color="text.secondary">
                    {t('noData')}
                  </Typography>
                )}
              </Box>
            )}
          </CardContent>
        </Card>
      </Grid>

      {/* Progress Alerts */}
      <Grid size={{ xs: 12, md: 4 }}>
        <Card>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              {t('progressAlerts')}
            </Typography>
            {workoutsLoading && <CircularProgress size={24} />}
            {workoutsError && <Alert severity="error">{t('error')}</Alert>}
            {workoutData && (
              <Box>
                <Typography variant="body2" color="text.secondary">
                  {t('recentWorkouts')}: {workoutData.totalElements}
                </Typography>
                {workoutData.content.slice(0, 5).map((session) => (
                  <Box key={session.id} sx={{ mt: 1 }}>
                    <Typography variant="body2">
                      {new Date(session.startedAt).toLocaleDateString()} — {session.status}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      {session.totalDuration ?? 0} {t('min')}, {session.caloriesBurned ?? 0} {t('kcal')}
                    </Typography>
                  </Box>
                ))}
                {workoutData.totalElements === 0 && (
                  <Typography variant="body2" color="text.secondary">
                    {t('noData')}
                  </Typography>
                )}
              </Box>
            )}
          </CardContent>
        </Card>
      </Grid>
    </Grid>
  );
}
