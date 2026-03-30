import { useEffect, useState } from 'react';
import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import {
  Box,
  Card,
  CardContent,
  Typography,
  TextField,
  Button,
  Avatar,
  Alert,
  CircularProgress,
  Grid2 as Grid,
  Chip,
} from '@mui/material';
import { useTranslation } from 'react-i18next';
import { useGetProfileQuery, useUpdateProfileMutation } from '@/api/usersApi';

const profileSchema = z.object({
  name: z.string().min(1),
  dateOfBirth: z.string().min(1),
  profilePhotoUrl: z.string().url().or(z.literal('')).optional(),
  primaryGoal: z.string().optional(),
  weeklyWorkouts: z.coerce.number().int().min(0).max(14).optional(),
  targetWeight: z.coerce.number().positive().optional().or(z.literal('')),
});

type ProfileFormValues = z.infer<typeof profileSchema>;

export default function ProfileSettingsPage() {
  const { t } = useTranslation('settings');
  const { data: profile, isLoading, isError } = useGetProfileQuery(undefined, {
    refetchOnMountOrArgChange: true,
  });
  const [updateProfile, { isLoading: isUpdating }] = useUpdateProfileMutation();

  const [successMsg, setSuccessMsg] = useState('');
  const [errorMsg, setErrorMsg] = useState('');
  const [conditions, setConditions] = useState<string[]>([]);
  const [conditionInput, setConditionInput] = useState('');

  const {
    control,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<ProfileFormValues>({
    resolver: zodResolver(profileSchema),
    defaultValues: {
      name: '',
      dateOfBirth: '',
      profilePhotoUrl: '',
      primaryGoal: '',
      weeklyWorkouts: 0,
      targetWeight: '',
    },
  });

  useEffect(() => {
    if (profile) {
      reset({
        name: profile.name,
        dateOfBirth: profile.dateOfBirth,
        profilePhotoUrl: profile.profilePhotoUrl ?? '',
        primaryGoal: profile.fitnessGoals?.primaryGoal ?? '',
        weeklyWorkouts: profile.fitnessGoals?.weeklyWorkouts ?? 0,
        targetWeight: profile.fitnessGoals?.targetWeight ?? '',
      });
      setConditions(profile.medicalConditions ?? []);
    }
  }, [profile, reset]);

  const onSubmit = async (values: ProfileFormValues) => {
    setSuccessMsg('');
    setErrorMsg('');
    try {
      const payload: Record<string, unknown> = {
        name: values.name,
        dateOfBirth: values.dateOfBirth,
        profilePhotoUrl: values.profilePhotoUrl || undefined,
        fitnessGoals: {
          primaryGoal: values.primaryGoal || '',
          weeklyWorkouts: values.weeklyWorkouts ?? 0,
          ...(values.targetWeight !== '' && values.targetWeight !== undefined
            ? { targetWeight: Number(values.targetWeight) }
            : {}),
        },
        medicalConditions: conditions,
      };
      await updateProfile(payload).unwrap();
      setSuccessMsg(t('profileUpdated'));
    } catch {
      setErrorMsg(t('updateError'));
    }
  };

  const handleAddCondition = () => {
    const trimmed = conditionInput.trim();
    if (trimmed && !conditions.includes(trimmed)) {
      setConditions((prev) => [...prev, trimmed]);
    }
    setConditionInput('');
  };

  const handleDeleteCondition = (condition: string) => {
    setConditions((prev) => prev.filter((c) => c !== condition));
  };

  if (isLoading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (isError) {
    return <Alert severity="error">{t('updateError')}</Alert>;
  }

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        {t('profile')}
      </Typography>

      {successMsg && (
        <Alert severity="success" sx={{ mb: 2 }}>
          {successMsg}
        </Alert>
      )}
      {errorMsg && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {errorMsg}
        </Alert>
      )}

      <form onSubmit={handleSubmit(onSubmit)}>
        <Grid container spacing={3}>
          {/* Profile Photo */}
          <Grid size={{ xs: 12 }}>
            <Card>
              <CardContent sx={{ display: 'flex', alignItems: 'center', gap: 3 }}>
                <Avatar
                  src={profile?.profilePhotoUrl}
                  sx={{ width: 80, height: 80 }}
                  alt={profile?.name}
                />
                <Box sx={{ flex: 1 }}>
                  <Typography variant="h6" gutterBottom>
                    {t('profilePhoto')}
                  </Typography>
                  <Controller
                    name="profilePhotoUrl"
                    control={control}
                    render={({ field }) => (
                      <TextField
                        {...field}
                        label={t('profilePhotoUrl')}
                        fullWidth
                        size="small"
                        error={!!errors.profilePhotoUrl}
                        helperText={errors.profilePhotoUrl?.message}
                      />
                    )}
                  />
                </Box>
              </CardContent>
            </Card>
          </Grid>

          {/* Personal Data */}
          <Grid size={{ xs: 12, md: 6 }}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  {t('personalData')}
                </Typography>
                <Controller
                  name="name"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label={t('name')}
                      fullWidth
                      sx={{ mb: 2 }}
                      error={!!errors.name}
                      helperText={errors.name?.message}
                    />
                  )}
                />
                <TextField
                  label={t('email')}
                  value={profile?.email ?? ''}
                  fullWidth
                  sx={{ mb: 2 }}
                  disabled
                  slotProps={{ input: { readOnly: true } }}
                />
                <Controller
                  name="dateOfBirth"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label={t('dateOfBirth')}
                      type="date"
                      fullWidth
                      slotProps={{ inputLabel: { shrink: true } }}
                      error={!!errors.dateOfBirth}
                      helperText={errors.dateOfBirth?.message}
                    />
                  )}
                />
              </CardContent>
            </Card>
          </Grid>

          {/* Fitness Goals */}
          <Grid size={{ xs: 12, md: 6 }}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  {t('fitnessGoals')}
                </Typography>
                <Controller
                  name="primaryGoal"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label={t('primaryGoal')}
                      fullWidth
                      sx={{ mb: 2 }}
                    />
                  )}
                />
                <Controller
                  name="weeklyWorkouts"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label={t('weeklyWorkouts')}
                      type="number"
                      fullWidth
                      sx={{ mb: 2 }}
                      error={!!errors.weeklyWorkouts}
                      helperText={errors.weeklyWorkouts?.message}
                    />
                  )}
                />
                <Controller
                  name="targetWeight"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label={t('targetWeight')}
                      type="number"
                      fullWidth
                      error={!!errors.targetWeight}
                      helperText={errors.targetWeight?.message}
                    />
                  )}
                />
              </CardContent>
            </Card>
          </Grid>

          {/* Medical Conditions */}
          <Grid size={{ xs: 12 }}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  {t('medicalConditions')}
                </Typography>
                <Box sx={{ display: 'flex', gap: 1, mb: 2 }}>
                  <TextField
                    value={conditionInput}
                    onChange={(e) => setConditionInput(e.target.value)}
                    label={t('addCondition')}
                    size="small"
                    fullWidth
                    onKeyDown={(e) => {
                      if (e.key === 'Enter') {
                        e.preventDefault();
                        handleAddCondition();
                      }
                    }}
                  />
                  <Button variant="outlined" onClick={handleAddCondition}>
                    {t('add')}
                  </Button>
                </Box>
                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                  {conditions.map((condition) => (
                    <Chip
                      key={condition}
                      label={condition}
                      onDelete={() => handleDeleteCondition(condition)}
                    />
                  ))}
                </Box>
              </CardContent>
            </Card>
          </Grid>

          {/* Submit */}
          <Grid size={{ xs: 12 }}>
            <Button
              type="submit"
              variant="contained"
              size="large"
              disabled={isUpdating}
              startIcon={isUpdating ? <CircularProgress size={20} /> : undefined}
            >
              {t('save')}
            </Button>
          </Grid>
        </Grid>
      </form>
    </Box>
  );
}
