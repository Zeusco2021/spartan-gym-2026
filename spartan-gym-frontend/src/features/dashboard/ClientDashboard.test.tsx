import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { Provider } from 'react-redux';
import { configureStore } from '@reduxjs/toolkit';
import { MemoryRouter } from 'react-router-dom';
import authReducer from '@/features/auth/authSlice';
import uiReducer from '@/features/settings/uiSlice';
import websocketReducer from '@/features/websocket/websocketSlice';
import { baseApi } from '@/api/baseApi';
import ClientDashboard from './ClientDashboard';

vi.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string) => {
      const translations: Record<string, string> = {
        trainingSummary: 'Training Summary',
        upcomingClasses: 'Upcoming Classes',
        nutritionBalance: 'Nutrition Balance',
        recentAchievements: 'Recent Achievements',
        progressChart: 'Progress Chart',
        workouts: 'Workouts',
        noData: 'No data',
        error: 'Error',
        loading: 'Loading',
        min: 'min',
        kcal: 'kcal',
        calories: 'Calories',
        protein: 'Protein',
        carbs: 'Carbs',
        fat: 'Fat',
        selectPeriod: 'Select period',
        day: 'Day',
        month: 'Month',
        year: 'Year',
        volume: 'Volume',
        duration: 'Duration',
      };
      return translations[key] ?? key;
    },
    i18n: { language: 'en' },
  }),
}));

vi.mock('socket.io-client', () => ({
  io: vi.fn(() => ({
    on: vi.fn(),
    emit: vi.fn(),
    disconnect: vi.fn(),
    connected: false,
  })),
}));

vi.mock('@/websocket/socketService', () => ({
  socketService: { connect: vi.fn(), disconnect: vi.fn() },
}));

// Mock Recharts
vi.mock('recharts', () => ({
  ResponsiveContainer: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="responsive-container">{children}</div>
  ),
  LineChart: ({ children, ...props }: Record<string, unknown>) => (
    <div data-testid="line-chart" role="img" aria-label={props['aria-label'] as string}>
      {children as React.ReactNode}
    </div>
  ),
  Line: () => <div />,
  XAxis: () => <div />,
  YAxis: () => <div />,
  CartesianGrid: () => <div />,
  Tooltip: () => <div />,
  Legend: () => <div />,
}));

let mockWorkoutData: unknown = undefined;
let mockWorkoutsLoading = false;
let mockWorkoutsError = false;

let mockClassesData: unknown = undefined;
let mockClassesLoading = false;
let mockClassesError = false;

let mockBalanceData: unknown = undefined;
let mockBalanceLoading = false;
let mockBalanceError = false;

let mockAchievements: unknown = undefined;
let mockAchievementsLoading = false;
let mockAchievementsError = false;

let mockProgressData: unknown = undefined;
let mockProgressLoading = false;
let mockProgressError = false;

vi.mock('@/api/workoutsApi', () => ({
  useGetWorkoutHistoryQuery: () => ({
    data: mockWorkoutData,
    isLoading: mockWorkoutsLoading,
    isError: mockWorkoutsError,
  }),
  useGetProgressQuery: () => ({
    data: mockProgressData,
    isLoading: mockProgressLoading,
    isError: mockProgressError,
  }),
}));

vi.mock('@/api/bookingsApi', () => ({
  useGetClassesQuery: () => ({
    data: mockClassesData,
    isLoading: mockClassesLoading,
    isError: mockClassesError,
  }),
}));

vi.mock('@/api/nutritionApi', () => ({
  useGetDailyBalanceQuery: () => ({
    data: mockBalanceData,
    isLoading: mockBalanceLoading,
    isError: mockBalanceError,
  }),
}));

vi.mock('@/api/socialApi', () => ({
  useGetAchievementsQuery: () => ({
    data: mockAchievements,
    isLoading: mockAchievementsLoading,
    isError: mockAchievementsError,
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

function renderDashboard() {
  render(
    <Provider store={createTestStore()}>
      <MemoryRouter>
        <ClientDashboard />
      </MemoryRouter>
    </Provider>,
  );
}

describe('ClientDashboard', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockWorkoutData = {
      content: [
        { id: '1', startedAt: '2024-01-01T10:00:00Z', totalDuration: 60, caloriesBurned: 400 },
      ],
      totalElements: 1,
    };
    mockWorkoutsLoading = false;
    mockWorkoutsError = false;
    mockClassesData = { content: [] };
    mockClassesLoading = false;
    mockClassesError = false;
    mockBalanceData = {
      totalCalories: 1500,
      targetCalories: 2000,
      totalProtein: 100,
      targetProtein: 150,
      totalCarbs: 200,
      targetCarbs: 250,
      totalFat: 50,
      targetFat: 70,
    };
    mockBalanceLoading = false;
    mockBalanceError = false;
    mockAchievements = [];
    mockAchievementsLoading = false;
    mockAchievementsError = false;
    mockProgressData = {
      period: 'month',
      data: [
        { date: '2024-01-01', volume: 5000, duration: 60, caloriesBurned: 400 },
      ],
    };
    mockProgressLoading = false;
    mockProgressError = false;
  });

  it('renders all dashboard sections', () => {
    renderDashboard();

    expect(screen.getByText('Training Summary')).toBeInTheDocument();
    expect(screen.getByText('Upcoming Classes')).toBeInTheDocument();
    expect(screen.getByText('Nutrition Balance')).toBeInTheDocument();
    expect(screen.getByText('Recent Achievements')).toBeInTheDocument();
    expect(screen.getByText('Progress Chart')).toBeInTheDocument();
  });

  it('renders progress chart with period selector', () => {
    renderDashboard();

    expect(screen.getByRole('group', { name: 'Select period' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Day' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Month' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Year' })).toBeInTheDocument();
  });

  it('renders the Recharts line chart', () => {
    renderDashboard();

    expect(screen.getByTestId('line-chart')).toBeInTheDocument();
  });

  it('shows loading state for progress chart', () => {
    mockProgressData = undefined;
    mockProgressLoading = true;
    renderDashboard();

    // Multiple loading spinners may exist; at least one for progress
    const spinners = screen.getAllByRole('progressbar');
    expect(spinners.length).toBeGreaterThanOrEqual(1);
  });

  it('shows error state for progress chart', () => {
    mockProgressData = undefined;
    mockProgressError = true;
    renderDashboard();

    const errors = screen.getAllByText('Error');
    expect(errors.length).toBeGreaterThanOrEqual(1);
  });

  it('renders nutrition balance with progress bars', () => {
    renderDashboard();

    expect(screen.getByText('Calories')).toBeInTheDocument();
    expect(screen.getByText('Protein')).toBeInTheDocument();
    expect(screen.getByText('Carbs')).toBeInTheDocument();
    expect(screen.getByText('Fat')).toBeInTheDocument();
    expect(screen.getByText('1500 / 2000')).toBeInTheDocument();
  });
});
