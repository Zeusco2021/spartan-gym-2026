import { useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  CircularProgress,
  Alert,
  ToggleButtonGroup,
  ToggleButton,
  TextField,
  Button,
  LinearProgress,
} from '@mui/material';
import {
  ResponsiveContainer,
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
} from 'recharts';
import { useTranslation } from 'react-i18next';
import { useGetProgressQuery } from '@/api/workoutsApi';

type Period = 'day' | 'week' | 'month' | 'year' | 'custom';

const WEEKLY_GOALS = { volume: 10000, duration: 300, caloriesBurned: 2000 };
const MONTHLY_GOALS = { volume: 40000, duration: 1200, caloriesBurned: 8000 };

export default function ProgressPage() {
  const { t } = useTranslation('training');

  const [period, setPeriod] = useState<Period>('week');
  const [from, setFrom] = useState('');
  const [to, setTo] = useState('');
  const [customApplied, setCustomApplied] = useState(false);

  const queryParams =
    period === 'custom' && customApplied
      ? { period, from, to }
      : { period };

  const { data, isLoading, isError } = useGetProgressQuery(queryParams, {
    skip: period === 'custom' && !customApplied,
  });

  const handlePeriodChange = (_: React.MouseEvent<HTMLElement>, value: Period | null) => {
    if (value) {
      setPeriod(value);
      setCustomApplied(false);
    }
  };

  const handleApplyCustom = () => {
    if (from && to) setCustomApplied(true);
  };

  const totals = data?.data.reduce(
    (acc, pt) => ({
      volume: acc.volume + pt.volume,
      duration: acc.duration + pt.duration,
      caloriesBurned: acc.caloriesBurned + pt.caloriesBurned,
    }),
    { volume: 0, duration: 0, caloriesBurned: 0 },
  ) ?? { volume: 0, duration: 0, caloriesBurned: 0 };

  const goals = period === 'month' || period === 'year' ? MONTHLY_GOALS : WEEKLY_GOALS;

  const goalItems = [
    { label: t('volume'), current: totals.volume, target: goals.volume, color: '#1976d2' },
    { label: t('duration'), current: totals.duration, target: goals.duration, color: '#2e7d32' },
    { label: t('calories'), current: totals.caloriesBurned, target: goals.caloriesBurned, color: '#ed6c02' },
  ];

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        {t('progressTitle')}
      </Typography>

      <Box sx={{ mb: 3 }}>
        <ToggleButtonGroup
          value={period}
          exclusive
          onChange={handlePeriodChange}
          size="small"
          aria-label={t('progressTitle')}
        >
          <ToggleButton value="day">{t('day')}</ToggleButton>
          <ToggleButton value="week">{t('week')}</ToggleButton>
          <ToggleButton value="month">{t('month')}</ToggleButton>
          <ToggleButton value="year">{t('year')}</ToggleButton>
          <ToggleButton value="custom">{t('customRange')}</ToggleButton>
        </ToggleButtonGroup>
      </Box>

      {period === 'custom' && (
        <Box sx={{ display: 'flex', gap: 2, mb: 3, alignItems: 'center', flexWrap: 'wrap' }}>
          <TextField
            label={t('from')}
            type="date"
            value={from}
            onChange={(e) => { setFrom(e.target.value); setCustomApplied(false); }}
            slotProps={{ inputLabel: { shrink: true } }}
            size="small"
          />
          <TextField
            label={t('to')}
            type="date"
            value={to}
            onChange={(e) => { setTo(e.target.value); setCustomApplied(false); }}
            slotProps={{ inputLabel: { shrink: true } }}
            size="small"
          />
          <Button variant="contained" size="small" onClick={handleApplyCustom} disabled={!from || !to}>
            {t('applyFilter')}
          </Button>
        </Box>
      )}

      {isLoading && (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
          <CircularProgress />
        </Box>
      )}

      {isError && <Alert severity="error">{t('error')}</Alert>}

      {data && data.data.length === 0 && (
        <Alert severity="info">{t('noProgressData')}</Alert>
      )}

      {data && data.data.length > 0 && (
        <>
          {/* Volume Chart */}
          <Card sx={{ mb: 3 }}>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                {t('volume')}
              </Typography>
              <ResponsiveContainer width="100%" height={300}>
                <LineChart data={data.data}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="date" />
                  <YAxis />
                  <Tooltip />
                  <Legend />
                  <Line type="monotone" dataKey="volume" stroke="#1976d2" name={t('volume')} />
                </LineChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>

          {/* Duration Chart */}
          <Card sx={{ mb: 3 }}>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                {t('duration')}
              </Typography>
              <ResponsiveContainer width="100%" height={300}>
                <LineChart data={data.data}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="date" />
                  <YAxis />
                  <Tooltip />
                  <Legend />
                  <Line type="monotone" dataKey="duration" stroke="#2e7d32" name={t('duration')} />
                </LineChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>

          {/* Calories Chart */}
          <Card sx={{ mb: 3 }}>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                {t('calories')}
              </Typography>
              <ResponsiveContainer width="100%" height={300}>
                <LineChart data={data.data}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="date" />
                  <YAxis />
                  <Tooltip />
                  <Legend />
                  <Line type="monotone" dataKey="caloriesBurned" stroke="#ed6c02" name={t('calories')} />
                </LineChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>

          {/* Goal Comparisons */}
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                {period === 'month' || period === 'year' ? t('monthlyGoal') : t('weeklyGoal')}
              </Typography>
              {goalItems.map(({ label, current, target, color }) => {
                const pct = Math.min((current / target) * 100, 100);
                return (
                  <Box key={label} sx={{ mb: 2 }}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                      <Typography variant="body2">{label}</Typography>
                      <Typography variant="body2" color="text.secondary">
                        {current} / {target} {pct >= 100 ? `✓ ${t('goalAchieved')}` : `(${Math.round(pct)}%)`}
                      </Typography>
                    </Box>
                    <LinearProgress
                      variant="determinate"
                      value={pct}
                      sx={{ mt: 0.5, '& .MuiLinearProgress-bar': { backgroundColor: color } }}
                    />
                  </Box>
                );
              })}
            </CardContent>
          </Card>
        </>
      )}
    </Box>
  );
}
