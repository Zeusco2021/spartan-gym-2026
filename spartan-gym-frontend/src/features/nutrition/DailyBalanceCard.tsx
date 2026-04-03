import {
  Card,
  CardContent,
  Typography,
  Box,
  LinearProgress,
  Chip,
  CircularProgress,
  Alert,
} from '@mui/material';
import { useTranslation } from 'react-i18next';
import type { DailyBalance, NutritionPlan } from '@/types';

const MACRO_COLORS = { calories: '#e91e63', protein: '#4caf50', carbs: '#2196f3', fat: '#ff9800' };

interface DailyBalanceCardProps {
  balance: DailyBalance | undefined;
  plan?: NutritionPlan | undefined;
  isLoading?: boolean;
  isError?: boolean;
}

export default function DailyBalanceCard({ balance, plan, isLoading, isError }: DailyBalanceCardProps) {
  const { t } = useTranslation('nutrition');

  if (isLoading) {
    return (
      <Card>
        <CardContent>
          <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
            <CircularProgress aria-label={t('loading')} />
          </Box>
        </CardContent>
      </Card>
    );
  }

  if (isError) {
    return (
      <Card>
        <CardContent>
          <Alert severity="error">{t('error')}</Alert>
        </CardContent>
      </Card>
    );
  }

  if (!balance) return null;

  const macroRows = [
    {
      label: t('calories'),
      consumed: balance.totalCalories,
      target: balance.targetCalories,
      unit: t('kcal'),
      color: MACRO_COLORS.calories,
      key: 'calories',
    },
    {
      label: t('protein'),
      consumed: balance.totalProtein,
      target: balance.targetProtein,
      unit: t('grams'),
      color: MACRO_COLORS.protein,
      key: 'protein',
    },
    {
      label: t('carbs'),
      consumed: balance.totalCarbs,
      target: balance.targetCarbs,
      unit: t('grams'),
      color: MACRO_COLORS.carbs,
      key: 'carbs',
    },
    {
      label: t('fat'),
      consumed: balance.totalFat,
      target: balance.targetFat,
      unit: t('grams'),
      color: MACRO_COLORS.fat,
      key: 'fat',
    },
  ];

  return (
    <Card aria-label={t('dailyBalance')}>
      <CardContent>
        <Typography variant="h6" gutterBottom>
          {t('dailyBalance')}
        </Typography>
        {plan && (
          <Chip
            label={t(plan.goal)}
            size="small"
            color="primary"
            variant="outlined"
            sx={{ mb: 2 }}
          />
        )}
        {macroRows.map(({ label, consumed, target, unit, color, key }) => {
          const pct = target > 0 ? Math.min((consumed / target) * 100, 100) : 0;
          const remaining = Math.max(target - consumed, 0);
          return (
            <Box key={key} sx={{ mb: 2 }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                <Typography variant="body2">{label}</Typography>
                <Typography variant="body2" color="text.secondary">
                  {consumed} / {target} {unit}
                </Typography>
              </Box>
              <LinearProgress
                variant="determinate"
                value={pct}
                aria-label={`${label} ${Math.round(pct)}%`}
                sx={{ mt: 0.5, '& .MuiLinearProgress-bar': { backgroundColor: color } }}
              />
              <Typography variant="caption" color="text.secondary">
                {t('remaining')}: {remaining} {unit}
              </Typography>
            </Box>
          );
        })}
      </CardContent>
    </Card>
  );
}
