import {
  createApi,
  fetchBaseQuery,
  type BaseQueryFn,
  type FetchArgs,
  type FetchBaseQueryError,
} from '@reduxjs/toolkit/query/react';
import type { RootState } from '@/app/store';

// Mutex to prevent concurrent refresh attempts
let isRefreshing = false;
let refreshPromise: Promise<boolean> | null = null;

const baseQuery = fetchBaseQuery({
  baseUrl: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  credentials: 'include',
  prepareHeaders: (headers, { getState }) => {
    const state = getState() as RootState;
    const token = state.auth.token;
    if (token) {
      headers.set('Authorization', `Bearer ${token}`);
    }
    headers.set('Accept-Language', state.ui.locale);
    return headers;
  },
});

const baseQueryWithReauth: BaseQueryFn<
  string | FetchArgs,
  unknown,
  FetchBaseQueryError
> = async (args, api, extraOptions) => {
  let result = await baseQuery(args, api, extraOptions);

  if (result.error?.status === 401) {
    if (!isRefreshing) {
      isRefreshing = true;
      refreshPromise = (async () => {
        const refreshResult = await baseQuery(
          { url: '/api/users/token/refresh', method: 'POST' },
          api,
          extraOptions,
        );
        if (refreshResult.data) {
          const { setCredentials } = await import(
            '@/features/auth/authSlice'
          );
          api.dispatch(
            setCredentials(
              refreshResult.data as { user: import('@/types').User; token: string },
            ),
          );
          return true;
        }
        const { logout } = await import('@/features/auth/authSlice');
        api.dispatch(logout());
        return false;
      })().finally(() => {
        isRefreshing = false;
        refreshPromise = null;
      });
    }

    const refreshSuccess = await refreshPromise;
    if (refreshSuccess) {
      result = await baseQuery(args, api, extraOptions);
    }
  }

  return result;
};

export const baseApi = createApi({
  reducerPath: 'api',
  baseQuery: baseQueryWithReauth,
  tagTypes: [
    'User',
    'Gym',
    'TrainingPlan',
    'Workout',
    'NutritionPlan',
    'MealLog',
    'Challenge',
    'Achievement',
    'Ranking',
    'Subscription',
    'Transaction',
    'Conversation',
    'Message',
    'CalendarEvent',
    'GroupClass',
    'Reservation',
    'Analytics',
    'Notification',
  ],
  endpoints: () => ({}),
});
