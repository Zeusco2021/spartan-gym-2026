import { baseApi } from './baseApi';
import type {
  NotificationPreferences,
  AppNotification,
  PagedResponse,
} from '@/types';

export const notificationsApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    getPreferences: builder.query<NotificationPreferences, void>({
      query: () => '/api/notifications/preferences',
      providesTags: ['Notification'],
    }),
    updatePreferences: builder.mutation<
      NotificationPreferences,
      Partial<NotificationPreferences>
    >({
      query: (data) => ({
        url: '/api/notifications/preferences',
        method: 'PUT',
        body: data,
      }),
      invalidatesTags: ['Notification'],
    }),
    getHistory: builder.query<
      PagedResponse<AppNotification>,
      { page?: number }
    >({
      query: (params) => ({ url: '/api/notifications/history', params }),
    }),
  }),
});

export const {
  useGetPreferencesQuery,
  useUpdatePreferencesMutation,
  useGetHistoryQuery,
} = notificationsApi;
