import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { Provider } from 'react-redux';
import { configureStore } from '@reduxjs/toolkit';
import { MemoryRouter } from 'react-router-dom';
import authReducer from '@/features/auth/authSlice';
import uiReducer from '@/features/settings/uiSlice';
import websocketReducer from '@/features/websocket/websocketSlice';
import { baseApi } from '@/api/baseApi';
import ChallengesPage from './ChallengesPage';

vi.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string) => {
      const m: Record<string, string> = {
        challengesTitle: 'Challenges',
        filterChallenges: 'Filter challenges',
        all: 'All',
        weekly: 'Weekly',
        monthly: 'Monthly',
        target: 'Target',
        participants: 'Participants',
        progress: 'Progress',
        joinChallenge: 'Join',
        noChallenges: 'No challenges',
        error: 'Error',
        loading: 'Loading',
        distance: 'Distance',
        weight_lifted: 'Weight Lifted',
        workouts_completed: 'Workouts Completed',
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

const mockChallenges = {
  content: [
    {
      id: 'c1',
      name: '10K Steps Challenge',
      description: 'Walk 10,000 steps daily',
      type: 'weekly',
      metric: 'distance',
      targetValue: 70000,
      startDate: '2025-01-01',
      endDate: '2025-01-07',
      participantCount: 42,
    },
  ],
  totalElements: 1,
};

let mockData: unknown = undefined;
let mockLoading = false;
let mockError = false;

vi.mock('@/api/socialApi', () => ({
  useGetChallengesQuery: () => ({
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

function renderPage() {
  render(
    <Provider store={createTestStore()}>
      <MemoryRouter>
        <ChallengesPage />
      </MemoryRouter>
    </Provider>,
  );
}

describe('ChallengesPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockData = mockChallenges;
    mockLoading = false;
    mockError = false;
  });

  it('renders the page title', () => {
    renderPage();
    expect(screen.getByText('Challenges')).toBeInTheDocument();
  });

  it('renders filter toggle buttons', () => {
    renderPage();
    expect(screen.getByRole('button', { name: 'All' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Weekly' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Monthly' })).toBeInTheDocument();
  });

  it('renders challenge cards with details', () => {
    renderPage();
    expect(screen.getByText('10K Steps Challenge')).toBeInTheDocument();
    expect(screen.getByText('Walk 10,000 steps daily')).toBeInTheDocument();
    expect(screen.getByText('Target: 70000')).toBeInTheDocument();
    expect(screen.getByText('Participants: 42')).toBeInTheDocument();
  });

  it('renders join button with aria-label', () => {
    renderPage();
    expect(screen.getByRole('button', { name: /join.*10k steps/i })).toBeInTheDocument();
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
    mockData = { content: [], totalElements: 0 };
    renderPage();
    expect(screen.getByText('No challenges')).toBeInTheDocument();
  });

  it('has aria-label on challenge cards', () => {
    renderPage();
    expect(screen.getByLabelText('10K Steps Challenge')).toBeInTheDocument();
  });
});
