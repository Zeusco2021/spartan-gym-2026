import { useEffect, useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Switch,
  FormControlLabel,
  Button,
  Alert,
  CircularProgress,
  Table,
  TableHead,
  TableBody,
  TableRow,
  TableCell,
} from '@mui/material';
import { useTranslation } from 'react-i18next';
import {
  useGetPreferencesQuery,
  useUpdatePreferencesMutation,
} from '@/api/notificationsApi';
import type { NotificationPreferences } from '@/types/notifications';

const CHANNELS = ['email', 'push', 'sms'] as const;
const CATEGORIES = ['training', 'nutrition', 'social', 'payments', 'calendar', 'messages'] as const;

type Channel = (typeof CHANNELS)[number];

export default function NotificationSettingsPage() {
  const { t } = useTranslation('settings');
  const { data: prefs, isLoading, isError } = useGetPreferencesQuery(undefined, {
    refetchOnMountOrArgChange: true,
  });
  const [updatePreferences, { isLoading: isSaving }] = useUpdatePreferencesMutation();

  const [local, setLocal] = useState<NotificationPreferences>({
    email: true,
    push: true,
    sms: false,
    categories: {},
  });
  const [successMsg, setSuccessMsg] = useState('');
  const [errorMsg, setErrorMsg] = useState('');

  useEffect(() => {
    if (prefs) {
      setLocal({
        email: prefs.email,
        push: prefs.push,
        sms: prefs.sms,
        categories: { ...prefs.categories },
      });
    }
  }, [prefs]);

  const toggleGlobal = (channel: Channel) => {
    setLocal((prev) => ({ ...prev, [channel]: !prev[channel] }));
  };

  const toggleCategory = (category: string, channel: Channel) => {
    setLocal((prev) => {
      const current = prev.categories[category] ?? { email: true, push: true, sms: false };
      return {
        ...prev,
        categories: {
          ...prev.categories,
          [category]: { ...current, [channel]: !current[channel] },
        },
      };
    });
  };

  const handleSave = async () => {
    setSuccessMsg('');
    setErrorMsg('');
    try {
      await updatePreferences(local).unwrap();
      setSuccessMsg(t('preferencesUpdated'));
    } catch {
      setErrorMsg(t('preferencesError'));
    }
  };

  if (isLoading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (isError) {
    return <Alert severity="error">{t('preferencesError')}</Alert>;
  }

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        {t('notificationPreferences')}
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

      {/* Global channel toggles */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            {t('globalChannels')}
          </Typography>
          <Box sx={{ display: 'flex', gap: 3, flexWrap: 'wrap' }}>
            {CHANNELS.map((ch) => (
              <FormControlLabel
                key={ch}
                control={
                  <Switch
                    checked={local[ch]}
                    onChange={() => toggleGlobal(ch)}
                  />
                }
                label={t(`${ch}Channel`)}
              />
            ))}
          </Box>
        </CardContent>
      </Card>

      {/* Per-category preferences table */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            {t('categoryPreferences')}
          </Typography>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>{t('notifications')}</TableCell>
                {CHANNELS.map((ch) => (
                  <TableCell key={ch} align="center">
                    {t(`${ch}Channel`)}
                  </TableCell>
                ))}
              </TableRow>
            </TableHead>
            <TableBody>
              {CATEGORIES.map((cat) => {
                const catPrefs = local.categories[cat] ?? { email: true, push: true, sms: false };
                return (
                  <TableRow key={cat}>
                    <TableCell>{t(cat)}</TableCell>
                    {CHANNELS.map((ch) => (
                      <TableCell key={ch} align="center">
                        <Switch
                          size="small"
                          checked={catPrefs[ch]}
                          onChange={() => toggleCategory(cat, ch)}
                        />
                      </TableCell>
                    ))}
                  </TableRow>
                );
              })}
            </TableBody>
          </Table>
        </CardContent>
      </Card>

      <Button
        variant="contained"
        size="large"
        onClick={handleSave}
        disabled={isSaving}
        startIcon={isSaving ? <CircularProgress size={20} /> : undefined}
      >
        {t('save')}
      </Button>
    </Box>
  );
}
