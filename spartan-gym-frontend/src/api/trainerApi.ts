import { baseApi } from './baseApi';
import type { TrainerClient } from '@/types/trainer';
import type { WorkoutProgress, PagedResponse, WorkoutSession } from '@/types';

export const trainerApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    getTrainerClients: builder.query<TrainerClient[], void>({
      query: () => '/api/training/clients',
      providesTags: ['TrainingPlan'],
    }),
    getClientProgress: builder.query<
      WorkoutProgress,
      { clientId: string; period: string }
    >({
      query: ({ clientId, period }) =>
        `/api/workouts/progress?userId=${clientId}&period=${period}`,
    }),
    getClientWorkoutHistory: builder.query<
      PagedResponse<WorkoutSession>,
      { clientId: string; page?: number }
    >({
      query: ({ clientId, page = 0 }) =>
        `/api/workouts/history?userId=${clientId}&page=${page}`,
      providesTags: ['Workout'],
    }),
  }),
});

export const {
  useGetTrainerClientsQuery,
  useGetClientProgressQuery,
  useGetClientWorkoutHistoryQuery,
} = trainerApi;
