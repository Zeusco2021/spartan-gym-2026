import { configureStore, createListenerMiddleware } from '@reduxjs/toolkit';
import { baseApi } from '@/api/baseApi';
import { logout } from '@/features/auth/authSlice';
import rootReducer from '@/app/rootReducer';

const listenerMiddleware = createListenerMiddleware();

listenerMiddleware.startListening({
  actionCreator: logout,
  effect: (_action, listenerApi) => {
    listenerApi.dispatch(baseApi.util.resetApiState());
  },
});

export const store = configureStore({
  reducer: rootReducer,
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware()
      .prepend(listenerMiddleware.middleware)
      .concat(baseApi.middleware),
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
