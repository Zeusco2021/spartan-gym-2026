import { useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  Alert,
  CircularProgress,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
} from '@mui/material';
import { useTranslation } from 'react-i18next';
import {
  useGetProfileQuery,
  useSetupMfaMutation,
  useRequestDataExportMutation,
  useDeleteAccountMutation,
} from '@/api/usersApi';
import { useAuth } from '@/hooks/useAuth';

export default function PrivacySettingsPage() {
  const { t } = useTranslation('settings');
  const { logout } = useAuth();

  const { data: profile } = useGetProfileQuery();
  const [setupMfa, { isLoading: mfaLoading }] = useSetupMfaMutation();
  const [requestDataExport, { isLoading: exportLoading }] = useRequestDataExportMutation();
  const [deleteAccount, { isLoading: deleteLoading }] = useDeleteAccountMutation();

  const [mfaData, setMfaData] = useState<{ qrCodeUrl: string; secret: string } | null>(null);
  const [mfaError, setMfaError] = useState('');
  const [exportSuccess, setExportSuccess] = useState(false);
  const [exportError, setExportError] = useState('');
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [deleteError, setDeleteError] = useState('');

  const handleSetupMfa = async () => {
    setMfaError('');
    try {
      const result = await setupMfa().unwrap();
      setMfaData(result);
    } catch {
      setMfaError(t('mfaSetupError'));
    }
  };

  const handleExportData = async () => {
    setExportError('');
    setExportSuccess(false);
    try {
      await requestDataExport().unwrap();
      setExportSuccess(true);
    } catch {
      setExportError(t('exportError'));
    }
  };

  const handleDeleteAccount = async () => {
    setDeleteError('');
    try {
      await deleteAccount().unwrap();
      setDeleteDialogOpen(false);
      logout();
    } catch {
      setDeleteError(t('deleteError'));
    }
  };

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        {t('privacy')}
      </Typography>

      {/* MFA Setup Section */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            {t('mfa')}
          </Typography>

          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            {t('mfaStatus')}:{' '}
            {profile?.mfaEnabled ? t('mfaEnabled') : t('mfaDisabled')}
          </Typography>

          {mfaError && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {mfaError}
            </Alert>
          )}

          {profile?.mfaEnabled && !mfaData && (
            <Alert severity="success">{t('mfaEnabled')}</Alert>
          )}

          {!profile?.mfaEnabled && !mfaData && (
            <Button
              variant="contained"
              onClick={handleSetupMfa}
              disabled={mfaLoading}
              startIcon={mfaLoading ? <CircularProgress size={20} /> : undefined}
            >
              {t('enableMfa')}
            </Button>
          )}

          {mfaData && (
            <Box sx={{ mt: 2 }}>
              <Alert severity="info" sx={{ mb: 2 }}>
                {t('qrCodeInstructions')}
              </Alert>
              <Box sx={{ display: 'flex', justifyContent: 'center', mb: 2 }}>
                <img
                  src={mfaData.qrCodeUrl}
                  alt="MFA QR Code"
                  style={{ maxWidth: 256, maxHeight: 256 }}
                />
              </Box>
              <Typography variant="body2">
                {t('secretKey')}: <code>{mfaData.secret}</code>
              </Typography>
            </Box>
          )}
        </CardContent>
      </Card>

      {/* Data Export Section */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            {t('exportData')}
          </Typography>

          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            {t('exportDataDescription')}
          </Typography>

          {exportError && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {exportError}
            </Alert>
          )}

          {exportSuccess && (
            <Alert severity="success" sx={{ mb: 2 }}>
              {t('exportRequested')}
            </Alert>
          )}

          <Button
            variant="outlined"
            onClick={handleExportData}
            disabled={exportLoading}
            startIcon={exportLoading ? <CircularProgress size={20} /> : undefined}
          >
            {t('exportData')}
          </Button>
        </CardContent>
      </Card>

      {/* Account Deletion Section */}
      <Card>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            {t('deleteAccount')}
          </Typography>

          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            {t('deleteAccountWarning')}
          </Typography>

          {deleteError && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {deleteError}
            </Alert>
          )}

          <Button
            variant="contained"
            color="error"
            onClick={() => setDeleteDialogOpen(true)}
          >
            {t('deleteAccount')}
          </Button>
        </CardContent>
      </Card>

      {/* Delete Confirmation Dialog */}
      <Dialog
        open={deleteDialogOpen}
        onClose={() => setDeleteDialogOpen(false)}
      >
        <DialogTitle>{t('deleteAccount')}</DialogTitle>
        <DialogContent>
          <DialogContentText>{t('deleteAccountConfirm')}</DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialogOpen(false)}>
            {t('cancel')}
          </Button>
          <Button
            onClick={handleDeleteAccount}
            color="error"
            disabled={deleteLoading}
            startIcon={deleteLoading ? <CircularProgress size={20} /> : undefined}
          >
            {deleteLoading ? t('deleting') : t('deleteAccount')}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
