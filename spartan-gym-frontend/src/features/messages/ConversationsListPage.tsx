import {
  Box,
  Typography,
  List,
  ListItemButton,
  ListItemAvatar,
  ListItemText,
  Avatar,
  Badge,
  CircularProgress,
  Alert,
  Chip,
  AvatarGroup,
} from '@mui/material';
import GroupIcon from '@mui/icons-material/Group';
import { useTranslation } from 'react-i18next';
import { useGetConversationsQuery } from '@/api/messagesApi';
import type { Conversation } from '@/types';

interface ConversationsListPageProps {
  onSelectConversation: (conversation: Conversation) => void;
  selectedId?: string;
}

export default function ConversationsListPage({
  onSelectConversation,
  selectedId,
}: ConversationsListPageProps) {
  const { t } = useTranslation('messages');
  const { data, isLoading, isError } = useGetConversationsQuery();

  return (
    <Box>
      <Typography variant="h5" sx={{ p: 2 }}>
        {t('conversations')}
      </Typography>

      {isLoading && (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
          <CircularProgress aria-label={t('loading')} />
        </Box>
      )}

      {isError && <Alert severity="error" sx={{ m: 2 }}>{t('error')}</Alert>}

      {data && data.length === 0 && (
        <Alert severity="info" sx={{ m: 2 }}>{t('noConversations')}</Alert>
      )}

      {data && data.length > 0 && (
        <List aria-label={t('conversations')}>
          {data.map((conversation) => {
            const displayName =
              conversation.type === 'group'
                ? conversation.participants.map((p) => p.name).join(', ')
                : conversation.participants[0]?.name ?? t('unknown');
            const hasOnline = conversation.participants.some((p) => p.online);

            return (
              <ListItemButton
                key={conversation.id}
                selected={selectedId === conversation.id}
                onClick={() => onSelectConversation(conversation)}
                aria-label={`${t('conversation')} ${displayName}${conversation.unreadCount > 0 ? `, ${conversation.unreadCount} ${t('unread')}` : ''}`}
              >
                <ListItemAvatar>
                  {conversation.type === 'group' ? (
                    <AvatarGroup max={2} sx={{ '& .MuiAvatar-root': { width: 32, height: 32 } }}>
                      {conversation.participants.slice(0, 2).map((p) => (
                        <Avatar key={p.userId} src={p.profilePhotoUrl}>
                          {p.name.charAt(0)}
                        </Avatar>
                      ))}
                    </AvatarGroup>
                  ) : (
                    <Badge
                      color="success"
                      variant="dot"
                      invisible={!hasOnline}
                      overlap="circular"
                    >
                      <Avatar src={conversation.participants[0]?.profilePhotoUrl}>
                        {conversation.participants[0]?.name.charAt(0)}
                      </Avatar>
                    </Badge>
                  )}
                </ListItemAvatar>
                <ListItemText
                  primary={
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      {displayName}
                      {conversation.type === 'group' && (
                        <GroupIcon fontSize="small" color="action" />
                      )}
                    </Box>
                  }
                  secondary={new Date(conversation.lastMessageAt).toLocaleString()}
                />
                {conversation.unreadCount > 0 && (
                  <Chip
                    label={conversation.unreadCount}
                    color="primary"
                    size="small"
                    aria-label={`${conversation.unreadCount} ${t('unread')}`}
                  />
                )}
              </ListItemButton>
            );
          })}
        </List>
      )}
    </Box>
  );
}
