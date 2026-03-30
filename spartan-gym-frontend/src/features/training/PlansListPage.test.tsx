import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Provider } from 'react-redux';
import { configureStore } from '@reduxjs/toolkit';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import authReducer from '@/features/auth/authSlice';
import uiReducer from '@/features/settings/uiSlice';
import websocketReducer from '@/features/websocket/websocketSlice';
import { baseApi } from '@/api/baseApi';
import PlansListPage from './PlansListPage';
import type { TrainingPlan, PagedResponse } from '@/types';

vi.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string) => {
      const translations: Record<string, string> = {
        plans: 'Training Plans',
        allPlans: 'All',
        active: 'Active',
        completed: 'Completed',
        paused: 'Paused',
        noPlans: 'No training plans found',
        filterByStatus: 'Filter by status',
        viewPlan: 'View Plan',
        createdAt: 'Created',
        aiGenerated: 'AI Generated',
        status: 'Status',
        error: 'Failed to load training plans',
        loading: 'Loading...',
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
  socketService: {
    connect: vi.fn(),
    disconnect: vi.fn(),
  },
}));

const mockPlans: TrainingPlan[] = [
  {
    id: '1',
    userId: 'u1',
    name: 'Strength Plan',
    description: 'Build muscle',
    aiGenerated: false,
    status: 'active',
    routines: [],
    createdAt: '2024-01-15T10:00:00Z',
  },
  {
    id: '2',
    userId: 'u1',
    name: 'Cardio Plan',
    description: 'Improve endurance',
    aiGenerated: true,
    status: 'completed',
    routines: [],
    createdAt: '2024-02-20T10:00:00Z',
  },
  {
    id: '3',
    userId: 'u1',
    name: 'Recovery Plan',
    aiGenerated: false,
    status: 'paused',
    routines: [],
    createdAt: '2024-03-10T10:00:00Z',
  },
];

const mockPagedResponse: PagedResponse<TrainingPlan> = {
  content: mockPlans,
  page: 0,
  size: 10,
  totalElements: 3,
  totalPages: 1,
};

let mockData: PagedResponse<TrainingPlan> | undefined = mockPagedResponse;
let mockIsLoading = false;
let mockIsError = false;

vi.mock('@/api/trainingApi', () => ({
  useGetPlansQuery: () => ({
    data: mockData,
    isLoading: mockIsLoading,
    isError: mockIsError,
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
    middleware: (getDefaultMiddleware) =>
      getDefaultMiddleware().concat(baseApi.middleware),
  });
}

function renderPlansListPage() {
  const store = createTestStore();
  render(
    <Provider store={store}>
      <MemoryRouter initialEntries={['/training/plans']}>
        <Routes>
          <Route path="/training/plans" element={<PlansListPage />} />
          <Route path="/training/plans/:id" element={<div>Plan Detail</div>} />
        </Routes>
      </MemoryRouter>
    </Provider>,
  );
  return store;
}

describe('PlansListPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockData = mockPagedResponse;
    mockIsLoading = false;
    mockIsError = false;
  });

  it('renders the page title and filter tabs', () => {
    renderPlansListPage();

    expect(screen.getByText('Training Plans')).toBeInTheDocument();
    expect(screen.getByRole('tab', { name: 'All' })).toBeInTheDocument();
    expect(screen.getByRole('tab', { name: 'Active' })).toBeInTheDocument();
    expect(screen.getByRole('tab', { name: 'Completed' })).toBeInTheDocument();
    expect(screen.getByRole('tab', { name: 'Paused' })).toBeInTheDocument();
  });

  it('renders all plans when "All" tab is selected', () => {
    renderPlansListPage();

    expect(screen.getByText('Strength Plan')).toBeInTheDocument();
    expect(screen.getByText('Cardio Plan')).toBeInTheDocument();
    expect(screen.getByText('Recovery Plan')).toBeInTheDocument();
  });

  it('shows plan descriptions and status chips', () => {
    renderPlansListPage();

    expect(screen.getByText('Build muscle')).toBeInTheDocument();
    expect(screen.getByText('Improve endurance')).toBeInTheDocument();
    // Status text appears in both tabs and chips, so use getAllByText
    expect(screen.getAllByText('Active').length).toBeGreaterThanOrEqual(2);
    expect(screen.getAllByText('Completed').length).toBeGreaterThanOrEqual(2);
    expect(screen.getAllByText('Paused').length).toBeGreaterThanOrEqual(2);
  });

  it('shows AI Generated chip for AI-generated plans', () => {
    renderPlansListPage();

    expect(screen.getByText('AI Generated')).toBeInTheDocument();
  });

  it('filters plans by status when clicking tabs', async () => {
    const user = userEvent.setup();
    renderPlansListPage();

    await user.click(screen.getByRole('tab', { name: 'Active' }));

    await waitFor(() => {
      expect(screen.getByText('Strength Plan')).toBeInTheDocument();
      expect(screen.queryByText('Cardio Plan')).not.toBeInTheDocument();
      expect(screen.queryByText('Recovery Plan')).not.toBeInTheDocument();
    });
  });

  it('shows loading spinner when loading', () => {
    mockData = undefined;
    mockIsLoading = true;
    renderPlansListPage();

    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });

  it('shows error alert when fetch fails', () => {
    mockData = undefined;
    mockIsError = true;
    renderPlansListPage();

    expect(screen.getByText('Failed to load training plans')).toBeInTheDocument();
  });

  it('shows empty state when no plans match filter', async () => {
    mockData = { content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 };
    renderPlansListPage();

    expect(screen.getByText('No training plans found')).toBeInTheDocument();
  });

  it('renders View Plan buttons that link to plan detail', () => {
    renderPlansListPage();

    const viewButtons = screen.getAllByRole('button', { name: 'View Plan' });
    expect(viewButtons).toHaveLength(3);
  });

  it('navigates to plan detail when View Plan is clicked', async () => {
    const user = userEvent.setup();
    renderPlansListPage();

    const viewButtons = screen.getAllByRole('button', { name: 'View Plan' });
    await user.click(viewButtons[0]);

    await waitFor(() => {
      expect(screen.getByText('Plan Detail')).toBeInTheDocument();
    });
  });
});
