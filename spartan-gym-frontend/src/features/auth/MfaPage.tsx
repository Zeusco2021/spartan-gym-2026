import { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useTranslation } from 'react-i18next';
import { useNavigate, Link as RouterLink } from 'react-router-dom';
import {
  Box,
  Button,
  TextField,
  Typography,
  Alert,
  CircularProgress,
  Link,
  Container,
  Paper,
} from '@mui/material';
import { useAppDispatch, useAppSelector } from '@/app/hooks';
import { setCredentials } from '@/features/auth/authSlice';
import { useVerifyMfaMutation } from '@/api/usersApi';
import { socketService } from '@/websocket/socketService';

const mfaSchema = z.object({
  code: z
    .string()
    .min(1, 'mfaCodeRequired')
    .regex(/^\d{6}$/, 'mfaCodeInvalid'),
});

type MfaFormValues = z.infer<typeof mfaSchema>;

export default function MfaPage() {
  const { t } = useTranslation('auth');
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const [verifyMfa, { isLoading }] = useVerifyMfaMutation();
  const [serverError, setServerError] = useState<string | null>(null);

  const mfaPending = useAppSelector((state) => state.auth.mfaPending);
  const mfaSessionToken = useAppSelector((state) => state.auth.mfaSessionToken);

  useEffect(() => {
    if (!mfaPending) {
      navigate('/login');
    }
  }, [mfaPending, navigate]);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<MfaFormValues>({
    resolver: zodResolver(mfaSchema),
    defaultValues: { code: '' },
  });

  const onSubmit = async (data: MfaFormValues) => {
    setServerError(null);
    try {
      const result = await verifyMfa({
        code: data.code,
        sessionToken: mfaSessionToken ?? '',
      }).unwrap();

      dispatch(setCredentials({ user: result.user, token: result.token }));
      socketService.connect(result.token, dispatch);
      navigate('/dashboard');
    } catch {
      setServerError(t('mfaError'));
    }
  };

  if (!mfaPending) {
    return null;
  }

  return (
    <Container maxWidth="sm">
      <Box
        sx={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          minHeight: '100vh',
        }}
      >
        <Paper elevation={3} sx={{ p: 4, width: '100%' }}>
          <Typography variant="h4" component="h1" gutterBottom align="center">
            {t('mfaTitle')}
          </Typography>
          <Typography
            variant="body1"
            color="text.secondary"
            gutterBottom
            align="center"
          >
            {t('mfaSubtitle')}
          </Typography>

          {serverError && (
            <Alert severity="error" sx={{ mt: 2 }} role="alert">
              {serverError}
            </Alert>
          )}

          <Box
            component="form"
            onSubmit={handleSubmit(onSubmit)}
            noValidate
            sx={{ mt: 3 }}
          >
            <TextField
              {...register('code')}
              label={t('mfaCode')}
              fullWidth
              margin="normal"
              error={!!errors.code}
              helperText={errors.code ? t(errors.code.message!) : undefined}
              autoComplete="one-time-code"
              autoFocus
              inputProps={{ maxLength: 6, inputMode: 'numeric' }}
            />
            <Button
              type="submit"
              fullWidth
              variant="contained"
              size="large"
              disabled={isLoading}
              sx={{ mt: 3, mb: 2 }}
              aria-label={t('mfaVerify')}
            >
              {isLoading ? <CircularProgress size={24} /> : t('mfaVerify')}
            </Button>
          </Box>

          <Box sx={{ textAlign: 'center', mt: 2 }}>
            <Typography variant="body2">
              <Link component={RouterLink} to="/login" aria-label={t('mfaBackToLogin')}>
                {t('mfaBackToLogin')}
              </Link>
            </Typography>
          </Box>
        </Paper>
      </Box>
    </Container>
  );
}
