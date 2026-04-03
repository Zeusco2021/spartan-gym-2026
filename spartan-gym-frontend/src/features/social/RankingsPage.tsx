import { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  CircularProgress,
  Alert,
  Avatar,
  List,
  ListItem,
  ListItemAvatar,
  ListItemText,
  ToggleButton,
  ToggleButtonGroup,
  Chip,
} from '@mui/material';
import EmojiEventsIcon from '@mui/icons-material/EmojiEvents';
import { useTranslation } from 'react-i18next';
import { useGetRankingsQuery } from '@/api/socialApi';
import { useSelector } from 'react-redux';
import type { RootState } from '@/app/store';
import { socketService } from '@/websocket/socketService';

const CATEGORIES = ['strength', 'endurance', 'consistency', 'nutrition'] as const;

export default function RankingsPage() {
  const { t } = useTranslation('social');
  const [category, setCategory] = useState<string>('strength');

  const { data, isLoading, isError } = useGetRankingsQuery({ category });
  const wsRankings = useSelector(
    (state: RootState) => state.websocket.rankings[category],
  );

  const rankings = wsRankings ?? data;

  useEffect(() => {
    socketService.joinRanking(category);
    return () => {
      socketService.leaveRanking(category);
    };
  }, [category]);

  return (
    <Box p={3}>
      <Typography variant="h4" gutterBottom>
        {t('rankingsTitle')}
      </Typography>

      <ToggleButtonGroup
        value={category}
        exclusive
        onChange={(_, v) => v && setCategory(v)}
        sx={{ mb: 3 }}
        aria-label={t('rankingCategory')}
      >
        {CATEGORIES.map((cat) => (
          <ToggleButton key={cat} value={cat} aria-label={t(cat)}>
            {t(cat)}
          </ToggleButton>
        ))}
      </ToggleButtonGroup>

      {isLoading && (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
          <CircularProgress aria-label={t('loading')} />
        </Box>
      )}

      {isError && <Alert severity="error">{t('error')}</Alert>}

      {rankings && rankings.length === 0 && (
        <Alert severity="info">{t('noRankings')}</Alert>
      )}

      {rankings && rankings.length > 0 && (
        <List aria-label={t('rankingsTitle')}>
          {rankings.map((entry) => (
            <ListItem
              key={entry.userId}
              aria-label={`${t('rank')} ${entry.rank} - ${entry.userName}`}
              sx={{
                bgcolor: entry.rank <= 3 ? 'action.hover' : 'transparent',
                borderRadius: 1,
                mb: 0.5,
              }}
            >
              <Chip
                label={`#${entry.rank}`}
                size="small"
                color={entry.rank === 1 ? 'warning' : entry.rank <= 3 ? 'primary' : 'default'}
                sx={{ mr: 2, minWidth: 48 }}
              />
              <ListItemAvatar>
                {entry.rank <= 3 ? (
                  <Avatar sx={{ bgcolor: entry.rank === 1 ? 'gold' : entry.rank === 2 ? 'silver' : '#cd7f32' }}>
                    <EmojiEventsIcon />
                  </Avatar>
                ) : (
                  <Avatar src={entry.profilePhotoUrl}>
                    {entry.userName.charAt(0)}
                  </Avatar>
                )}
              </ListItemAvatar>
              <ListItemText
                primary={entry.userName}
                secondary={`${t('score')}: ${entry.score}`}
              />
            </ListItem>
          ))}
        </List>
      )}
    </Box>
  );
}
