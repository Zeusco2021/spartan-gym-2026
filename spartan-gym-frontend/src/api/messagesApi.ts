import { baseApi } from './baseApi';
import type { Conversation, Message, PagedResponse } from '@/types';

export const messagesApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    getConversations: builder.query<Conversation[], void>({
      query: () => '/api/messages/conversations',
      providesTags: ['Conversation'],
    }),
    getMessages: builder.query<
      PagedResponse<Message>,
      { conversationId: string; page?: number }
    >({
      query: ({ conversationId, ...params }) => ({
        url: `/api/messages/conversations/${conversationId}`,
        params,
      }),
      providesTags: (_, __, { conversationId }) => [
        { type: 'Message', id: conversationId },
      ],
    }),
    sendMessage: builder.mutation<
      Message,
      { conversationId: string; content: string; contentType?: string }
    >({
      query: (data) => ({
        url: '/api/messages/send',
        method: 'POST',
        body: data,
      }),
      invalidatesTags: (_, __, { conversationId }) => [
        { type: 'Message', id: conversationId },
        'Conversation',
      ],
    }),
  }),
});

export const {
  useGetConversationsQuery,
  useGetMessagesQuery,
  useSendMessageMutation,
} = messagesApi;
