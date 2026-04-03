import { useState } from 'react';
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
import { useGetWorkoutHistoryQuery, useGetProgressQuery } from '@/api/workoutsApi';
import { useGetClassesQuery } from '@/api/bookingsApi';
import { useGetDailyBalanceQuery } from '@/api/nutritionApi';
import { useGetAchievementsQuery } from '@/api/socialApi';
import { ProgressChart } from '@/components/Charts';

export default function ClientDashboard() {
  const { t } = useTranslation('dashboard');
  const [progressPeriod, setProgressPeriod] = useState<'day' | 'month' | 'year'>('month');

  const {
    data: workoutData,
    isLoading: workoutsLoading,
    isError: workoutsError,
  } = useGetWorkoutHistoryQuery({ page: 0 }, { refetchOnMountOrArgChange: true });

  const {
    data: classesData,
    isLoading: classesLoading,
    isError: classesError,
  } = useGetClassesQuery({}, { refetchOnMountOrArgChange: true });

  const {
    data: balanceData,
    isLoading: balanceLoading,
    isError: balanceError,
  } = useGetDailyBalanceQuery({}, { refetchOnMountOrArgChange: true });

  const {
    data: achievements,
    isLoading: achievementsLoading,
    isError: achievementsError,
  } = useGetAchievementsQuery(undefined, { refetchOnMountOrArgChange: true });

  const {
    data: progressData,
    isLoading: progressLoading,
    isError: progressError,
  } = useGetProgressQuery({ period: progressPeriod }, { refetchOnMountOrArgChange: true });

  return (
    <Grid container spacing={3}>
      {/* Training Summary */}
      <Grid size={{ xs: 12, md: 6 }}>
        <Card>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              {t('trainingSummary')}
            </Typography>
            {workoutsLoading && <CircularProgress aria-label={t('loading')} size={24} />}
            {workoutsError && <Alert severity="error">{t('error')}</Alert>}
            {workoutData && (
              <Box>
                <Typography variant="body2" color="text.secondary">
                  {t('workouts')}: {workoutData.totalElements}
                </Typography>
                {workoutData.content.slice(0, 3).map((session) => (
                  <Box key={session.id} sx={{ mt: 1 }}>
                    <Typography variant="body2">
                      {new Date(session.startedAt).toLocaleDateString()} —{' '}
                      {session.totalDuration ?? 0} {t('min')},{' '}
                      {session.caloriesBurned ?? 0} {t('kcal')}
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

      {/* Upcoming Classes */}
      <Grid size={{ xs: 12, md: 6 }}>
        <Card>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              {t('upcomingClasses')}
            </Typography>
            {classesLoading && <CircularProgress aria-label={t('loading')} size={24} />}
            {classesError && <Alert severity="error">{t('error')}</Alert>}
            {classesData && (
              <Box>
                {classesData.content.slice(0, 3).map((cls) => (
                  <Box key={cls.id} sx={{ mt: 1 }}>
                    <Typography variant="body2" fontWeight="bold">
                      {cls.name}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      {new Date(cls.scheduledAt).toLocaleString()} — {cls.instructorName}
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

      {/* Nutrition Balance */}
      <Grid size={{ xs: 12, md: 6 }}>
        <Card>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              {t('nutritionBalance')}
            </Typography>
            {balanceLoading && <CircularProgress aria-label={t('loading')} size={24} />}
            {balanceError && <Alert severity="error">{t('error')}</Alert>}
            {balanceData && (
              <Box>
                {[
                  { label: t('calories'), current: balanceData.totalCalories, target: balanceData.targetCalories },
                  { label: t('protein'), current: balanceData.totalProtein, target: balanceData.targetProtein },
                  { label: t('carbs'), current: balanceData.totalCarbs, target: balanceData.targetCarbs },
                  { label: t('fat'), current: balanceData.totalFat, target: balanceData.targetFat },
                ].map(({ label, current, target }) => (
                  <Box key={label} sx={{ mt: 1 }}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                      <Typography variant="body2">{label}</Typography>
                      <Typography variant="body2" color="text.secondary">
                        {current} / {target}
                      </Typography>
                    </Box>
                    <LinearProgress
                      variant="determinate"
                      value={Math.min((current / target) * 100, 100)}
                      sx={{ mt: 0.5 }}
                    />
                  </Box>
                ))}
              </Box>
            )}
          </CardContent>
        </Card>
      </Grid>

      {/* Recent Achievements */}
      <Grid size={{ xs: 12, md: 6 }}>
        <Card>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              {t('recentAchievements')}
            </Typography>
            {achievementsLoading && <CircularProgress aria-label={t('loading')} size={24} />}
            {achievementsError && <Alert severity="error">{t('error')}</Alert>}
            {achievements && (
              <Box>
                {achievements.slice(0, 4).map((achievement) => (
                  <Box key={achievement.id} sx={{ display: 'flex', alignItems: 'center', mt: 1, gap: 1 }}>
                    <img
                      src={achievement.iconUrl}
                      alt={achievement.name}
                      width={32}
                      height={32}
                      loading="lazy"
                    />
                    <Box>
                      <Typography variant="body2" fontWeight="bold">
                        {achievement.name}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        {achievement.description}
                      </Typography>
                    </Box>
                  </Box>
                ))}
                {achievements.length === 0 && (
                  <Typography variant="body2" color="text.secondary">
                    {t('noData')}
                  </Typography>
                )}
              </Box>
            )}
          </CardContent>
        </Card>
      </Grid>

      {/* Progress Chart */}
      <Grid size={{ xs: 12 }}>
        {progressLoading && <CircularProgress aria-label={t('loading')} />}
        {progressError && <Alert severity="error">{t('error')}</Alert>}
        {progressData && (
          <ProgressChart
            data={progressData.data}
            defaultPeriod={progressPeriod}
            onPeriodChange={setProgressPeriod}
            title={t('progressChart')}
          />
        )}
      </Grid>
    </Grid>
  );
}
