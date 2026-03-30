import { baseApi } from './baseApi';
import type {
  User,
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  OnboardingState,
  OnboardingData,
} from '@/types';

export const usersApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    login: builder.mutation<LoginResponse, LoginRequest>({
      query: (credentials) => ({
        url: '/api/users/login',
        method: 'POST',
        body: credentials,
      }),
    }),
    register: builder.mutation<User, RegisterRequest>({
      query: (data) => ({
        url: '/api/users/register',
        method: 'POST',
        body: data,
      }),
    }),
    getProfile: builder.query<User, void>({
      query: () => '/api/users/profile',
      providesTags: ['User'],
    }),
    updateProfile: builder.mutation<User, Partial<User>>({
      query: (data) => ({
        url: '/api/users/profile',
        method: 'PUT',
        body: data,
      }),
      invalidatesTags: ['User'],
    }),
    setupMfa: builder.mutation<{ qrCodeUrl: string; secret: string }, void>({
      query: () => ({ url: '/api/users/mfa/setup', method: 'POST' }),
    }),
    verifyMfa: builder.mutation<
      LoginResponse,
      { code: string; sessionToken: string }
    >({
      query: (data) => ({
        url: '/api/users/mfa/verify',
        method: 'POST',
        body: data,
      }),
    }),
    getOnboarding: builder.query<OnboardingState, void>({
      query: () => '/api/users/onboarding',
    }),
    submitOnboarding: builder.mutation<User, OnboardingData>({
      query: (data) => ({
        url: '/api/users/onboarding',
        method: 'POST',
        body: data,
      }),
      invalidatesTags: ['User'],
    }),
    requestDataExport: builder.mutation<{ requestId: string }, void>({
      query: () => ({ url: '/api/users/data-export', method: 'POST' }),
    }),
    deleteAccount: builder.mutation<void, void>({
      query: () => ({ url: '/api/users/profile/delete', method: 'DELETE' }),
    }),
  }),
});

export const {
  useLoginMutation,
  useRegisterMutation,
  useGetProfileQuery,
  useUpdateProfileMutation,
  useSetupMfaMutation,
  useVerifyMfaMutation,
  useGetOnboardingQuery,
  useSubmitOnboardingMutation,
  useRequestDataExportMutation,
  useDeleteAccountMutation,
} = usersApi;
