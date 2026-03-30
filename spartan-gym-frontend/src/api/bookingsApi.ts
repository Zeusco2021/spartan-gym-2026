import { baseApi } from './baseApi';
import type {
  GroupClass,
  ClassReservation,
  TrainerAvailability,
  PagedResponse,
} from '@/types';

export const bookingsApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    getClasses: builder.query<
      PagedResponse<GroupClass>,
      {
        gymId?: string;
        type?: string;
        difficulty?: string;
        from?: string;
        to?: string;
      }
    >({
      query: (params) => ({ url: '/api/bookings/classes', params }),
      providesTags: ['GroupClass'],
    }),
    reserveClass: builder.mutation<ClassReservation, string>({
      query: (classId) => ({
        url: `/api/bookings/classes/${classId}/reserve`,
        method: 'POST',
      }),
      invalidatesTags: ['GroupClass', 'Reservation'],
    }),
    cancelReservation: builder.mutation<void, string>({
      query: (classId) => ({
        url: `/api/bookings/classes/${classId}/cancel`,
        method: 'POST',
      }),
      invalidatesTags: ['GroupClass', 'Reservation'],
    }),
    getTrainerAvailability: builder.query<TrainerAvailability[], string>({
      query: (trainerId) =>
        `/api/bookings/trainers/${trainerId}/availability`,
    }),
  }),
});

export const {
  useGetClassesQuery,
  useReserveClassMutation,
  useCancelReservationMutation,
  useGetTrainerAvailabilityQuery,
} = bookingsApi;
