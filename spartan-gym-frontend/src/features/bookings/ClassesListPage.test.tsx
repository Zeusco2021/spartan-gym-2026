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
import ClassesListPage from './ClassesListPage';
import type { GroupClass } from '@/types/bookings';

vi.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string, params?: Record<string, string>) => {
      const translations: Record<string, string> = {
        classesTitle: 'Group Classes',
        filterType: 'Type',
        filterDifficulty: 'Difficulty',
        all: 'All',
        beginner: 'Beginner',
        intermediate: 'Intermediate',
        advanced: 'Advanced',
        loading: 'Loading',
        loadError: 'Failed to load classes',
        noClasses: 'No classes available',
        capacity: 'Capacity',
        reserve: 'Reserve',
        joinWaitlist: 'Join Waitlist',
        cancelReservation: 'Cancel',
        waitlist: 'Waitlist',
        confirmReserve: 'Confirm Reservation',
        confirmCancel: 'Confirm Cancellation',
        confirmReserveMsg: `Reserve ${params?.name ?? ''}?`,
        confirmCancelMsg: `Cancel reservation for ${params?.name ?? ''}?`,
        dismiss: 'Dismiss',
        confirm: 'Confirm',
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

const mockClasses: GroupClass[] = [
  {
    id: 'c1',
    gymId: 'g1',
    instructorId: 'i1',
    instructorName: 'Maria Lopez',
    name: 'Morning Yoga',
    room: 'Room A',
    maxCapacity: 20,
    currentCapacity: 15,
    difficultyLevel: 'beginner',
    scheduledAt: '2025-01-15T08:00:00Z',
    durationMinutes: 60,
  },
  {
    id: 'c2',
    gymId: 'g1',
    instructorId: 'i2',
    instructorName: 'Carlos Ruiz',
    name: 'HIIT Blast',
    room: 'Room B',
    maxCapacity: 15,
    currentCapacity: 15,
    difficultyLevel: 'advanced',
    scheduledAt: '2025-01-15T10:00:00Z',
    durationMinutes: 45,
  },
];

const mockReserveTrigger = vi.fn();
const mockReserveUnwrap = vi.fn();
const mockCancelTrigger = vi.fn();
const mockCancelUnwrap = vi.fn();

let mockQueryResult: {
  data: { content: GroupClass[] } | undefined;
  isLoading: boolean;
  isError: boolean;
};

vi.mock('@/api/bookingsApi', () => ({
  useGetClassesQuery: () => mockQueryResult,
  useReserveClassMutation: () => [
    (...args: unknown[]) => {
      mockReserveTrigger(...args);
      return { unwrap: mockReserveUnwrap };
    },
    { isLoading: false },
  ],
  useCancelReservationMutation: () => [
    (...args: unknown[]) => {
      mockCancelTrigger(...args);
      return { unwrap: mockCancelUnwrap };
    },
    { isLoading: false },
  ],
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
        <ClassesListPage />
      </MemoryRouter>
    </Provider>,
  );
  return store;
}

describe('ClassesListPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockReserveUnwrap.mockResolvedValue({});
    mockCancelUnwrap.mockResolvedValue({});
    mockQueryResult = {
      data: { content: mockClasses },
      isLoading: false,
      isError: false,
    };
  });

  it('renders the heading and class cards', () => {
    renderPage();
    expect(screen.getByText('Group Classes')).toBeInTheDocument();
    expect(screen.getByText('Morning Yoga')).toBeInTheDocument();
    expect(screen.getByText('HIIT Blast')).toBeInTheDocument();
  });

  it('displays instructor name and room', () => {
    renderPage();
    expect(screen.getByText(/Maria Lopez/)).toBeInTheDocument();
    expect(screen.getByText(/Room A/)).toBeInTheDocument();
  });

  it('shows capacity information', () => {
    renderPage();
    expect(screen.getByText('Capacity: 15/20')).toBeInTheDocument();
    expect(screen.getByText('Capacity: 15/15')).toBeInTheDocument();
  });

  it('shows waitlist chip when class is full', () => {
    renderPage();
    expect(screen.getByText('Waitlist')).toBeInTheDocument();
  });

  it('shows Join Waitlist button for full classes', () => {
    renderPage();
    expect(screen.getByRole('button', { name: 'Join Waitlist: HIIT Blast' })).toBeInTheDocument();
  });

  it('shows Reserve button for available classes', () => {
    renderPage();
    expect(screen.getByRole('button', { name: 'Reserve: Morning Yoga' })).toBeInTheDocument();
  });

  it('opens confirmation dialog and reserves class', async () => {
    const user = userEvent.setup();
    renderPage();

    await user.click(screen.getByRole('button', { name: 'Reserve: Morning Yoga' }));
    expect(screen.getByText('Confirm Reservation')).toBeInTheDocument();
    expect(screen.getByText('Reserve Morning Yoga?')).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'Confirm' }));

    await waitFor(() => {
      expect(mockReserveTrigger).toHaveBeenCalledWith('c1');
    });
  });

  it('opens cancel dialog and cancels reservation', async () => {
    const user = userEvent.setup();
    renderPage();

    const cancelButtons = screen.getAllByRole('button', { name: /Cancel:/ });
    await user.click(cancelButtons[0]);

    expect(screen.getByText('Confirm Cancellation')).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'Confirm' }));

    await waitFor(() => {
      expect(mockCancelTrigger).toHaveBeenCalledWith('c1');
    });
  });

  it('shows difficulty chips', () => {
    renderPage();
    expect(screen.getByText('Beginner')).toBeInTheDocument();
    expect(screen.getByText('Advanced')).toBeInTheDocument();
  });

  it('shows loading state', () => {
    mockQueryResult = { data: undefined, isLoading: true, isError: false };
    renderPage();
    expect(screen.getByRole('progressbar', { name: 'Loading' })).toBeInTheDocument();
  });

  it('shows error state', () => {
    mockQueryResult = { data: undefined, isLoading: false, isError: true };
    renderPage();
    expect(screen.getByText('Failed to load classes')).toBeInTheDocument();
  });

  it('shows empty state', () => {
    mockQueryResult = { data: { content: [] }, isLoading: false, isError: false };
    renderPage();
    expect(screen.getByText('No classes available')).toBeInTheDocument();
  });

  it('has accessible filter controls', () => {
    renderPage();
    expect(screen.getByRole('combobox', { name: /Type/ })).toBeInTheDocument();
    expect(screen.getByRole('combobox', { name: /Difficulty/ })).toBeInTheDocument();
  });
});
