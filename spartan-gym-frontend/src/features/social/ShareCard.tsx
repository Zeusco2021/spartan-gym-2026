import { Box, Typography, Avatar, Card, CardContent } from '@mui/material';
import EmojiEventsIcon from '@mui/icons-material/EmojiEvents';
import { useTranslation } from 'react-i18next';
import type { Achievement } from '@/types';

interface ShareCardProps {
  achievement: Achievement;
}

export default function ShareCard({ achievement }: ShareCardProps) {
  const { t } = useTranslation('social');

  return (
    <Card
      sx={{
        background: 'linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%)',
        color: 'white',
        p: 2,
      }}
      aria-label={`${t('shareCard')} ${achievement.name}`}
    >
      <CardContent sx={{ textAlign: 'center' }}>
        <Box sx={{ display: 'flex', justifyContent: 'center', mb: 2 }}>
          {achievement.iconUrl ? (
            <Avatar
              src={achievement.iconUrl}
              sx={{ width: 80, height: 80 }}
              alt={achievement.name}
            />
          ) : (
            <Avatar sx={{ width: 80, height: 80, bgcolor: 'gold' }}>
              <EmojiEventsIcon sx={{ fontSize: 48 }} />
            </Avatar>
          )}
        </Box>
        <Typography variant="h5" sx={{ fontWeight: 'bold', mb: 1 }}>
          {achievement.name}
        </Typography>
        <Typography variant="body1" sx={{ mb: 2, opacity: 0.9 }}>
          {achievement.description}
        </Typography>
        <Typography variant="caption" sx={{ opacity: 0.7 }}>
          {t('earnedOn')} {new Date(achievement.earnedAt).toLocaleDateString()}
        </Typography>
        <Typography
          variant="caption"
          display="block"
          sx={{ mt: 2, opacity: 0.5, fontStyle: 'italic' }}
        >
          Spartan Golden Gym
        </Typography>
      </CardContent>
    </Card>
  );
}
