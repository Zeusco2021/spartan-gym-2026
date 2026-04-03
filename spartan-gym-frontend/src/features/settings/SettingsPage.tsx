import { useState } from 'react';
import { Box, Tabs, Tab } from '@mui/material';
import { useTranslation } from 'react-i18next';
import LanguageSettingsPage from './LanguageSettingsPage';
import NotificationSettingsPage from './NotificationSettingsPage';
import ProfileSettingsPage from './ProfileSettingsPage';
import PrivacySettingsPage from './PrivacySettingsPage';

const TAB_KEYS = ['language', 'notifications', 'profile', 'privacy'] as const;

export default function SettingsPage() {
  const { t } = useTranslation('settings');
  const [tab, setTab] = useState(0);

  return (
    <Box sx={{ p: 3 }}>
      <Tabs
        value={tab}
        onChange={(_, v) => setTab(v)}
        aria-label={t('settingsNavigation')}
        sx={{ mb: 3 }}
      >
        {TAB_KEYS.map((key) => (
          <Tab key={key} label={t(key)} aria-label={t(key)} />
        ))}
      </Tabs>

      {tab === 0 && <LanguageSettingsPage />}
      {tab === 1 && <NotificationSettingsPage />}
      {tab === 2 && <ProfileSettingsPage />}
      {tab === 3 && <PrivacySettingsPage />}
    </Box>
  );
}
