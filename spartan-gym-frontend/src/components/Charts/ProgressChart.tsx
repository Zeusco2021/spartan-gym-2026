import { useState } from 'react';
import {
  Box,
  ToggleButtonGroup,
  ToggleButton,
  Typography,
  Card,
  CardContent,
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
import type { ProgressDataPoint } from '@/types';

type Period = 'day' | 'month' | 'year';

interface ProgressChartProps {
  data: ProgressDataPoint[];
  defaultPeriod?: Period;
  onPeriodChange?: (period: Period) => void;
  title?: string;
}

const METRIC_COLORS: Record<string, string> = {
  volume: '#1976d2',
  duration: '#2e7d32',
  caloriesBurned: '#ed6c02',
};

export default function ProgressChart({
  data,
  defaultPeriod = 'month',
  onPeriodChange,
  title,
}: ProgressChartProps) {
  const { t } = useTranslation('dashboard');
  const [period, setPeriod] = useState<Period>(defaultPeriod);

  const handlePeriodChange = (
    _: React.MouseEvent<HTMLElement>,
    value: Period | null,
  ) => {
    if (value) {
      setPeriod(value);
      onPeriodChange?.(value);
    }
  };

  return (
    <Card>
      <CardContent>
        {title && (
          <Typography variant="h6" gutterBottom>
            {title}
          </Typography>
        )}

        <Box sx={{ display: 'flex', justifyContent: 'flex-end', mb: 2 }}>
          <ToggleButtonGroup
            value={period}
            exclusive
            onChange={handlePeriodChange}
            size="small"
            aria-label={t('selectPeriod')}
          >
            <ToggleButton value="day" aria-label={t('day')}>
              {t('day')}
            </ToggleButton>
            <ToggleButton value="month" aria-label={t('month')}>
              {t('month')}
            </ToggleButton>
            <ToggleButton value="year" aria-label={t('year')}>
              {t('year')}
            </ToggleButton>
          </ToggleButtonGroup>
        </Box>

        {data.length === 0 ? (
          <Typography
            variant="body2"
            color="text.secondary"
            sx={{ textAlign: 'center', py: 4 }}
          >
            {t('noData')}
          </Typography>
        ) : (
          <ResponsiveContainer width="100%" height={300}>
            <LineChart data={data} role="img" aria-label={t('progressChart')}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="date" />
              <YAxis />
              <Tooltip />
              <Legend />
              <Line
                type="monotone"
                dataKey="volume"
                stroke={METRIC_COLORS.volume}
                name={t('volume')}
              />
              <Line
                type="monotone"
                dataKey="duration"
                stroke={METRIC_COLORS.duration}
                name={t('duration')}
              />
              <Line
                type="monotone"
                dataKey="caloriesBurned"
                stroke={METRIC_COLORS.caloriesBurned}
                name={t('calories')}
              />
            </LineChart>
          </ResponsiveContainer>
        )}
      </CardContent>
    </Card>
  );
}
