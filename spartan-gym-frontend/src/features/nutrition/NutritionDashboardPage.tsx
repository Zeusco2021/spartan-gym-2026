import {
  Box,
  Card,
  CardContent,
  Typography,
  CircularProgress,
  Alert,
  Grid2 as Grid,
} from '@mui/material';
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip, Legend } from 'recharts';
import { useTranslation } from 'react-i18next';
import { useGetDailyBalanceQuery, useGetNutritionPlanQuery } from '@/api/nutritionApi';
import DailyBalanceCard from './DailyBalanceCard';

const MACRO_COLORS = { protein: '#4caf50', carbs: '#2196f3', fat: '#ff9800' };

export default function NutritionDashboardPage() {
  const { t } = useTranslation('nutrition');

  const { data: balance, isLoading: balanceLoading, isError: balanceError } =
    useGetDailyBalanceQuery({}, { refetchOnMountOrArgChange: true });
  const { data: plan } = useGetNutritionPlanQuery();

  const isLoading = balanceLoading;
  const isError = balanceError;

  const pieData = balance
    ? [
        { name: t('protein'), value: balance.totalProtein, color: MACRO_COLORS.protein },
        { name: t('carbs'), value: balance.totalCarbs, color: MACRO_COLORS.carbs },
        { name: t('fat'), value: balance.totalFat, color: MACRO_COLORS.fat },
      ]
    : [];

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        {t('nutritionDashboard')}
      </Typography>

      {isLoading && (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
          <CircularProgress aria-label={t('loading')} />
        </Box>
      )}

      {isError && <Alert severity="error">{t('error')}</Alert>}

      {balance && (
        <Grid container spacing={3}>
          {/* Macro Pie Chart */}
          <Grid size={{ xs: 12, md: 6 }}>
            <Card aria-label={t('macroBreakdown')}>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  {t('macroBreakdown')}
                </Typography>
                {balance.totalProtein + balance.totalCarbs + balance.totalFat === 0 ? (
                  <Alert severity="info">{t('noMeals')}</Alert>
                ) : (
                  <ResponsiveContainer width="100%" height={300}>
                    <PieChart>
                      <Pie
                        data={pieData}
                        cx="50%"
                        cy="50%"
                        innerRadius={60}
                        outerRadius={100}
                        dataKey="value"
                        label={({ name, value }) => `${name}: ${value}${t('grams')}`}
                      >
                        {pieData.map((entry) => (
                          <Cell key={entry.name} fill={entry.color} />
                        ))}
                      </Pie>
                      <Tooltip formatter={(value: number) => `${value}${t('grams')}`} />
                      <Legend />
                    </PieChart>
                  </ResponsiveContainer>
                )}
              </CardContent>
            </Card>
          </Grid>

          {/* Daily Balance Summary */}
          <Grid size={{ xs: 12, md: 6 }}>
            <DailyBalanceCard balance={balance} plan={plan} />
          </Grid>
        </Grid>
      )}
    </Box>
  );
}
