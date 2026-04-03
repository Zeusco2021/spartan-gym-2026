import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useTranslation } from 'react-i18next';
import {
  Box,
  Button,
  TextField,
  Typography,
  Card,
  CardContent,
  Grid,
  Alert,
  CircularProgress,
} from '@mui/material';
import CloudUploadIcon from '@mui/icons-material/CloudUpload';

const bodyMeasurementsSchema = z.object({
  weight: z.union([z.coerce.number().positive('weightPositive'), z.literal('').transform(() => undefined)]).optional(),
  height: z.union([z.coerce.number().positive('heightPositive'), z.literal('').transform(() => undefined)]).optional(),
  waist: z.union([z.coerce.number().positive('waistPositive'), z.literal('').transform(() => undefined)]).optional(),
  chest: z.union([z.coerce.number().positive('chestPositive'), z.literal('').transform(() => undefined)]).optional(),
  arms: z.union([z.coerce.number().positive('armsPositive'), z.literal('').transform(() => undefined)]).optional(),
  legs: z.union([z.coerce.number().positive('legsPositive'), z.literal('').transform(() => undefined)]).optional(),
});

export type BodyMeasurementsValues = z.infer<typeof bodyMeasurementsSchema>;

interface BodyMeasurementsFormProps {
  onSubmit: (data: BodyMeasurementsValues) => void;
  onSkip: () => void;
  isLoading?: boolean;
  error?: string | null;
}

export default function BodyMeasurementsForm({
  onSubmit,
  onSkip,
  isLoading = false,
  error = null,
}: BodyMeasurementsFormProps) {
  const { t } = useTranslation('auth');

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<BodyMeasurementsValues>({
    resolver: zodResolver(bodyMeasurementsSchema),
  });

  return (
    <Box>
      <Typography variant="h5" gutterBottom>
        {t('bodyMeasurements.title')}
      </Typography>
      <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
        {t('bodyMeasurements.subtitle')}
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} role="alert">
          {error}
        </Alert>
      )}

      <Box component="form" onSubmit={handleSubmit(onSubmit)} noValidate>
        <Card sx={{ mb: 3 }}>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              {t('bodyMeasurements.measurements')}
            </Typography>
            <Grid container spacing={2}>
              <Grid size={{ xs: 12, sm: 6 }}>
                <TextField
                  {...register('weight')}
                  label={t('bodyMeasurements.weight')}
                  type="number"
                  fullWidth
                  error={!!errors.weight}
                  helperText={errors.weight ? t(errors.weight.message!) : undefined}
                  slotProps={{ htmlInput: { min: 0, step: 0.1 } }}
                />
              </Grid>
              <Grid size={{ xs: 12, sm: 6 }}>
                <TextField
                  {...register('height')}
                  label={t('bodyMeasurements.height')}
                  type="number"
                  fullWidth
                  error={!!errors.height}
                  helperText={errors.height ? t(errors.height.message!) : undefined}
                  slotProps={{ htmlInput: { min: 0, step: 0.1 } }}
                />
              </Grid>
              <Grid size={{ xs: 12, sm: 6 }}>
                <TextField
                  {...register('waist')}
                  label={t('bodyMeasurements.waist')}
                  type="number"
                  fullWidth
                  error={!!errors.waist}
                  helperText={errors.waist ? t(errors.waist.message!) : undefined}
                  slotProps={{ htmlInput: { min: 0, step: 0.1 } }}
                />
              </Grid>
              <Grid size={{ xs: 12, sm: 6 }}>
                <TextField
                  {...register('chest')}
                  label={t('bodyMeasurements.chest')}
                  type="number"
                  fullWidth
                  error={!!errors.chest}
                  helperText={errors.chest ? t(errors.chest.message!) : undefined}
                  slotProps={{ htmlInput: { min: 0, step: 0.1 } }}
                />
              </Grid>
              <Grid size={{ xs: 12, sm: 6 }}>
                <TextField
                  {...register('arms')}
                  label={t('bodyMeasurements.arms')}
                  type="number"
                  fullWidth
                  error={!!errors.arms}
                  helperText={errors.arms ? t(errors.arms.message!) : undefined}
                  slotProps={{ htmlInput: { min: 0, step: 0.1 } }}
                />
              </Grid>
              <Grid size={{ xs: 12, sm: 6 }}>
                <TextField
                  {...register('legs')}
                  label={t('bodyMeasurements.legs')}
                  type="number"
                  fullWidth
                  error={!!errors.legs}
                  helperText={errors.legs ? t(errors.legs.message!) : undefined}
                  slotProps={{ htmlInput: { min: 0, step: 0.1 } }}
                />
              </Grid>
            </Grid>
          </CardContent>
        </Card>

        <Card sx={{ mb: 3 }}>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              {t('bodyMeasurements.progressPhotos')}
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
              {t('bodyMeasurements.progressPhotosHint')}
            </Typography>
            <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
              {['front', 'side', 'back'].map((angle) => (
                <Button
                  key={angle}
                  variant="outlined"
                  startIcon={<CloudUploadIcon />}
                  aria-label={t(`bodyMeasurements.upload.${angle}`)}
                  disabled
                  sx={{ textTransform: 'none' }}
                >
                  {t(`bodyMeasurements.upload.${angle}`)}
                </Button>
              ))}
            </Box>
            <Typography variant="caption" color="text.secondary" sx={{ mt: 1, display: 'block' }}>
              {t('bodyMeasurements.photosComingSoon')}
            </Typography>
          </CardContent>
        </Card>

        <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
          <Button onClick={onSkip} aria-label={t('bodyMeasurements.skip')}>
            {t('bodyMeasurements.skip')}
          </Button>
          <Button
            type="submit"
            variant="contained"
            disabled={isLoading}
            aria-label={t('bodyMeasurements.save')}
          >
            {isLoading ? <CircularProgress size={24} /> : t('bodyMeasurements.save')}
          </Button>
        </Box>
      </Box>
    </Box>
  );
}
