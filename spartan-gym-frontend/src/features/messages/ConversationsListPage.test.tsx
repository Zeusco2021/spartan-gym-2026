import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { Provider } from 'react-redux';
import { configureStore } from '@reduxjs/toolkit';
import { MemoryRouter } from 'react-router-dom';
import authReducer from '@/features/auth/authSlice';
import uiReducer from '@/features/settings/uiSlice';
import websocketReducer from '@/features/websocket/websocketSlice';
import { baseApi } from '@/api/baseApi';
import ConversationsListPage from './ConversationsListPage';

vi.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string) => {
      const m: Record<string, string> = {
        conversations: 'Conversations',
        conversation: 'Conversation',
        unread: 'unread',
        unknown: 'Unknown',
        noConversations: 'No conversations',
        error: 'Error',
        loading: 'Loading',
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
  socketService: { connect: vi.fn(), disconnect: vi.fn() },
}));

const mockConversations = [
  {
    id: 'conv1',
    participantIds: ['u1', 'u2'],
    type: 'direct' as const,
    lastMessageAt: '2025-01-15T10:00:00Z',
    unreadCount: 3,
    participants: [
      { userId: 'u2', name: 'Alice', online: true },
    ],
  },
  {
    id: 'conv2',
    participantIds: ['u1', 'u2', 'u3'],
    type: 'group' as const,
    lastMessageAt: '2025-01-14T10:00:00Z',
    unreadCount: 0,
    participants: [
      { userId: 'u2', name: 'Bob', online: false },
      { userId: 'u3', name: 'Charlie', online: true },
    ],
  },
];

let mockData: unknown = undefined;
let mockLoading = false;
let mockError = false;

vi.mock('@/api/messagesApi', () => ({
  useGetConversationsQuery: () => ({
    data: mockData,
    isLoading: mockLoading,
    isError: mockError,
  }),
}));

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

const mockOnSelect = vi.fn();

function renderPage() {
  render(
    <Provider store={createTestStore()}>
      <MemoryRouter>
        <ConversationsListPage onSelectConversation={mockOnSelect} />
      </MemoryRouter>
    </Provider>,
  );
}

describe('ConversationsListPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockData = mockConversations;
    mockLoading = false;
    mockError = false;
  });

  it('renders the title', () => {
    renderPage();
    expect(screen.getByText('Conversations')).toBeInTheDocument();
  });

  it('renders direct conversation with participant name', () => {
    renderPage();
    expect(screen.getByText('Alice')).toBeInTheDocument();
  });

  it('renders group conversation with participant names', () => {
    renderPage();
    expect(screen.getByText('Bob, Charlie')).toBeInTheDocument();
  });

  it('renders unread count badge', () => {
    renderPage();
    expect(screen.getByText('3')).toBeInTheDocument();
  });

  it('has aria-labels on conversation items', () => {
    renderPage();
    expect(screen.getByLabelText(/conversation alice.*3 unread/i)).toBeInTheDocument();
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

  it('shows empty state', () => {
    mockData = [];
    renderPage();
    expect(screen.getByText('No conversations')).toBeInTheDocument();
  });
});
