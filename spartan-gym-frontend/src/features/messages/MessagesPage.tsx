import { useState } from 'react';
import { Box, Paper } from '@mui/material';
import ConversationsListPage from './ConversationsListPage';
import ChatPage from './ChatPage';
import type { Conversation } from '@/types';

export default function MessagesPage() {
  const [selectedConversation, setSelectedConversation] = useState<Conversation | null>(null);

  return (
    <Box sx={{ display: 'flex', height: 'calc(100vh - 64px)' }}>
      <Paper sx={{ width: 360, borderRight: 1, borderColor: 'divider', overflow: 'auto' }}>
        <ConversationsListPage
          onSelectConversation={setSelectedConversation}
          selectedId={selectedConversation?.id}
        />
      </Paper>
      <Box sx={{ flex: 1 }}>
        {selectedConversation ? (
          <ChatPage conversation={selectedConversation} />
        ) : (
          <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%' }}>
            <Box sx={{ textAlign: 'center', color: 'text.secondary' }}>
              Select a conversation to start chatting
            </Box>
          </Box>
        )}
      </Box>
    </Box>
  );
}
