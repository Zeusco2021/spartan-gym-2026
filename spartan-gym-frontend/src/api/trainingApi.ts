import { baseApi } from './baseApi';
import type { TrainingPlan, Exercise, PagedResponse } from '@/types';

export const trainingApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    getPlans: builder.query<
      PagedResponse<TrainingPlan>,
      { page?: number; size?: number }
    >({
      query: ({ page = 0, size = 10 }) =>
        `/api/training/plans?page=${page}&size=${size}`,
      providesTags: (result) =>
        result
          ? [
              ...result.content.map(({ id }) => ({
                type: 'TrainingPlan' as const,
                id,
              })),
              'TrainingPlan',
            ]
          : ['TrainingPlan'],
    }),
    getPlanById: builder.query<TrainingPlan, string>({
      query: (id) => `/api/training/plans/${id}`,
      providesTags: (_, __, id) => [{ type: 'TrainingPlan', id }],
    }),
    createPlan: builder.mutation<TrainingPlan, Partial<TrainingPlan>>({
      query: (data) => ({
        url: '/api/training/plans',
        method: 'POST',
        body: data,
      }),
      invalidatesTags: ['TrainingPlan'],
    }),
    updatePlan: builder.mutation<
      TrainingPlan,
      { id: string; data: Partial<TrainingPlan> }
    >({
      query: ({ id, data }) => ({
        url: `/api/training/plans/${id}`,
        method: 'PUT',
        body: data,
      }),
      invalidatesTags: (_, __, { id }) => [{ type: 'TrainingPlan', id }],
    }),
    assignPlan: builder.mutation<void, { planId: string; clientId: string }>({
      query: ({ planId, ...body }) => ({
        url: `/api/training/plans/${planId}/assign`,
        method: 'POST',
        body,
      }),
      invalidatesTags: ['TrainingPlan'],
    }),
    getExercises: builder.query<
      PagedResponse<Exercise>,
      { muscleGroup?: string; difficulty?: string }
    >({
      query: (params) => ({ url: '/api/training/exercises', params }),
    }),
  }),
});

export const {
  useGetPlansQuery,
  useGetPlanByIdQuery,
  useCreatePlanMutation,
  useUpdatePlanMutation,
  useAssignPlanMutation,
  useGetExercisesQuery,
} = trainingApi;
