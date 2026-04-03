import { createSlice, type PayloadAction } from '@reduxjs/toolkit';
import type { Message } from '@/types';

interface RankingEntry {
  userId: string;
  score: number;
  rank: number;
}

interface LiveWorkoutUpdate {
  userId: string;
  sessionId: string;
  currentExercise: string;
  progress: number;
}

interface WebSocketState {
  connected: boolean;
  activeConversation: string | null;
  incomingMessages: Message[];
  occupancyUpdates: Record<string, number>;
  typingUsers: Record<string, string[]>;
  rankings: Record<string, RankingEntry[]>;
  liveWorkouts: Record<string, LiveWorkoutUpdate>;
}

const initialState: WebSocketState = {
  connected: false,
  activeConversation: null,
  incomingMessages: [],
  occupancyUpdates: {},
  typingUsers: {},
  rankings: {},
  liveWorkouts: {},
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
    updateRanking(
      state,
      action: PayloadAction<{ category: string; entries: RankingEntry[] }>,
    ) {
      state.rankings[action.payload.category] = action.payload.entries;
    },
    updateLiveWorkout(state, action: PayloadAction<LiveWorkoutUpdate>) {
      state.liveWorkouts[action.payload.sessionId] = action.payload;
    },
  },
});

export const {
  setConnected,
  addIncomingMessage,
  updateOccupancy,
  setTypingUsers,
  clearIncomingMessages,
  updateRanking,
  updateLiveWorkout,
} = websocketSlice.actions;
export default websocketSlice.reducer;
