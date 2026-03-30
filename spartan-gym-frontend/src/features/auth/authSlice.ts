import { createSlice, type PayloadAction } from '@reduxjs/toolkit';
import type { User, AuthState } from '@/types';

const initialState: AuthState = {
  user: null,
  token: null,
  isAuthenticated: false,
  mfaPending: false,
  mfaSessionToken: null,
};

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    setCredentials(
      state,
      action: PayloadAction<{ user: User; token: string }>,
    ) {
      state.user = action.payload.user;
      state.token = action.payload.token;
      state.isAuthenticated = true;
      state.mfaPending = false;
      state.mfaSessionToken = null;
    },
    setMfaPending(state, action: PayloadAction<string>) {
      state.mfaPending = true;
      state.mfaSessionToken = action.payload;
    },
    logout(state) {
      state.user = null;
      state.token = null;
      state.isAuthenticated = false;
      state.mfaPending = false;
      state.mfaSessionToken = null;
    },
  },
});

export const { setCredentials, setMfaPending, logout } = authSlice.actions;
export default authSlice.reducer;
