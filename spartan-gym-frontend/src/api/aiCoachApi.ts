import { baseApi } from './baseApi';
import type {
  TrainingPlan,
  Exercise,
  ExerciseRecommendation,
  OvertrainingResult,
} from '@/types';

export const aiCoachApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    generatePlan: builder.mutation<
      TrainingPlan,
      {
        fitnessLevel: string;
        goals: string[];
        medicalConditions?: string[];
        availableEquipment?: string[];
      }
    >({
      query: (data) => ({
        url: '/api/ai/plans/generate',
        method: 'POST',
        body: data,
      }),
      invalidatesTags: ['TrainingPlan'],
    }),
    checkOvertraining: builder.query<OvertrainingResult, void>({
      query: () => '/api/ai/overtraining/check',
    }),
    getAlternatives: builder.query<
      Exercise[],
      { exerciseId: string; availableEquipment?: string[] }
    >({
      query: (params) => ({ url: '/api/ai/alternatives', params }),
    }),
    getWarmup: builder.query<Exercise[], { routineId: string }>({
      query: ({ routineId }) =>
        `/api/ai/warmup?routineId=${routineId}`,
    }),
  }),
});

export const {
  useGeneratePlanMutation,
  useCheckOvertrainingQuery,
  useGetAlternativesQuery,
  useGetWarmupQuery,
} = aiCoachApi;
