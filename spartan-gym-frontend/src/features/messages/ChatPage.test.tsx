import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { Provider } from 'react-redux';
import { configureStore } from '@reduxjs/toolkit';
import { MemoryRouter } from 'react-router-dom';
import authReducer from '@/features/auth/authSlice';
import uiReducer from '@/features/settings/uiSlice';
import websocketReducer from '@/features/websocket/websocketSlice';
import { baseApi } from '@/api/baseApi';
import ChatPage from './ChatPage';
import type { Conversation } from '@/types';

vi.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string) => {
      const m: Record<string, string> = {
        typeMessage: 'Type a message',
        sendMessage: 'Send message',
        messageFrom: 'Message from',
        unknown: 'Unknown',
        read: 'Read',
        typing: 'Typing',
        loading: 'Loading',
        error: 'Error',
      };
      return m[key] ?? key;
    },
    i18n: { language: 'en' },
  }),
}));

vi.mock('socket.io-client', () => ({
  io: vi.fn(() => ({ on: vi.fn(), emit: vi.fn(), disconnect: vi.fn(), connected: false })),
}));
vi.mock('@/websocket/socketService', () => ({
  socketService: {
    connect: vi.fn(),
    disconnect: vi.fn(),
    sendMessage: vi.fn(),
    startTyping: vi.fn(),
    markAsRead: vi.fn(),
  },
}));

const mockMessages = {
  content: [
    {
      id: 'm1',
      conversationId: 'conv1',
      senderId: 'u2',
      content: 'Hello there!',
      contentType: 'text',
      status: 'read',
      sentAt: '2025-01-15T10:00:00Z',
    },
    {
      id: 'm2',
      conversationId: 'conv1',
      senderId: 'u1',
      content: 'Hi! How are you?',
      contentType: 'text',
      status: 'sent',
      sentAt: '2025-01-15T10:01:00Z',
    },
  ],
  totalElements: 2,
};

let mockData: unknown = undefined;
let mockLoading = false;
let mockError = false;

vi.mock('@/api/messagesApi', () => ({
  useGetMessagesQuery: () => ({
    data: mockData,
    isLoading: mockLoading,
    isError: mockError,
  }),
  useSendMessageMutation: () => [vi.fn().mockResolvedValue({})],
}));

const mockConversation: Conversation = {
  id: 'conv1',
  participantIds: ['u1', 'u2'],
  type: 'direct',
  lastMessageAt: '2025-01-15T10:01:00Z',
  unreadCount: 0,
  participants: [
    { userId: 'u2', name: 'Alice', online: true },
  ],
};

function createTestStore() {
  return configureStore({
    reducer: {
      auth: authReducer,
      ui: uiReducer,
      websocket: websocketReducer,
      [baseApi.reducerPath]: baseApi.reducer,
    },
    middleware: (gDM) => gDM().concat(baseApi.middleware),
  });
}

function renderPage() {
  render(
    <Provider store={createTestStore()}>
      <MemoryRouter>
        <ChatPage conversation={mockConversation} />
      </MemoryRouter>
    </Provider>,
  );
}

describe('ChatPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockData = mockMessages;
    mockLoading = false;
    mockError = false;
  });

  it('renders conversation header with participant name', () => {
    renderPage();
    expect(screen.getByText('Alice')).toBeInTheDocument();
  });

  it('renders messages', () => {
    renderPage();
    expect(screen.getByText('Hello there!')).toBeInTheDocument();
    expect(screen.getByText('Hi! How are you?')).toBeInTheDocument();
  });

  it('renders message input field', () => {
    renderPage();
    expect(screen.getByLabelText('Type a message')).toBeInTheDocument();
  });

  it('renders send button with aria-label', () => {
    renderPage();
    expect(screen.getByRole('button', { name: 'Send message' })).toBeInTheDocument();
  });

  it('shows read receipt for read messages', () => {
    renderPage();
    expect(screen.getByText('Read')).toBeInTheDocument();
  });

  it('shows loading state', () => {
    mockData = undefined;
    mockLoading = true;
    renderPage();
    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });

  it('shows error state', () => {
    mockData = undefined;
    mockError = true;
    renderPage();
    expect(screen.getByText('Error')).toBeInTheDocument();
  });
});
