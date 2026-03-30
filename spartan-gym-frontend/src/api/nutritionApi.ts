import { baseApi } from './baseApi';
import type {
  NutritionPlan,
  MealLog,
  DailyBalance,
  Food,
  Recipe,
  Supplement,
  PagedResponse,
} from '@/types';

export const nutritionApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    getNutritionPlan: builder.query<NutritionPlan, void>({
      query: () => '/api/nutrition/plans',
      providesTags: ['NutritionPlan'],
    }),
    logMeal: builder.mutation<
      MealLog,
      { foodId: string; quantityGrams: number; mealType: string }
    >({
      query: (data) => ({
        url: '/api/nutrition/meals',
        method: 'POST',
        body: data,
      }),
      invalidatesTags: ['MealLog'],
    }),
    getDailyBalance: builder.query<DailyBalance, { date?: string }>({
      query: ({ date } = {}) =>
        `/api/nutrition/daily-balance${date ? `?date=${date}` : ''}`,
      providesTags: ['MealLog'],
    }),
    searchFoods: builder.query<
      PagedResponse<Food>,
      { query: string; region?: string }
    >({
      query: (params) => ({ url: '/api/nutrition/foods', params }),
    }),
    getRecipes: builder.query<PagedResponse<Recipe>, { goal?: string }>({
      query: (params) => ({ url: '/api/nutrition/recipes', params }),
    }),
    getSupplements: builder.query<Supplement[], void>({
      query: () => '/api/nutrition/supplements',
    }),
  }),
});

export const {
  useGetNutritionPlanQuery,
  useLogMealMutation,
  useGetDailyBalanceQuery,
  useSearchFoodsQuery,
  useGetRecipesQuery,
  useGetSupplementsQuery,
} = nutritionApi;
