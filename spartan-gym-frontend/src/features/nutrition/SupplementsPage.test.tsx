import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { Provider } from 'react-redux';
import { configureStore } from '@reduxjs/toolkit';
import { MemoryRouter } from 'react-router-dom';
import authReducer from '@/features/auth/authSlice';
import uiReducer from '@/features/settings/uiSlice';
import websocketReducer from '@/features/websocket/websocketSlice';
import { baseApi } from '@/api/baseApi';
import SupplementsPage from './SupplementsPage';

vi.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string) => {
      const translations: Record<string, string> = {
        supplementsTitle: 'Supplements',
        supplementsDescription: 'Browse supplements info',
        dosage: 'Dosage',
        benefits: 'Benefits',
        noSupplements: 'No supplements found',
        error: 'Error',
        loading: 'Loading',
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

const mockSupplements = [
  {
    id: 's1',
    name: 'Whey Protein',
    description: 'High quality protein supplement',
    dosage: '30g per serving',
    benefits: ['Muscle recovery', 'Protein intake'],
    category: 'Protein',
  },
  {
    id: 's2',
    name: 'Creatine Monohydrate',
    description: 'Strength and power supplement',
    dosage: '5g daily',
    benefits: ['Strength', 'Power output'],
    category: 'Performance',
  },
];

let mockSupplementsData: unknown = undefined;
let mockSupplementsLoading = false;
let mockSupplementsError = false;

vi.mock('@/api/nutritionApi', () => ({
  useGetSupplementsQuery: () => ({
    data: mockSupplementsData,
    isLoading: mockSupplementsLoading,
    isError: mockSupplementsError,
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
        <SupplementsPage />
      </MemoryRouter>
    </Provider>,
  );
}

describe('SupplementsPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockSupplementsData = mockSupplements;
    mockSupplementsLoading = false;
    mockSupplementsError = false;
  });

  it('renders the page title and description', () => {
    renderPage();
    expect(screen.getByText('Supplements')).toBeInTheDocument();
    expect(screen.getByText('Browse supplements info')).toBeInTheDocument();
  });

  it('renders supplement cards with details', () => {
    renderPage();
    expect(screen.getByText('Whey Protein')).toBeInTheDocument();
    expect(screen.getByText('High quality protein supplement')).toBeInTheDocument();
    expect(screen.getByText('Dosage: 30g per serving')).toBeInTheDocument();
    expect(screen.getByText('Creatine Monohydrate')).toBeInTheDocument();
  });

  it('renders supplement categories as chips', () => {
    renderPage();
    expect(screen.getByText('Protein')).toBeInTheDocument();
    expect(screen.getByText('Performance')).toBeInTheDocument();
  });

  it('renders benefit chips', () => {
    renderPage();
    expect(screen.getByText('Muscle recovery')).toBeInTheDocument();
    expect(screen.getByText('Protein intake')).toBeInTheDocument();
    expect(screen.getByText('Strength')).toBeInTheDocument();
  });

  it('shows loading state', () => {
    mockSupplementsData = undefined;
    mockSupplementsLoading = true;
    renderPage();
    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });

  it('shows error state', () => {
    mockSupplementsData = undefined;
    mockSupplementsError = true;
    renderPage();
    expect(screen.getByText('Error')).toBeInTheDocument();
  });

  it('shows no supplements message when empty', () => {
    mockSupplementsData = [];
    renderPage();
    expect(screen.getByText('No supplements found')).toBeInTheDocument();
  });

  it('has aria-labels on supplement cards', () => {
    renderPage();
    expect(screen.getByLabelText('Whey Protein')).toBeInTheDocument();
    expect(screen.getByLabelText('Creatine Monohydrate')).toBeInTheDocument();
  });
});
