import { combineReducers } from '@reduxjs/toolkit';
import { baseApi } from '@/api/baseApi';
import authReducer from '@/features/auth/authSlice';
import uiReducer from '@/features/settings/uiSlice';
import websocketReducer from '@/features/websocket/websocketSlice';

const rootReducer = combineReducers({
  [baseApi.reducerPath]: baseApi.reducer,
  auth: authReducer,
  ui: uiReducer,
  websocket: websocketReducer,
});

export default rootReducer;
