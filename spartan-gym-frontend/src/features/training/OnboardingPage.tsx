import { useState, useCallback } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  Stepper,
  Step,
  StepLabel,
  FormControl,
  FormLabel,
  RadioGroup,
  FormControlLabel,
  Radio,
  Checkbox,
  FormGroup,
  Alert,
  CircularProgress,
  LinearProgress,
  Chip,
} from '@mui/material';
import { useTranslation } from 'react-i18next';
import { useSubmitOnboardingMutation } from '@/api/usersApi';
import { useGeneratePlanMutation } from '@/api/aiCoachApi';
import type { OnboardingData, TrainingPlan } from '@/types';

const FITNESS_LEVELS = ['beginner', 'intermediate', 'advanced'] as const;

const GOALS = [
  'weight_loss',
  'muscle_gain',
  'endurance',
  'flexibility',
  'general_fitness',
] as const;

const MEDICAL_CONDITIONS = [
  'back_pain',
  'knee_injury',
  'heart_condition',
  'asthma',
  'diabetes',
  'hypertension',
] as const;

const EQUIPMENT = [
  'dumbbells',
  'barbell',
  'pull_up_bar',
  'resistance_bands',
  'treadmill',
  'stationary_bike',
  'kettlebell',
  'bench',
] as const;

const STEP_KEYS = ['fitnessLevel', 'goals', 'medicalConditions', 'equipment'] as const;

export default function OnboardingPage() {
  const { t } = useTranslation('training');

  const [activeStep, setActiveStep] = useState(0);
  const [fitnessLevel, setFitnessLevel] = useState('');
  const [goals, setGoals] = useState<string[]>([]);
  const [medicalConditions, setMedicalConditions] = useState<string[]>([]);
  const [equipment, setEquipment] = useState<string[]>([]);
  const [generatedPlan, setGeneratedPlan] = useState<TrainingPlan | null>(null);

  const [submitOnboarding, { isLoading: isSubmitting }] = useSubmitOnboardingMutation();
  const [generatePlan, { isLoading: isGenerating, error: generateError }] = useGeneratePlanMutation();

  const steps = STEP_KEYS.map((key) => t(`onboarding.steps.${key}`));
  const isOptionalStep = (step: number) => step === 2 || step === 3;

  const canProceed = useCallback(() => {
    if (activeStep === 0) return fitnessLevel !== '';
    if (activeStep === 1) return goals.length > 0;
    return true; // steps 2 and 3 are optional
  }, [activeStep, fitnessLevel, goals]);

  const handleToggleGoal = (goal: string) => {
    setGoals((prev) =>
      prev.includes(goal) ? prev.filter((g) => g !== goal) : [...prev, goal],
    );
  };

  const handleToggleMedical = (condition: string) => {
    setMedicalConditions((prev) =>
      prev.includes(condition) ? prev.filter((c) => c !== condition) : [...prev, condition],
    );
  };

  const handleToggleEquipment = (item: string) => {
    setEquipment((prev) =>
      prev.includes(item) ? prev.filter((e) => e !== item) : [...prev, item],
    );
  };

  const handleNext = () => {
    if (activeStep < steps.length - 1) {
      setActiveStep((prev) => prev + 1);
    }
  };

  const handleBack = () => {
    if (activeStep > 0) {
      setActiveStep((prev) => prev - 1);
    }
  };

  const handleSkip = () => {
    if (isOptionalStep(activeStep)) {
      handleNext();
    }
  };

  const handleComplete = async () => {
    const data: OnboardingData = {
      fitnessLevel,
      goals,
      medicalConditions: medicalConditions.length > 0 ? medicalConditions : undefined,
      availableEquipment: equipment.length > 0 ? equipment : undefined,
    };

    try {
      await submitOnboarding(data).unwrap();
      const plan = await generatePlan(data).unwrap();
      setGeneratedPlan(plan);
    } catch {
      // errors handled via RTK Query state
    }
  };

  const progress = ((activeStep + 1) / steps.length) * 100;

  if (generatedPlan) {
    return (
      <Box>
        <Typography variant="h4" gutterBottom>
          {t('onboarding.planGenerated')}
        </Typography>
        <Card>
          <CardContent>
            <Typography variant="h5" gutterBottom>
              {generatedPlan.name}
            </Typography>
            {generatedPlan.description && (
              <Typography variant="body1" color="text.secondary" sx={{ mb: 2 }}>
                {generatedPlan.description}
              </Typography>
            )}
            <Chip
              label={t('aiGenerated')}
              color="primary"
              size="small"
              sx={{ mb: 2 }}
            />
            <Typography variant="h6" sx={{ mt: 2, mb: 1 }}>
              {t('routines')} ({generatedPlan.routines.length})
            </Typography>
            {generatedPlan.routines.map((routine) => (
              <Card key={routine.id} variant="outlined" sx={{ mb: 1 }}>
                <CardContent sx={{ py: 1, '&:last-child': { pb: 1 } }}>
                  <Typography variant="subtitle1">{routine.name}</Typography>
                  <Typography variant="body2" color="text.secondary">
                    {routine.exercises.length} {t('exercises')}
                  </Typography>
                </CardContent>
              </Card>
            ))}
          </CardContent>
        </Card>
      </Box>
    );
  }

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        {t('onboarding.title')}
      </Typography>
      <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
        {t('onboarding.subtitle')}
      </Typography>

      <LinearProgress variant="determinate" value={progress} sx={{ mb: 3 }} />

      <Stepper activeStep={activeStep} sx={{ mb: 4 }} alternativeLabel>
        {steps.map((label, index) => (
          <Step key={label}>
            <StepLabel
              optional={
                isOptionalStep(index) ? (
                  <Typography variant="caption">{t('onboarding.optional')}</Typography>
                ) : undefined
              }
            >
              {label}
            </StepLabel>
          </Step>
        ))}
      </Stepper>

      {generateError && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {t('onboarding.generateError')}
        </Alert>
      )}

      <Card>
        <CardContent>
          {activeStep === 0 && (
            <FormControl component="fieldset">
              <FormLabel component="legend">
                {t('onboarding.selectFitnessLevel')}
              </FormLabel>
              <RadioGroup
                value={fitnessLevel}
                onChange={(e) => setFitnessLevel(e.target.value)}
              >
                {FITNESS_LEVELS.map((level) => (
                  <FormControlLabel
                    key={level}
                    value={level}
                    control={<Radio />}
                    label={t(level)}
                  />
                ))}
              </RadioGroup>
            </FormControl>
          )}

          {activeStep === 1 && (
            <FormControl component="fieldset">
              <FormLabel component="legend">
                {t('onboarding.selectGoals')}
              </FormLabel>
              <FormGroup>
                {GOALS.map((goal) => (
                  <FormControlLabel
                    key={goal}
                    control={
                      <Checkbox
                        checked={goals.includes(goal)}
                        onChange={() => handleToggleGoal(goal)}
                      />
                    }
                    label={t(`onboarding.goals.${goal}`)}
                  />
                ))}
              </FormGroup>
            </FormControl>
          )}

          {activeStep === 2 && (
            <FormControl component="fieldset">
              <FormLabel component="legend">
                {t('onboarding.selectMedicalConditions')}
              </FormLabel>
              <FormGroup>
                {MEDICAL_CONDITIONS.map((condition) => (
                  <FormControlLabel
                    key={condition}
                    control={
                      <Checkbox
                        checked={medicalConditions.includes(condition)}
                        onChange={() => handleToggleMedical(condition)}
                      />
                    }
                    label={t(`onboarding.medical.${condition}`)}
                  />
                ))}
              </FormGroup>
            </FormControl>
          )}

          {activeStep === 3 && (
            <FormControl component="fieldset">
              <FormLabel component="legend">
                {t('onboarding.selectEquipment')}
              </FormLabel>
              <FormGroup>
                {EQUIPMENT.map((item) => (
                  <FormControlLabel
                    key={item}
                    control={
                      <Checkbox
                        checked={equipment.includes(item)}
                        onChange={() => handleToggleEquipment(item)}
                      />
                    }
                    label={t(`onboarding.equipment.${item}`)}
                  />
                ))}
              </FormGroup>
            </FormControl>
          )}
        </CardContent>
      </Card>

      <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 3 }}>
        <Button disabled={activeStep === 0} onClick={handleBack}>
          {t('onboarding.back')}
        </Button>
        <Box sx={{ display: 'flex', gap: 1 }}>
          {isOptionalStep(activeStep) && (
            <Button onClick={handleSkip}>{t('onboarding.skip')}</Button>
          )}
          {activeStep === steps.length - 1 ? (
            <Button
              variant="contained"
              onClick={handleComplete}
              disabled={!canProceed() || isSubmitting || isGenerating}
            >
              {isSubmitting || isGenerating ? (
                <CircularProgress size={24} />
              ) : (
                t('onboarding.complete')
              )}
            </Button>
          ) : (
            <Button
              variant="contained"
              onClick={handleNext}
              disabled={!canProceed()}
            >
              {t('onboarding.next')}
            </Button>
          )}
        </Box>
      </Box>
    </Box>
  );
}
