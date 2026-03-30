import { useState, useEffect, useCallback } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  TextField,
  Alert,
  CircularProgress,
  LinearProgress,
} from '@mui/material';
import { useTranslation } from 'react-i18next';
import {
  useStartWorkoutMutation,
  useAddSetMutation,
  useCompleteWorkoutMutation,
} from '@/api/workoutsApi';
import { useCheckOvertrainingQuery } from '@/api/aiCoachApi';
import type { WorkoutSession } from '@/types';

interface RestTimerProps {
  seconds: number;
  onComplete: () => void;
}

function RestTimer({ seconds, onComplete }: RestTimerProps) {
  const { t } = useTranslation('training');
  const [remaining, setRemaining] = useState(seconds);

  useEffect(() => {
    setRemaining(seconds);
  }, [seconds]);

  useEffect(() => {
    if (remaining <= 0) {
      onComplete();
      return;
    }
    const timer = setInterval(() => {
      setRemaining((prev) => {
        if (prev <= 1) {
          clearInterval(timer);
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
    return () => clearInterval(timer);
  }, [remaining, onComplete]);

  const progress = seconds > 0 ? ((seconds - remaining) / seconds) * 100 : 100;

  return (
    <Box sx={{ mt: 2 }}>
      <Typography variant="body2" color="text.secondary">
        {t('restTimer')}: {remaining}s
      </Typography>
      <LinearProgress variant="determinate" value={progress} sx={{ mt: 1 }} />
    </Box>
  );
}

interface LoggedSet {
  exerciseId: string;
  weight: number;
  reps: number;
  restSeconds: number;
}

type PageState = 'not_started' | 'active' | 'completed';

export default function WorkoutLivePage() {
  const { t } = useTranslation('training');

  const [startWorkout, { isLoading: isStarting }] = useStartWorkoutMutation();
  const [addSet] = useAddSetMutation();
  const [completeWorkout, { isLoading: isCompleting }] = useCompleteWorkoutMutation();

  const { data: overtrainingData } = useCheckOvertrainingQuery();

  const [pageState, setPageState] = useState<PageState>('not_started');
  const [session, setSession] = useState<WorkoutSession | null>(null);
  const [currentExerciseIndex, setCurrentExerciseIndex] = useState(0);
  const [loggedSets, setLoggedSets] = useState<LoggedSet[]>([]);
  const [showRestTimer, setShowRestTimer] = useState(false);
  const restSeconds = 60;

  const [weight, setWeight] = useState('');
  const [reps, setReps] = useState('');
  const [setError, setSetError] = useState<string | null>(null);
  const [isLoggingSet, setIsLoggingSet] = useState(false);

  const [completedSession, setCompletedSession] = useState<WorkoutSession | null>(null);

  const currentExerciseName = `Exercise ${currentExerciseIndex + 1}`;
  const nextExerciseName = `Exercise ${currentExerciseIndex + 2}`;
  const completedSetsCount = loggedSets.length;

  const handleStartWorkout = async () => {
    try {
      const result = await startWorkout({}).unwrap();
      setSession(result);
      setPageState('active');
    } catch {
      // start error handled by RTK Query
    }
  };

  const handleLogSet = async () => {
    if (!session) return;
    const weightVal = parseFloat(weight);
    const repsVal = parseInt(reps, 10);
    if (isNaN(weightVal) || isNaN(repsVal)) return;

    setIsLoggingSet(true);
    setSetError(null);

    try {
      await addSet({
        sessionId: session.id,
        set: {
          exerciseId: `exercise-${currentExerciseIndex}`,
          weight: weightVal,
          reps: repsVal,
          restSeconds,
        },
      }).unwrap();

      setLoggedSets((prev) => [
        ...prev,
        {
          exerciseId: `exercise-${currentExerciseIndex}`,
          weight: weightVal,
          reps: repsVal,
          restSeconds,
        },
      ]);
      setWeight('');
      setReps('');
      setShowRestTimer(true);
    } catch {
      setSetError(t('setLogError'));
    } finally {
      setIsLoggingSet(false);
    }
  };

  const handleRetrySet = () => {
    handleLogSet();
  };

  const handleRestComplete = useCallback(() => {
    setShowRestTimer(false);
  }, []);

  const handleFinishWorkout = async () => {
    if (!session) return;
    try {
      const result = await completeWorkout(session.id).unwrap();
      setCompletedSession(result);
      setPageState('completed');
    } catch {
      // complete error handled by RTK Query
    }
  };

  const totalVolume = loggedSets.reduce((sum, s) => sum + s.weight * s.reps, 0);

  if (pageState === 'not_started') {
    return (
      <Box>
        <Typography variant="h4" gutterBottom>
          {t('workout')}
        </Typography>
        {overtrainingData?.isOvertraining && (
          <Alert severity="warning" sx={{ mb: 2 }}>
            <Typography variant="subtitle2" gutterBottom>
              {t('overtrainingWarning')}
            </Typography>
            <Typography variant="body2">
              {t('overtrainingMessage', {
                days: overtrainingData.suggestedRestDays,
                recommendation: overtrainingData.recommendation,
              })}
            </Typography>
          </Alert>
        )}
        <Card>
          <CardContent>
            <Typography variant="body1" sx={{ mb: 2 }}>
              {t('noActiveWorkout')}
            </Typography>
            <Button
              variant="contained"
              onClick={handleStartWorkout}
              disabled={isStarting}
            >
              {isStarting ? <CircularProgress size={24} /> : t('startWorkoutSession')}
            </Button>
          </CardContent>
        </Card>
      </Box>
    );
  }

  if (pageState === 'completed') {
    const duration = completedSession?.totalDuration ?? 0;
    const calories = completedSession?.caloriesBurned ?? 0;

    return (
      <Box>
        <Typography variant="h4" gutterBottom>
          {t('workoutCompleted')}
        </Typography>
        <Card>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              {t('workoutSummary')}
            </Typography>
            <Typography variant="body1">
              {t('totalDuration')}: {duration} {t('min')}
            </Typography>
            <Typography variant="body1">
              {t('totalVolume')}: {totalVolume} {t('kg')}
            </Typography>
            <Typography variant="body1">
              {t('caloriesBurned')}: {calories} {t('kcal')}
            </Typography>
            <Button
              variant="contained"
              sx={{ mt: 2 }}
              onClick={() => {
                setPageState('not_started');
                setSession(null);
                setLoggedSets([]);
                setCurrentExerciseIndex(0);
                setCompletedSession(null);
              }}
            >
              {t('startNew')}
            </Button>
          </CardContent>
        </Card>
      </Box>
    );
  }

  // Active workout state
  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        {t('workout')}
      </Typography>

      <Card sx={{ mb: 2 }}>
        <CardContent>
          <Typography variant="h6">
            {t('currentExercise')}: {currentExerciseName}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            {t('completedSets')}: {completedSetsCount}
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
            {t('nextExercise')}: {nextExerciseName}
          </Typography>

          {showRestTimer && (
            <RestTimer seconds={restSeconds} onComplete={handleRestComplete} />
          )}
        </CardContent>
      </Card>

      <Card sx={{ mb: 2 }}>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            {t('logSet')}
          </Typography>

          {setError && (
            <Alert
              severity="error"
              sx={{ mb: 2 }}
              action={
                <Button color="inherit" size="small" onClick={handleRetrySet}>
                  {t('retrySet')}
                </Button>
              }
            >
              {setError}
            </Alert>
          )}

          <Box sx={{ display: 'flex', gap: 2, mb: 2 }}>
            <TextField
              label={t('weight')}
              type="number"
              value={weight}
              onChange={(e) => setWeight(e.target.value)}
              size="small"
              slotProps={{ htmlInput: { min: 0 } }}
            />
            <TextField
              label={t('repsCount')}
              type="number"
              value={reps}
              onChange={(e) => setReps(e.target.value)}
              size="small"
              slotProps={{ htmlInput: { min: 0 } }}
            />
          </Box>

          <Button
            variant="contained"
            onClick={handleLogSet}
            disabled={isLoggingSet || !weight || !reps}
          >
            {isLoggingSet ? <CircularProgress size={24} /> : t('logSet')}
          </Button>
        </CardContent>
      </Card>

      <Button
        variant="outlined"
        color="error"
        onClick={handleFinishWorkout}
        disabled={isCompleting}
      >
        {isCompleting ? <CircularProgress size={24} /> : t('finishWorkout')}
      </Button>
    </Box>
  );
}
