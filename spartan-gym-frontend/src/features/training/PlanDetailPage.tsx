import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import {
  Box,
  Typography,
  Button,
  CircularProgress,
  Alert,
  Chip,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  IconButton,
  List,
  ListItem,
  ListItemText,
  Collapse,
} from '@mui/material';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import FitnessCenterIcon from '@mui/icons-material/FitnessCenter';
import { useGetPlanByIdQuery } from '@/api/trainingApi';
import { useGetAlternativesQuery, useGetWarmupQuery } from '@/api/aiCoachApi';
import VideoPlayer from '@/components/VideoPlayer/VideoPlayer';
import type { RoutineExercise } from '@/types';

const statusColorMap: Record<string, 'success' | 'default' | 'warning'> = {
  active: 'success',
  completed: 'default',
  paused: 'warning',
};

const DAYS_OF_WEEK = [
  'sunday',
  'monday',
  'tuesday',
  'wednesday',
  'thursday',
  'friday',
  'saturday',
];

function AlternativesButton({ exercise }: { exercise: RoutineExercise }) {
  const { t } = useTranslation('training');
  const [open, setOpen] = useState(false);

  const { data: alternatives, isLoading, isError } = useGetAlternativesQuery(
    { exerciseId: exercise.exercise.id },
    { skip: !open },
  );

  return (
    <>
      <Button
        size="small"
        variant="outlined"
        startIcon={<FitnessCenterIcon />}
        onClick={() => setOpen(true)}
      >
        {t('showAlternatives')}
      </Button>
      <Dialog open={open} onClose={() => setOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{t('alternativeExercises')}</DialogTitle>
        <DialogContent>
          {isLoading && (
            <Box sx={{ display: 'flex', justifyContent: 'center', py: 2 }}>
              <CircularProgress size={24} />
            </Box>
          )}
          {isError && <Alert severity="error">{t('error')}</Alert>}
          {alternatives && alternatives.length === 0 && (
            <Typography color="text.secondary">{t('noAlternatives')}</Typography>
          )}
          {alternatives && alternatives.length > 0 && (
            <List>
              {alternatives.map((alt) => (
                <ListItem key={alt.id}>
                  <ListItemText
                    primary={alt.name}
                    secondary={`${t('muscleGroups')}: ${alt.muscleGroups.join(', ')} · ${t('difficulty')}: ${t(alt.difficulty)}`}
                  />
                </ListItem>
              ))}
            </List>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}>{t('close')}</Button>
        </DialogActions>
      </Dialog>
    </>
  );
}

function WarmupSection({ routineId }: { routineId: string }) {
  const { t } = useTranslation('training');
  const [expanded, setExpanded] = useState(false);

  const { data: warmupExercises, isLoading, isError } = useGetWarmupQuery(
    { routineId },
    { skip: !expanded },
  );

  return (
    <Box sx={{ mb: 2 }}>
      <Button
        size="small"
        variant="text"
        onClick={() => setExpanded((prev) => !prev)}
        endIcon={<ExpandMoreIcon sx={{ transform: expanded ? 'rotate(180deg)' : 'none', transition: 'transform 0.2s' }} />}
      >
        {t('showWarmup')}
      </Button>
      <Collapse in={expanded}>
        <Box sx={{ pl: 2, pt: 1 }}>
          {isLoading && <CircularProgress size={20} />}
          {isError && <Alert severity="error" sx={{ mt: 1 }}>{t('error')}</Alert>}
          {warmupExercises && warmupExercises.length === 0 && (
            <Typography variant="body2" color="text.secondary">{t('noWarmup')}</Typography>
          )}
          {warmupExercises && warmupExercises.length > 0 && (
            <List dense>
              {warmupExercises.map((ex) => (
                <ListItem key={ex.id}>
                  <ListItemText
                    primary={ex.name}
                    secondary={`${t('muscleGroups')}: ${ex.muscleGroups.join(', ')}`}
                  />
                </ListItem>
              ))}
            </List>
          )}
        </Box>
      </Collapse>
    </Box>
  );
}

export default function PlanDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { t } = useTranslation('training');

  const { data: plan, isLoading, isError } = useGetPlanByIdQuery(id!, { skip: !id });

  if (isLoading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
        <CircularProgress />
        <Typography sx={{ ml: 2 }}>{t('loadingPlan')}</Typography>
      </Box>
    );
  }

  if (isError || !plan) {
    return (
      <Box>
        <Button startIcon={<ArrowBackIcon />} onClick={() => navigate('/training/plans')}>
          {t('backToPlans')}
        </Button>
        <Alert severity="error" sx={{ mt: 2 }}>{t('planNotFound')}</Alert>
      </Box>
    );
  }

  return (
    <Box>
      <IconButton onClick={() => navigate('/training/plans')} sx={{ mb: 1 }} aria-label={t('backToPlans')}>
        <ArrowBackIcon />
      </IconButton>

      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
        <Typography variant="h4">{plan.name}</Typography>
        {plan.aiGenerated && (
          <Chip label={t('aiGenerated')} size="small" color="primary" variant="outlined" />
        )}
        <Chip label={t(plan.status)} size="small" color={statusColorMap[plan.status] ?? 'default'} />
      </Box>

      {plan.description && (
        <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
          {plan.description}
        </Typography>
      )}

      <Typography variant="h5" sx={{ mb: 2 }}>{t('routines')}</Typography>

      {plan.routines.map((routine) => (
        <Accordion key={routine.id} defaultExpanded={plan.routines.length === 1}>
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <Typography variant="h6">{routine.name}</Typography>
              {routine.dayOfWeek != null && (
                <Chip label={t(`dayOfWeek.${DAYS_OF_WEEK[routine.dayOfWeek]}`)} size="small" variant="outlined" />
              )}
            </Box>
          </AccordionSummary>
          <AccordionDetails>
            <WarmupSection routineId={routine.id} />

            {routine.exercises
              .slice()
              .sort((a, b) => a.sortOrder - b.sortOrder)
              .map((re) => (
                <Box
                  key={re.id}
                  sx={{ mb: 3, p: 2, border: 1, borderColor: 'divider', borderRadius: 1 }}
                >
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 1 }}>
                    <Typography variant="subtitle1" fontWeight="bold">
                      {re.exercise.name}
                    </Typography>
                    <Chip label={t(re.exercise.difficulty)} size="small" />
                  </Box>

                  <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', mb: 1 }}>
                    <Typography variant="body2">
                      {t('sets')}: {re.sets}
                    </Typography>
                    <Typography variant="body2">
                      {t('reps')}: {re.reps}
                    </Typography>
                    <Typography variant="body2">
                      {t('rest')}: {re.restSeconds}s
                    </Typography>
                  </Box>

                  {re.exercise.muscleGroups.length > 0 && (
                    <Box sx={{ display: 'flex', gap: 0.5, flexWrap: 'wrap', mb: 1 }}>
                      <Typography variant="body2" color="text.secondary" sx={{ mr: 0.5 }}>
                        {t('muscleGroups')}:
                      </Typography>
                      {re.exercise.muscleGroups.map((mg) => (
                        <Chip key={mg} label={mg} size="small" variant="outlined" />
                      ))}
                    </Box>
                  )}

                  {re.exercise.videoUrl && (
                    <Box sx={{ my: 2 }}>
                      <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                        {t('videoTutorial')}
                      </Typography>
                      <VideoPlayer src={re.exercise.videoUrl} />
                    </Box>
                  )}

                  <AlternativesButton exercise={re} />
                </Box>
              ))}
          </AccordionDetails>
        </Accordion>
      ))}
    </Box>
  );
}
