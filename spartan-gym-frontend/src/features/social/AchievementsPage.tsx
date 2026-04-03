import { useState } from 'react';
import {
  Box,
  Typography,
  Card,
  CardContent,
  CardActions,
  Button,
  CircularProgress,
  Alert,
  Grid2 as Grid,
  Avatar,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
} from '@mui/material';
import ShareIcon from '@mui/icons-material/Share';
import { useTranslation } from 'react-i18next';
import { useGetAchievementsQuery, useShareAchievementMutation } from '@/api/socialApi';
import type { Achievement } from '@/types';
import ShareCard from './ShareCard';

export default function AchievementsPage() {
  const { t } = useTranslation('social');
  const { data, isLoading, isError } = useGetAchievementsQuery();
  const [shareAchievement] = useShareAchievementMutation();
  const [selectedAchievement, setSelectedAchievement] = useState<Achievement | null>(null);

  const handleShare = async (achievement: Achievement) => {
    setSelectedAchievement(achievement);
    await shareAchievement(achievement.id);
  };

  return (
    <Box p={3}>
      <Typography variant="h4" gutterBottom>
        {t('achievementsTitle')}
      </Typography>

      {isLoading && (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
          <CircularProgress aria-label={t('loading')} />
        </Box>
      )}

      {isError && <Alert severity="error">{t('error')}</Alert>}

      {data && data.length === 0 && (
        <Alert severity="info">{t('noAchievements')}</Alert>
      )}

      {data && data.length > 0 && (
        <Grid container spacing={3}>
          {data.map((achievement) => (
            <Grid key={achievement.id} size={{ xs: 12, sm: 6, md: 4 }}>
              <Card aria-label={achievement.name}>
                <CardContent sx={{ textAlign: 'center' }}>
                  <Avatar
                    src={achievement.iconUrl}
                    sx={{ width: 64, height: 64, mx: 'auto', mb: 1 }}
                    alt={achievement.name}
                  />
                  <Typography variant="h6">{achievement.name}</Typography>
                  <Typography variant="body2" color="text.secondary">
                    {achievement.description}
                  </Typography>
                  <Typography variant="caption" color="text.secondary" display="block" sx={{ mt: 1 }}>
                    {new Date(achievement.earnedAt).toLocaleDateString()}
                  </Typography>
                </CardContent>
                <CardActions sx={{ justifyContent: 'center' }}>
                  <Button
                    size="small"
                    startIcon={<ShareIcon />}
                    onClick={() => handleShare(achievement)}
                    aria-label={`${t('share')} ${achievement.name}`}
                  >
                    {t('share')}
                  </Button>
                </CardActions>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}

      <Dialog
        open={!!selectedAchievement}
        onClose={() => setSelectedAchievement(null)}
        maxWidth="sm"
        fullWidth
        aria-label={t('shareDialog')}
      >
        <DialogTitle>{t('shareAchievement')}</DialogTitle>
        <DialogContent>
          {selectedAchievement && <ShareCard achievement={selectedAchievement} />}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setSelectedAchievement(null)} aria-label={t('close')}>
            {t('close')}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
