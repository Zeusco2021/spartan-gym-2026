import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Provider } from 'react-redux';
import { configureStore } from '@reduxjs/toolkit';
import { MemoryRouter } from 'react-router-dom';
import authReducer from '@/features/auth/authSlice';
import uiReducer from '@/features/settings/uiSlice';
import websocketReducer from '@/features/websocket/websocketSlice';
import { baseApi } from '@/api/baseApi';
import TrainerPanelPage from './TrainerPanelPage';

vi.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string) => {
      const translations: Record<string, string> = {
        trainerPanel: 'Trainer Panel',
        loading: 'Loading',
        loadError: 'Failed to load',
        noClients: 'No clients assigned.',
        clientList: 'Client list',
        selectClient: 'Select client',
        selectClientPrompt: 'Select a client to view details.',
        activePlans: 'Active Plans',
        noActivePlans: 'No active plans.',
        aiGenerated: 'AI Generated',
        manual: 'Manual',
        routines: 'routines',
        progressMetrics: 'Progress Metrics',
        progressTabs: 'Progress tabs',
        volume: 'Volume',
        duration: 'Duration',
        calories: 'Calories',
        loadingProgress: 'Loading progress',
        noProgressData: 'No progress data available.',
        progressChart: 'Progress chart',
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

// Mock Victory Charts to avoid SVG rendering issues in jsdom
vi.mock('victory', () => ({
  VictoryChart: ({ children }: { children: React.ReactNode }) => <div data-testid="victory-chart">{children}</div>,
  VictoryLine: () => <div data-testid="victory-line" />,
  VictoryBar: () => <div data-testid="victory-bar" />,
  VictoryAxis: () => <div data-testid="victory-axis" />,
  VictoryTheme: { material: {} },
  VictoryTooltip: () => null,
}));

const mockClients = [
  { id: 'c1', name: 'Alice Johnson', email: 'alice@test.com', assignedAt: '2024-01-01T00:00:00Z' },
  { id: 'c2', name: 'Bob Smith', email: 'bob@test.com', profilePhotoUrl: 'https://example.com/bob.jpg', assignedAt: '2024-02-01T00:00:00Z' },
];

const mockPlans = {
  content: [
    { id: 'p1', userId: 'c1', name: 'Strength Plan', aiGenerated: true, status: 'active' as const, routines: [{ id: 'r1' }, { id: 'r2' }], createdAt: '2024-01-01' },
    { id: 'p2', userId: 'c1', name: 'Cardio Plan', aiGenerated: false, status: 'completed' as const, routines: [{ id: 'r3' }], createdAt: '2024-01-01' },
    { id: 'p3', userId: 'c2', name: 'Weight Loss', aiGenerated: true, status: 'active' as const, routines: [{ id: 'r4' }], createdAt: '2024-02-01' },
  ],
  page: 0,
  size: 100,
  totalElements: 3,
  totalPages: 1,
};

const mockProgress = {
  period: 'month',
  data: [
    { date: '2024-01-01', volume: 5000, duration: 60, caloriesBurned: 400 },
    { date: '2024-01-08', volume: 5500, duration: 65, caloriesBurned: 450 },
    { date: '2024-01-15', volume: 6000, duration: 70, caloriesBurned: 500 },
  ],
};

let mockClientsLoading = false;
let mockClientsError = false;
let mockPlansLoading = false;
let mockProgressLoading = false;

vi.mock('@/api/trainerApi', () => ({
  useGetTrainerClientsQuery: () => ({
    data: mockClientsLoading ? undefined : (mockClientsError ? undefined : mockClients),
    isLoading: mockClientsLoading,
    isError: mockClientsError,
  }),
  useGetClientProgressQuery: () => ({
    data: mockProgressLoading ? undefined : mockProgress,
    isLoading: mockProgressLoading,
    isError: false,
  }),
}));

vi.mock('@/api/trainingApi', () => ({
  useGetPlansQuery: () => ({
    data: mockPlansLoading ? undefined : mockPlans,
    isLoading: mockPlansLoading,
    isError: false,
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
  const store = createTestStore();
  render(
    <Provider store={store}>
      <MemoryRouter>
        <TrainerPanelPage />
      </MemoryRouter>
    </Provider>,
  );
  return store;
}

describe('TrainerPanelPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockClientsLoading = false;
    mockClientsError = false;
    mockPlansLoading = false;
    mockProgressLoading = false;
  });

  it('renders the trainer panel heading', () => {
    renderPage();
    expect(screen.getByText('Trainer Panel')).toBeInTheDocument();
  });

  it('displays client list with names', () => {
    renderPage();
    expect(screen.getByText('Alice Johnson')).toBeInTheDocument();
    expect(screen.getByText('Bob Smith')).toBeInTheDocument();
  });

  it('shows active plan count for each client', () => {
    renderPage();
    // Alice has 1 active plan, Bob has 1 active plan
    const chips = screen.getAllByText(/Active Plans/);
    expect(chips.length).toBeGreaterThanOrEqual(2);
  });

  it('shows select client prompt when no client is selected', () => {
    renderPage();
    expect(screen.getByText('Select a client to view details.')).toBeInTheDocument();
  });

  it('displays client details and active plans when a client is selected', async () => {
    const user = userEvent.setup();
    renderPage();

    await user.click(screen.getByLabelText('Select client: Alice Johnson'));

    await waitFor(() => {
      expect(screen.getAllByText('Alice Johnson')).toHaveLength(2); // list + detail header
      expect(screen.getByText('Strength Plan')).toBeInTheDocument();
    });
  });

  it('renders Victory Charts for progress metrics', async () => {
    const user = userEvent.setup();
    renderPage();

    await user.click(screen.getByLabelText('Select client: Alice Johnson'));

    await waitFor(() => {
      expect(screen.getByTestId('victory-chart')).toBeInTheDocument();
    });
  });

  it('has accessible client list', () => {
    renderPage();
    expect(screen.getByRole('list', { name: 'Client list' })).toBeInTheDocument();
  });

  it('switches progress metric tabs', async () => {
    const user = userEvent.setup();
    renderPage();

    await user.click(screen.getByLabelText('Select client: Alice Johnson'));

    await waitFor(() => {
      expect(screen.getByRole('tab', { name: 'Duration' })).toBeInTheDocument();
    });

    await user.click(screen.getByRole('tab', { name: 'Duration' }));
    // Tab should be selected
    expect(screen.getByRole('tab', { name: 'Duration' })).toHaveAttribute('aria-selected', 'true');
  });
});
