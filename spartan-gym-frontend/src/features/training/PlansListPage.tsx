import { useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  CircularProgress,
  Alert,
  Grid2 as Grid,
  Chip,
  Pagination,
  Tabs,
  Tab,
  Button,
} from '@mui/material';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import { useGetPlansQuery } from '@/api/trainingApi';
import type { TrainingPlan } from '@/types';

const STATUS_FILTERS = ['all', 'active', 'completed', 'paused'] as const;
type StatusFilter = (typeof STATUS_FILTERS)[number];

const statusColorMap: Record<TrainingPlan['status'], 'success' | 'default' | 'warning'> = {
  active: 'success',
  completed: 'default',
  paused: 'warning',
};

export default function PlansListPage() {
  const { t } = useTranslation('training');
  const navigate = useNavigate();

  const [page, setPage] = useState(0);
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('all');

  const {
    data,
    isLoading,
    isError,
  } = useGetPlansQuery({ page, size: 10 }, { refetchOnMountOrArgChange: true });

  const filteredPlans = data?.content.filter(
    (plan) => statusFilter === 'all' || plan.status === statusFilter,
  );

  const handleTabChange = (_: React.SyntheticEvent, newValue: number) => {
    setStatusFilter(STATUS_FILTERS[newValue]);
    setPage(0);
  };

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        {t('plans')}
      </Typography>

      <Tabs
        value={STATUS_FILTERS.indexOf(statusFilter)}
        onChange={handleTabChange}
        sx={{ mb: 3 }}
        aria-label={t('filterByStatus')}
      >
        {STATUS_FILTERS.map((status) => (
          <Tab key={status} label={t(status === 'all' ? 'allPlans' : status)} />
        ))}
      </Tabs>

      {isLoading && (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
          <CircularProgress />
        </Box>
      )}

      {isError && <Alert severity="error">{t('error')}</Alert>}

      {filteredPlans && filteredPlans.length === 0 && (
        <Alert severity="info">{t('noPlans')}</Alert>
      )}

      {filteredPlans && filteredPlans.length > 0 && (
        <Grid container spacing={3}>
          {filteredPlans.map((plan) => (
            <Grid key={plan.id} size={{ xs: 12, md: 6 }}>
              <Card>
                <CardContent>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 1 }}>
                    <Typography variant="h6">{plan.name}</Typography>
                    <Box sx={{ display: 'flex', gap: 1 }}>
                      {plan.aiGenerated && (
                        <Chip label={t('aiGenerated')} size="small" color="primary" variant="outlined" />
                      )}
                      <Chip
                        label={t(plan.status)}
                        size="small"
                        color={statusColorMap[plan.status]}
                      />
                    </Box>
                  </Box>

                  {plan.description && (
                    <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                      {plan.description}
                    </Typography>
                  )}

                  <Typography variant="caption" color="text.secondary">
                    {t('createdAt')}: {new Date(plan.createdAt).toLocaleDateString()}
                  </Typography>

                  <Box sx={{ mt: 2 }}>
                    <Button
                      size="small"
                      variant="outlined"
                      onClick={() => navigate(`/training/plans/${plan.id}`)}
                    >
                      {t('viewPlan')}
                    </Button>
                  </Box>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}

      {data && data.totalPages > 1 && (
        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 3 }}>
          <Pagination
            count={data.totalPages}
            page={page + 1}
            onChange={(_, value) => setPage(value - 1)}
          />
        </Box>
      )}
    </Box>
  );
}
