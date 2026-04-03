import { useState, useEffect, useRef, useCallback } from 'react';
import {
  Box,
  Typography,
  TextField,
  IconButton,
  CircularProgress,
  Alert,
  Avatar,
  Paper,
} from '@mui/material';
import SendIcon from '@mui/icons-material/Send';
import { useTranslation } from 'react-i18next';
import { useGetMessagesQuery, useSendMessageMutation } from '@/api/messagesApi';
import { useSelector } from 'react-redux';
import type { RootState } from '@/app/store';
import { socketService } from '@/websocket/socketService';
import type { Conversation } from '@/types';

interface ChatPageProps {
  conversation: Conversation;
}

export default function ChatPage({ conversation }: ChatPageProps) {
  const { t } = useTranslation('messages');
  const [messageText, setMessageText] = useState('');
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const typingTimeoutRef = useRef<ReturnType<typeof setTimeout>>();

  const { data, isLoading, isError } = useGetMessagesQuery({
    conversationId: conversation.id,
  });
  const [sendMessage] = useSendMessageMutation();

  const incomingMessages = useSelector(
    (state: RootState) => state.websocket.incomingMessages,
  );
  const typingUsers = useSelector(
    (state: RootState) => state.websocket.typingUsers[conversation.id] ?? [],
  );

  const conversationMessages = incomingMessages.filter(
    (m) => m.conversationId === conversation.id,
  );

  const allMessages = [
    ...(data?.content ?? []),
    ...conversationMessages.filter(
      (cm) => !data?.content.some((m) => m.id === cm.id),
    ),
  ];

  useEffect(() => {
    if (messagesEndRef.current && typeof messagesEndRef.current.scrollIntoView === 'function') {
      messagesEndRef.current.scrollIntoView({ behavior: 'smooth' });
    }
  }, [allMessages.length]);

  const handleTyping = useCallback(() => {
    socketService.startTyping(conversation.id);
    if (typingTimeoutRef.current) clearTimeout(typingTimeoutRef.current);
    typingTimeoutRef.current = setTimeout(() => {}, 2000);
  }, [conversation.id]);

  const handleSend = async () => {
    const text = messageText.trim();
    if (!text) return;
    setMessageText('');
    socketService.sendMessage(conversation.id, text);
    await sendMessage({
      conversationId: conversation.id,
      content: text,
      contentType: 'text',
    });
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  const participantMap = Object.fromEntries(
    conversation.participants.map((p) => [p.userId, p]),
  );

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
      <Box sx={{ p: 2, borderBottom: 1, borderColor: 'divider' }}>
        <Typography variant="h6">
          {conversation.type === 'group'
            ? conversation.participants.map((p) => p.name).join(', ')
            : conversation.participants[0]?.name}
        </Typography>
      </Box>

      <Box sx={{ flex: 1, overflow: 'auto', p: 2 }}>
        {isLoading && (
          <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
            <CircularProgress aria-label={t('loading')} />
          </Box>
        )}

        {isError && <Alert severity="error">{t('error')}</Alert>}

        {allMessages.map((message) => {
          const sender = participantMap[message.senderId];
          return (
            <Box
              key={message.id}
              sx={{ display: 'flex', mb: 1.5, gap: 1 }}
              aria-label={`${t('messageFrom')} ${sender?.name ?? t('unknown')}`}
            >
              <Avatar
                src={sender?.profilePhotoUrl}
                sx={{ width: 32, height: 32 }}
                alt={sender?.name}
              >
                {sender?.name?.charAt(0)}
              </Avatar>
              <Box>
                <Typography variant="caption" color="text.secondary">
                  {sender?.name} · {new Date(message.sentAt).toLocaleTimeString()}
                </Typography>
                <Paper sx={{ p: 1, px: 1.5 }} elevation={1}>
                  <Typography variant="body2">{message.content}</Typography>
                </Paper>
                {message.status === 'read' && (
                  <Typography variant="caption" color="text.secondary">
                    {t('read')}
                  </Typography>
                )}
              </Box>
            </Box>
          );
        })}

        {typingUsers.length > 0 && (
          <Typography variant="caption" color="text.secondary" aria-live="polite">
            {t('typing')}...
          </Typography>
        )}
        <div ref={messagesEndRef} />
      </Box>

      <Box sx={{ p: 2, borderTop: 1, borderColor: 'divider', display: 'flex', gap: 1 }}>
        <TextField
          fullWidth
          size="small"
          placeholder={t('typeMessage')}
          value={messageText}
          onChange={(e) => {
            setMessageText(e.target.value);
            handleTyping();
          }}
          onKeyDown={handleKeyDown}
          aria-label={t('typeMessage')}
          multiline
          maxRows={3}
        />
        <IconButton
          onClick={handleSend}
          disabled={!messageText.trim()}
          color="primary"
          aria-label={t('sendMessage')}
        >
          <SendIcon />
        </IconButton>
      </Box>
    </Box>
  );
}
