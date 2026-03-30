import { baseApi } from './baseApi';
import type { DashboardMetrics, AnalyticsReport, PagedResponse } from '@/types';

export const analyticsApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    getDashboardMetrics: builder.query<
      DashboardMetrics,
      { gymId?: string; period?: string }
    >({
      query: (params) => ({ url: '/api/analytics/dashboard', params }),
      providesTags: ['Analytics'],
    }),
    getReports: builder.query<
      PagedResponse<AnalyticsReport>,
      { type?: string }
    >({
      query: (params) => ({ url: '/api/analytics/reports', params }),
    }),
    getMetrics: builder.query<
      Record<string, number>,
      { metric: string; from: string; to: string }
    >({
      query: (params) => ({ url: '/api/analytics/metrics', params }),
    }),
  }),
});

export const {
  useGetDashboardMetricsQuery,
  useGetReportsQuery,
  useGetMetricsQuery,
} = analyticsApi;
