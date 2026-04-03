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
import { useRegisterMutation } from '@/api/usersApi';

const registerSchema = z.object({
  name: z.string().min(1, 'nameRequired'),
  email: z
    .string()
    .min(1, 'emailRequired')
    .email('emailInvalid'),
  password: z
    .string()
    .min(1, 'passwordRequired')
    .min(8, 'passwordMinLength'),
  dateOfBirth: z.string().min(1, 'dateOfBirthRequired'),
});

type RegisterFormValues = z.infer<typeof registerSchema>;

export default function RegisterPage() {
  const { t } = useTranslation('auth');
  const navigate = useNavigate();
  const [registerUser, { isLoading }] = useRegisterMutation();
  const [serverError, setServerError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<RegisterFormValues>({
    resolver: zodResolver(registerSchema),
    defaultValues: { name: '', email: '', password: '', dateOfBirth: '' },
  });

  const onSubmit = async (data: RegisterFormValues) => {
    setServerError(null);
    try {
      await registerUser(data).unwrap();
      navigate('/login');
    } catch (err: unknown) {
      const error = err as { status?: number };
      if (error.status === 409) {
        setServerError(t('emailAlreadyRegistered'));
      } else {
        setServerError(t('registerError'));
      }
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
            {t('registerTitle')}
          </Typography>
          <Typography
            variant="body1"
            color="text.secondary"
            gutterBottom
            align="center"
          >
            {t('registerSubtitle')}
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
              {...register('name')}
              label={t('name')}
              fullWidth
              margin="normal"
              error={!!errors.name}
              helperText={errors.name ? t(errors.name.message!) : undefined}
              autoComplete="name"
              autoFocus
            />
            <TextField
              {...register('email')}
              label={t('email')}
              type="email"
              fullWidth
              margin="normal"
              error={!!errors.email}
              helperText={errors.email ? t(errors.email.message!) : undefined}
              autoComplete="email"
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
              autoComplete="new-password"
            />
            <TextField
              {...register('dateOfBirth')}
              label={t('dateOfBirth')}
              type="date"
              fullWidth
              margin="normal"
              error={!!errors.dateOfBirth}
              helperText={
                errors.dateOfBirth ? t(errors.dateOfBirth.message!) : undefined
              }
              slotProps={{ inputLabel: { shrink: true } }}
            />
            <Button
              type="submit"
              fullWidth
              variant="contained"
              size="large"
              disabled={isLoading}
              sx={{ mt: 3, mb: 2 }}
              aria-label={t('register')}
            >
              {isLoading ? <CircularProgress size={24} /> : t('register')}
            </Button>
          </Box>

          <Box sx={{ textAlign: 'center', mt: 2 }}>
            <Typography variant="body2">
              {t('hasAccount')}{' '}
              <Link component={RouterLink} to="/login" aria-label={t('loginLink')}>
                {t('loginLink')}
              </Link>
            </Typography>
          </Box>
        </Paper>
      </Box>
    </Container>
  );
}
