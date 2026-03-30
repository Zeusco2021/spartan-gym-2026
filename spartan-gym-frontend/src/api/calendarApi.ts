import { baseApi } from './baseApi';
import type { CalendarEvent } from '@/types';

export const calendarApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    getEvents: builder.query<
      CalendarEvent[],
      { from: string; to: string }
    >({
      query: (params) => ({ url: '/api/calendar/events', params }),
      providesTags: ['CalendarEvent'],
    }),
    createEvent: builder.mutation<CalendarEvent, Partial<CalendarEvent>>({
      query: (data) => ({
        url: '/api/calendar/events',
        method: 'POST',
        body: data,
      }),
      invalidatesTags: ['CalendarEvent'],
    }),
    updateEvent: builder.mutation<
      CalendarEvent,
      { id: string; data: Partial<CalendarEvent> }
    >({
      query: ({ id, data }) => ({
        url: `/api/calendar/events/${id}`,
        method: 'PUT',
        body: data,
      }),
      invalidatesTags: ['CalendarEvent'],
    }),
    deleteEvent: builder.mutation<void, string>({
      query: (id) => ({
        url: `/api/calendar/events/${id}`,
        method: 'DELETE',
      }),
      invalidatesTags: ['CalendarEvent'],
    }),
    syncExternalCalendar: builder.mutation<
      void,
      { provider: string; token: string }
    >({
      query: (data) => ({
        url: '/api/calendar/sync',
        method: 'POST',
        body: data,
      }),
      invalidatesTags: ['CalendarEvent'],
    }),
  }),
});

export const {
  useGetEventsQuery,
  useCreateEventMutation,
  useUpdateEventMutation,
  useDeleteEventMutation,
  useSyncExternalCalendarMutation,
} = calendarApi;
