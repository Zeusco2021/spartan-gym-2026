import { baseApi } from './baseApi';
import type {
  Subscription,
  Transaction,
  Donation,
  PaymentMethod,
  PagedResponse,
} from '@/types';

export const paymentsApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    getSubscription: builder.query<Subscription, void>({
      query: () => '/api/payments/subscribe',
      providesTags: ['Subscription'],
    }),
    subscribe: builder.mutation<
      Subscription,
      { planType: string; paymentMethodId: string }
    >({
      query: (data) => ({
        url: '/api/payments/subscribe',
        method: 'POST',
        body: data,
      }),
      invalidatesTags: ['Subscription'],
    }),
    getTransactions: builder.query<
      PagedResponse<Transaction>,
      { page?: number }
    >({
      query: (params) => ({ url: '/api/payments/transactions', params }),
      providesTags: ['Transaction'],
    }),
    requestRefund: builder.mutation<
      Transaction,
      { transactionId: string; reason: string }
    >({
      query: (data) => ({
        url: '/api/payments/refund',
        method: 'POST',
        body: data,
      }),
      invalidatesTags: ['Transaction', 'Subscription'],
    }),
    donate: builder.mutation<
      Donation,
      {
        creatorId: string;
        amount: number;
        currency: string;
        message?: string;
      }
    >({
      query: (data) => ({
        url: '/api/payments/donations',
        method: 'POST',
        body: data,
      }),
    }),
    getPaymentMethods: builder.query<PaymentMethod[], void>({
      query: () => '/api/payments/methods',
    }),
    addPaymentMethod: builder.mutation<
      PaymentMethod,
      { token: string; provider: string }
    >({
      query: (data) => ({
        url: '/api/payments/methods',
        method: 'POST',
        body: data,
      }),
    }),
    removePaymentMethod: builder.mutation<void, string>({
      query: (id) => ({
        url: `/api/payments/methods/${id}`,
        method: 'DELETE',
      }),
    }),
  }),
});

export const {
  useGetSubscriptionQuery,
  useSubscribeMutation,
  useGetTransactionsQuery,
  useRequestRefundMutation,
  useDonateMutation,
  useGetPaymentMethodsQuery,
  useAddPaymentMethodMutation,
  useRemovePaymentMethodMutation,
} = paymentsApi;
