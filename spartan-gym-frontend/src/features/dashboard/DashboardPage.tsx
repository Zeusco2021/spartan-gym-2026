import { Box, Typography } from '@mui/material';
import { useTranslation } from 'react-i18next';
import { useAppSelector } from '@/app/hooks';
import ClientDashboard from './ClientDashboard';
import TrainerDashboard from './TrainerDashboard';
import AdminDashboard from './AdminDashboard';

export default function DashboardPage() {
  const { t } = useTranslation('dashboard');
  const user = useAppSelector((state) => state.auth.user);

  const role = user?.role ?? 'client';

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        {t('welcome')}, {user?.name ?? ''}
      </Typography>

      {role === 'client' && <ClientDashboard />}
      {role === 'trainer' && <TrainerDashboard />}
      {role === 'admin' && <AdminDashboard />}
    </Box>
  );
}
