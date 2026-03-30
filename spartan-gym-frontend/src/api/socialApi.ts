import { baseApi } from './baseApi';
import type {
  Challenge,
  Achievement,
  RankingEntry,
  SocialGroup,
  PagedResponse,
} from '@/types';

export const socialApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    getChallenges: builder.query<
      PagedResponse<Challenge>,
      { type?: string; active?: boolean }
    >({
      query: (params) => ({ url: '/api/social/challenges', params }),
      providesTags: ['Challenge'],
    }),
    getAchievements: builder.query<Achievement[], void>({
      query: () => '/api/social/achievements',
      providesTags: ['Achievement'],
    }),
    getRankings: builder.query<RankingEntry[], { category: string }>({
      query: ({ category }) =>
        `/api/social/rankings?category=${category}`,
      providesTags: ['Ranking'],
    }),
    shareAchievement: builder.mutation<{ shareUrl: string }, string>({
      query: (achievementId) => ({
        url: '/api/social/share',
        method: 'POST',
        body: { achievementId },
      }),
    }),
    getGroups: builder.query<PagedResponse<SocialGroup>, void>({
      query: () => '/api/social/groups',
    }),
  }),
});

export const {
  useGetChallengesQuery,
  useGetAchievementsQuery,
  useGetRankingsQuery,
  useShareAchievementMutation,
  useGetGroupsQuery,
} = socialApi;
