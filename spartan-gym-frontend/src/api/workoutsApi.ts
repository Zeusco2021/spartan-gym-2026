import { baseApi } from './baseApi';
import type {
  WorkoutSession,
  WorkoutSet,
  WorkoutProgress,
  PagedResponse,
} from '@/types';

export const workoutsApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    startWorkout: builder.mutation<
      WorkoutSession,
      { planId?: string; routineId?: string }
    >({
      query: (data) => ({
        url: '/api/workouts/start',
        method: 'POST',
        body: data,
      }),
    }),
    addSet: builder.mutation<
      WorkoutSet,
      { sessionId: string; set: Omit<WorkoutSet, 'id' | 'timestamp'> }
    >({
      query: ({ sessionId, set }) => ({
        url: `/api/workouts/${sessionId}/sets`,
        method: 'POST',
        body: set,
      }),
    }),
    completeWorkout: builder.mutation<WorkoutSession, string>({
      query: (sessionId) => ({
        url: `/api/workouts/${sessionId}/complete`,
        method: 'POST',
      }),
      invalidatesTags: ['Workout'],
    }),
    getWorkoutHistory: builder.query<
      PagedResponse<WorkoutSession>,
      { page?: number; from?: string; to?: string }
    >({
      query: (params) => ({ url: '/api/workouts/history', params }),
      providesTags: ['Workout'],
    }),
    getProgress: builder.query<
      WorkoutProgress,
      { period: string; from?: string; to?: string }
    >({
      query: (params) => ({ url: '/api/workouts/progress', params }),
    }),
  }),
});

export const {
  useStartWorkoutMutation,
  useAddSetMutation,
  useCompleteWorkoutMutation,
  useGetWorkoutHistoryQuery,
  useGetProgressQuery,
} = workoutsApi;
