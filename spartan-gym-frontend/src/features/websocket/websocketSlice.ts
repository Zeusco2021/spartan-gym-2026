import { createSlice, type PayloadAction } from '@reduxjs/toolkit';
import type { Message } from '@/types';

interface WebSocketState {
  connected: boolean;
  activeConversation: string | null;
  incomingMessages: Message[];
  occupancyUpdates: Record<string, number>;
  typingUsers: Record<string, string[]>;
}

const initialState: WebSocketState = {
  connected: false,
  activeConversation: null,
  incomingMessages: [],
  occupancyUpdates: {},
  typingUsers: {},
};

const websocketSlice = createSlice({
  name: 'websocket',
  initialState,
  reducers: {
    setConnected(state, action: PayloadAction<boolean>) {
      state.connected = action.payload;
    },
    addIncomingMessage(state, action: PayloadAction<Message>) {
      state.incomingMessages.push(action.payload);
    },
    updateOccupancy(
      state,
      action: PayloadAction<{ gymId: string; occupancy: number }>,
    ) {
      state.occupancyUpdates[action.payload.gymId] = action.payload.occupancy;
    },
    setTypingUsers(
      state,
      action: PayloadAction<{ conversationId: string; users: string[] }>,
    ) {
      state.typingUsers[action.payload.conversationId] =
        action.payload.users;
    },
    clearIncomingMessages(state) {
      state.incomingMessages = [];
    },
  },
});

export const {
  setConnected,
  addIncomingMessage,
  updateOccupancy,
  setTypingUsers,
  clearIncomingMessages,
} = websocketSlice.actions;
export default websocketSlice.reducer;
