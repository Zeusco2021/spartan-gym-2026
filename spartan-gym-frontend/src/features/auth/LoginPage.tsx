import { useState } from 'react';
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
import { useAppDispatch } from '@/app/hooks';
import { setCredentials, setMfaPending } from '@/features/auth/authSlice';
import { useLoginMutation } from '@/api/usersApi';
import { socketService } from '@/websocket/socketService';

const loginSchema = z.object({
  email: z
    .string()
    .min(1, 'emailRequired')
    .email('emailInvalid'),
  password: z
    .string()
    .min(1, 'passwordRequired')
    .min(8, 'passwordMinLength'),
});

type LoginFormValues = z.infer<typeof loginSchema>;

export default function LoginPage() {
  const { t } = useTranslation('auth');
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const [login, { isLoading }] = useLoginMutation();
  const [serverError, setServerError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: '', password: '' },
  });

  const onSubmit = async (data: LoginFormValues) => {
    setServerError(null);
    try {
      const result = await login(data).unwrap();

      if (result.mfaRequired) {
        const sessionToken =
          (result as unknown as { sessionToken: string }).sessionToken ?? '';
        dispatch(setMfaPending(sessionToken));
        navigate('/mfa');
        return;
      }

      dispatch(setCredentials({ user: result.user, token: result.token }));
      socketService.connect(result.token, dispatch);
      navigate('/dashboard');
    } catch {
      setServerError(t('loginError'));
    }
  };

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
            {t('loginTitle')}
          </Typography>
          <Typography
            variant="body1"
            color="text.secondary"
            gutterBottom
            align="center"
          >
            {t('loginSubtitle')}
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
              {...register('email')}
              label={t('email')}
              type="email"
              fullWidth
              margin="normal"
              error={!!errors.email}
              helperText={errors.email ? t(errors.email.message!) : undefined}
              autoComplete="email"
              autoFocus
            />
            <TextField
              {...register('password')}
              label={t('password')}
              type="password"
              fullWidth
              margin="normal"
              error={!!errors.password}
              helperText={
                errors.password ? t(errors.password.message!) : undefined
              }
              autoComplete="current-password"
            />
            <Button
              type="submit"
              fullWidth
              variant="contained"
              size="large"
              disabled={isLoading}
              sx={{ mt: 3, mb: 2 }}
            >
              {isLoading ? <CircularProgress size={24} /> : t('login')}
            </Button>
          </Box>

          <Box sx={{ textAlign: 'center', mt: 2 }}>
            <Typography variant="body2">
              {t('noAccount')}{' '}
              <Link component={RouterLink} to="/register">
                {t('registerLink')}
              </Link>
            </Typography>
          </Box>
        </Paper>
      </Box>
    </Container>
  );
}
