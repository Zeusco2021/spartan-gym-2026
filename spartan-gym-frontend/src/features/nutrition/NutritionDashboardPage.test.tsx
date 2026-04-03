import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { Provider } from 'react-redux';
import { configureStore } from '@reduxjs/toolkit';
import { MemoryRouter } from 'react-router-dom';
import authReducer from '@/features/auth/authSlice';
import uiReducer from '@/features/settings/uiSlice';
import websocketReducer from '@/features/websocket/websocketSlice';
import { baseApi } from '@/api/baseApi';
import NutritionDashboardPage from './NutritionDashboardPage';

vi.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string) => {
      const translations: Record<string, string> = {
        nutritionDashboard: 'Nutrition Dashboard',
        macroBreakdown: 'Macro Breakdown',
        dailyBalance: 'Daily Balance',
        calories: 'Calories',
        protein: 'Protein',
        carbs: 'Carbs',
        fat: 'Fat',
        kcal: 'kcal',
        grams: 'g',
        remaining: 'Remaining',
        noMeals: 'No meals logged today',
        error: 'Error loading data',
        loading: 'Loading',
        weight_loss: 'Weight Loss',
        muscle_gain: 'Muscle Gain',
        maintenance: 'Maintenance',
      };
      return translations[key] ?? key;
    },
    i18n: { language: 'en' },
  }),
}));

vi.mock('socket.io-client', () => ({
  io: vi.fn(() => ({
    on: vi.fn(), emit: vi.fn(), disconnect: vi.fn(), connected: false,
  })),
}));

vi.mock('@/websocket/socketService', () => ({
  socketService: { connect: vi.fn(), disconnect: vi.fn() },
}));

vi.mock('recharts', () => ({
  ResponsiveContainer: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="responsive-container">{children}</div>
  ),
  PieChart: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="pie-chart">{children}</div>
  ),
  Pie: () => <div data-testid="pie" />,
  Cell: () => <div />,
  Tooltip: () => <div />,
  Legend: () => <div />,
}));

let mockBalanceData: unknown = undefined;
let mockBalanceLoading = false;
let mockBalanceError = false;
let mockPlanData: unknown = undefined;

vi.mock('@/api/nutritionApi', () => ({
  useGetDailyBalanceQuery: () => ({
    data: mockBalanceData,
    isLoading: mockBalanceLoading,
    isError: mockBalanceError,
  }),
  useGetNutritionPlanQuery: () => ({
    data: mockPlanData,
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
        <NutritionDashboardPage />
      </MemoryRouter>
    </Provider>,
  );
}

describe('NutritionDashboardPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockBalanceData = {
      date: '2024-01-15',
      totalCalories: 1500,
      totalProtein: 100,
      totalCarbs: 200,
      totalFat: 50,
      targetCalories: 2000,
      targetProtein: 150,
      targetCarbs: 250,
      targetFat: 70,
    };
    mockBalanceLoading = false;
    mockBalanceError = false;
    mockPlanData = { id: '1', goal: 'muscle_gain', dailyCalories: 2000 };
  });

  it('renders the page title', () => {
    renderPage();
    expect(screen.getByText('Nutrition Dashboard')).toBeInTheDocument();
  });

  it('renders macro breakdown chart and daily balance card', () => {
    renderPage();
    expect(screen.getByText('Macro Breakdown')).toBeInTheDocument();
    expect(screen.getByText('Daily Balance')).toBeInTheDocument();
  });

  it('renders progress bars for each macro', () => {
    renderPage();
    expect(screen.getByText('Calories')).toBeInTheDocument();
    expect(screen.getByText('Protein')).toBeInTheDocument();
    expect(screen.getByText('Carbs')).toBeInTheDocument();
    expect(screen.getByText('Fat')).toBeInTheDocument();
  });

  it('shows consumed vs target values', () => {
    renderPage();
    expect(screen.getByText('1500 / 2000 kcal')).toBeInTheDocument();
    expect(screen.getByText('100 / 150 g')).toBeInTheDocument();
  });

  it('shows loading state', () => {
    mockBalanceData = undefined;
    mockBalanceLoading = true;
    renderPage();
    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });

  it('shows error state', () => {
    mockBalanceData = undefined;
    mockBalanceError = true;
    renderPage();
    expect(screen.getByText('Error loading data')).toBeInTheDocument();
  });

  it('shows no meals message when all macros are zero', () => {
    mockBalanceData = {
      date: '2024-01-15',
      totalCalories: 0, totalProtein: 0, totalCarbs: 0, totalFat: 0,
      targetCalories: 2000, targetProtein: 150, targetCarbs: 250, targetFat: 70,
    };
    renderPage();
    expect(screen.getByText('No meals logged today')).toBeInTheDocument();
  });

  it('renders pie chart when macros have values', () => {
    renderPage();
    expect(screen.getByTestId('pie-chart')).toBeInTheDocument();
  });

  it('displays the nutrition plan goal chip', () => {
    renderPage();
    expect(screen.getByText('Muscle Gain')).toBeInTheDocument();
  });
});
