import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { Provider } from 'react-redux';
import { configureStore } from '@reduxjs/toolkit';
import { MemoryRouter } from 'react-router-dom';
import authReducer from '@/features/auth/authSlice';
import uiReducer from '@/features/settings/uiSlice';
import websocketReducer from '@/features/websocket/websocketSlice';
import { baseApi } from '@/api/baseApi';
import AchievementsPage from './AchievementsPage';

vi.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string) => {
      const m: Record<string, string> = {
        achievementsTitle: 'Achievements',
        share: 'Share',
        shareDialog: 'Share dialog',
        shareAchievement: 'Share Achievement',
        close: 'Close',
        noAchievements: 'No achievements',
        error: 'Error',
        loading: 'Loading',
        shareCard: 'Share card',
        earnedOn: 'Earned on',
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

const mockAchievements = [
  {
    id: 'a1',
    type: 'milestone',
    name: 'First Workout',
    description: 'Completed your first workout',
    earnedAt: '2025-01-15T10:00:00Z',
    iconUrl: '',
  },
  {
    id: 'a2',
    type: 'streak',
    name: '7-Day Streak',
    description: 'Worked out 7 days in a row',
    earnedAt: '2025-01-22T10:00:00Z',
    iconUrl: '',
  },
];

let mockData: unknown = undefined;
let mockLoading = false;
let mockError = false;

vi.mock('@/api/socialApi', () => ({
  useGetAchievementsQuery: () => ({
    data: mockData,
    isLoading: mockLoading,
    isError: mockError,
  }),
  useShareAchievementMutation: () => [vi.fn().mockResolvedValue({})],
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
        <AchievementsPage />
      </MemoryRouter>
    </Provider>,
  );
}

describe('AchievementsPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockData = mockAchievements;
    mockLoading = false;
    mockError = false;
  });

  it('renders the page title', () => {
    renderPage();
    expect(screen.getByText('Achievements')).toBeInTheDocument();
  });

  it('renders achievement cards', () => {
    renderPage();
    expect(screen.getByText('First Workout')).toBeInTheDocument();
    expect(screen.getByText('Completed your first workout')).toBeInTheDocument();
    expect(screen.getByText('7-Day Streak')).toBeInTheDocument();
  });

  it('renders share buttons with aria-labels', () => {
    renderPage();
    expect(screen.getByRole('button', { name: /share first workout/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /share 7-day streak/i })).toBeInTheDocument();
  });

  it('has aria-labels on achievement cards', () => {
    renderPage();
    expect(screen.getByLabelText('First Workout')).toBeInTheDocument();
    expect(screen.getByLabelText('7-Day Streak')).toBeInTheDocument();
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
    expect(screen.getByText('No achievements')).toBeInTheDocument();
  });
});
