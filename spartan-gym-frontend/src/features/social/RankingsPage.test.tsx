import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { Provider } from 'react-redux';
import { configureStore } from '@reduxjs/toolkit';
import { MemoryRouter } from 'react-router-dom';
import authReducer from '@/features/auth/authSlice';
import uiReducer from '@/features/settings/uiSlice';
import websocketReducer from '@/features/websocket/websocketSlice';
import { baseApi } from '@/api/baseApi';
import RankingsPage from './RankingsPage';

vi.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string) => {
      const m: Record<string, string> = {
        rankingsTitle: 'Rankings',
        rankingCategory: 'Ranking category',
        strength: 'Strength',
        endurance: 'Endurance',
        consistency: 'Consistency',
        nutrition: 'Nutrition',
        rank: 'Rank',
        score: 'Score',
        noRankings: 'No rankings',
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
  socketService: {
    connect: vi.fn(),
    disconnect: vi.fn(),
    joinRanking: vi.fn(),
    leaveRanking: vi.fn(),
  },
}));

const mockRankings = [
  { userId: 'u1', userName: 'Alice', score: 1500, rank: 1 },
  { userId: 'u2', userName: 'Bob', score: 1200, rank: 2 },
  { userId: 'u3', userName: 'Charlie', score: 900, rank: 3 },
];

let mockData: unknown = undefined;
let mockLoading = false;
let mockError = false;

vi.mock('@/api/socialApi', () => ({
  useGetRankingsQuery: () => ({
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
        <RankingsPage />
      </MemoryRouter>
    </Provider>,
  );
}

describe('RankingsPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockData = mockRankings;
    mockLoading = false;
    mockError = false;
  });

  it('renders the page title', () => {
    renderPage();
    expect(screen.getByText('Rankings')).toBeInTheDocument();
  });

  it('renders category toggle buttons', () => {
    renderPage();
    expect(screen.getByRole('button', { name: 'Strength' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Endurance' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Consistency' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Nutrition' })).toBeInTheDocument();
  });

  it('renders ranking entries with names and scores', () => {
    renderPage();
    expect(screen.getByText('Alice')).toBeInTheDocument();
    expect(screen.getByText('Score: 1500')).toBeInTheDocument();
    expect(screen.getByText('Bob')).toBeInTheDocument();
    expect(screen.getByText('Charlie')).toBeInTheDocument();
  });

  it('renders rank chips', () => {
    renderPage();
    expect(screen.getByText('#1')).toBeInTheDocument();
    expect(screen.getByText('#2')).toBeInTheDocument();
    expect(screen.getByText('#3')).toBeInTheDocument();
  });

  it('has aria-labels on ranking entries', () => {
    renderPage();
    expect(screen.getByLabelText('Rank 1 - Alice')).toBeInTheDocument();
    expect(screen.getByLabelText('Rank 2 - Bob')).toBeInTheDocument();
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
    expect(screen.getByText('No rankings')).toBeInTheDocument();
  });
});
