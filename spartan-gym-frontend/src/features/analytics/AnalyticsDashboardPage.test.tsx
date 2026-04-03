import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { Provider } from 'react-redux';
import { configureStore } from '@reduxjs/toolkit';
import { MemoryRouter } from 'react-router-dom';
import authReducer from '@/features/auth/authSlice';
import uiReducer from '@/features/settings/uiSlice';
import websocketReducer from '@/features/websocket/websocketSlice';
import { baseApi } from '@/api/baseApi';
import AnalyticsDashboardPage from './AnalyticsDashboardPage';
import type { DashboardMetrics } from '@/types/analytics';

vi.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string) => {
      const translations: Record<string, string> = {
        dashboardTitle: 'Analytics Dashboard',
        loading: 'Loading',
        loadError: 'Failed to load analytics',
        totalUsers: 'Total Users',
        retentionRate: 'Retention Rate',
        monthlyRevenue: 'Monthly Revenue',
        workoutsThisMonth: 'Workouts This Month',
        revenueChart: 'Revenue',
        retentionChart: 'Retention',
        occupancyChart: 'Gym Occupancy',
        revenue: 'Revenue',
        occupied: 'Occupied',
        available: 'Available',
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

// Mock Recharts to avoid SVG rendering issues in jsdom
vi.mock('recharts', () => ({
  ResponsiveContainer: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="responsive-container">{children}</div>
  ),
  BarChart: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="bar-chart">{children}</div>
  ),
  Bar: () => <div data-testid="bar" />,
  LineChart: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="line-chart">{children}</div>
  ),
  Line: () => <div data-testid="line" />,
  PieChart: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="pie-chart">{children}</div>
  ),
  Pie: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="pie">{children}</div>
  ),
  Cell: () => <div data-testid="cell" />,
  XAxis: () => null,
  YAxis: () => null,
  CartesianGrid: () => null,
  Tooltip: () => null,
  Legend: () => null,
}));

const mockMetrics: DashboardMetrics = {
  totalUsers: 12500,
  activeSubscriptions: 8400,
  monthlyRevenue: 53000,
  averageOccupancy: 72,
  retentionRate: 87,
  workoutsThisMonth: 45000,
};

let mockQueryResult: {
  data: DashboardMetrics | undefined;
  isLoading: boolean;
  isError: boolean;
};

vi.mock('@/api/analyticsApi', () => ({
  useGetDashboardMetricsQuery: () => mockQueryResult,
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
  const store = createTestStore();
  render(
    <Provider store={store}>
      <MemoryRouter>
        <AnalyticsDashboardPage />
      </MemoryRouter>
    </Provider>,
  );
  return store;
}

describe('AnalyticsDashboardPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockQueryResult = {
      data: mockMetrics,
      isLoading: false,
      isError: false,
    };
  });

  it('renders the dashboard title', () => {
    renderPage();
    expect(screen.getByText('Analytics Dashboard')).toBeInTheDocument();
  });

  it('displays metric cards with formatted values', () => {
    renderPage();
    expect(screen.getByText('Total Users')).toBeInTheDocument();
    expect(screen.getByText('12,500')).toBeInTheDocument();
    expect(screen.getByText('Retention Rate')).toBeInTheDocument();
    expect(screen.getByText('87%')).toBeInTheDocument();
    expect(screen.getByText('Monthly Revenue')).toBeInTheDocument();
    expect(screen.getByText('$53,000')).toBeInTheDocument();
    expect(screen.getByText('Workouts This Month')).toBeInTheDocument();
    expect(screen.getByText('45,000')).toBeInTheDocument();
  });

  it('renders chart sections', () => {
    renderPage();
    expect(screen.getByText('Revenue')).toBeInTheDocument();
    expect(screen.getByText('Retention')).toBeInTheDocument();
    expect(screen.getByText('Gym Occupancy')).toBeInTheDocument();
  });

  it('renders Recharts components', () => {
    renderPage();
    expect(screen.getByTestId('bar-chart')).toBeInTheDocument();
    expect(screen.getByTestId('line-chart')).toBeInTheDocument();
    expect(screen.getByTestId('pie-chart')).toBeInTheDocument();
  });

  it('shows loading state', () => {
    mockQueryResult = { data: undefined, isLoading: true, isError: false };
    renderPage();
    expect(screen.getByRole('progressbar', { name: 'Loading' })).toBeInTheDocument();
  });

  it('shows error state', () => {
    mockQueryResult = { data: undefined, isLoading: false, isError: true };
    renderPage();
    expect(screen.getByText('Failed to load analytics')).toBeInTheDocument();
  });

  it('has accessible chart containers with aria-labels', () => {
    renderPage();
    expect(screen.getByLabelText('Revenue')).toBeInTheDocument();
    expect(screen.getByLabelText('Retention')).toBeInTheDocument();
    expect(screen.getByLabelText('Gym Occupancy')).toBeInTheDocument();
  });

  it('has accessible metric values', () => {
    renderPage();
    expect(screen.getByLabelText('Total Users: 12,500')).toBeInTheDocument();
    expect(screen.getByLabelText('Retention Rate: 87%')).toBeInTheDocument();
  });
});
