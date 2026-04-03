import { useState } from 'react';
import {
  Box,
  Typography,
  Card,
  CardContent,
  CardActions,
  Button,
  CircularProgress,
  Alert,
  Grid2 as Grid,
  Chip,
  LinearProgress,
  ToggleButton,
  ToggleButtonGroup,
} from '@mui/material';
import { useTranslation } from 'react-i18next';
import { useGetChallengesQuery } from '@/api/socialApi';

export default function ChallengesPage() {
  const { t } = useTranslation('social');
  const [filter, setFilter] = useState<'all' | 'weekly' | 'monthly'>('all');

  const { data, isLoading, isError } = useGetChallengesQuery(
    filter === 'all' ? {} : { type: filter, active: true },
  );

  return (
    <Box p={3}>
      <Typography variant="h4" gutterBottom>
        {t('challengesTitle')}
      </Typography>

      <ToggleButtonGroup
        value={filter}
        exclusive
        onChange={(_, v) => v && setFilter(v)}
        sx={{ mb: 3 }}
        aria-label={t('filterChallenges')}
      >
        <ToggleButton value="all" aria-label={t('all')}>
          {t('all')}
        </ToggleButton>
        <ToggleButton value="weekly" aria-label={t('weekly')}>
          {t('weekly')}
        </ToggleButton>
        <ToggleButton value="monthly" aria-label={t('monthly')}>
          {t('monthly')}
        </ToggleButton>
      </ToggleButtonGroup>

      {isLoading && (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
          <CircularProgress aria-label={t('loading')} />
        </Box>
      )}

      {isError && <Alert severity="error">{t('error')}</Alert>}

      {data && data.content.length === 0 && (
        <Alert severity="info">{t('noChallenges')}</Alert>
      )}

      {data && data.content.length > 0 && (
        <Grid container spacing={3}>
          {data.content.map((challenge) => {
            const progress = Math.min(
              100,
              (challenge.participantCount / challenge.targetValue) * 100,
            );
            return (
              <Grid key={challenge.id} size={{ xs: 12, sm: 6, md: 4 }}>
                <Card aria-label={challenge.name}>
                  <CardContent>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                      <Typography variant="h6">{challenge.name}</Typography>
                      <Chip
                        label={t(challenge.type)}
                        size="small"
                        color={challenge.type === 'weekly' ? 'primary' : 'secondary'}
                      />
                    </Box>
                    <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                      {challenge.description}
                    </Typography>
                    <Chip
                      label={t(challenge.metric)}
                      size="small"
                      variant="outlined"
                      sx={{ mb: 1 }}
                    />
                    <Typography variant="body2" sx={{ mb: 0.5 }}>
                      {t('target')}: {challenge.targetValue}
                    </Typography>
                    <Typography variant="body2" sx={{ mb: 1 }}>
                      {t('participants')}: {challenge.participantCount}
                    </Typography>
                    <LinearProgress
                      variant="determinate"
                      value={progress}
                      aria-label={`${t('progress')} ${Math.round(progress)}%`}
                    />
                    <Typography variant="caption" color="text.secondary">
                      {new Date(challenge.startDate).toLocaleDateString()} –{' '}
                      {new Date(challenge.endDate).toLocaleDateString()}
                    </Typography>
                  </CardContent>
                  <CardActions>
                    <Button size="small" aria-label={`${t('joinChallenge')} ${challenge.name}`}>
                      {t('joinChallenge')}
                    </Button>
                  </CardActions>
                </Card>
              </Grid>
            );
          })}
        </Grid>
      )}
    </Box>
  );
}
