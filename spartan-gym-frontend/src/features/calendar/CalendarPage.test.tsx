import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { Provider } from 'react-redux';
import { configureStore } from '@reduxjs/toolkit';
import { MemoryRouter } from 'react-router-dom';
import authReducer from '@/features/auth/authSlice';
import uiReducer from '@/features/settings/uiSlice';
import websocketReducer from '@/features/websocket/websocketSlice';
import { baseApi } from '@/api/baseApi';
import CalendarPage from './CalendarPage';

vi.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string) => {
      const m: Record<string, string> = {
        calendarTitle: 'Calendar',
        viewMode: 'View mode',
        daily: 'Daily',
        weekly: 'Weekly',
        monthly: 'Monthly',
        previous: 'Previous',
        next: 'Next',
        event: 'Event',
        day: 'Day',
        noEvents: 'No events',
        error: 'Error',
        loading: 'Loading',
        workout: 'Workout',
        class: 'Class',
        trainer_session: 'Trainer Session',
        nutrition_reminder: 'Nutrition Reminder',
        custom: 'Custom',
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

let mockData: unknown = undefined;
let mockLoading = false;
let mockError = false;

vi.mock('@/api/calendarApi', () => ({
  calendarApi: { reducerPath: 'calendarApi', reducer: () => ({}) },
  useGetEventsQuery: () => ({
    data: mockData,
    isLoading: mockLoading,
    isError: mockError,
  }),
  useUpdateEventMutation: () => [vi.fn().mockResolvedValue({}), { isLoading: false }],
  useCreateEventMutation: () => [vi.fn()],
  useDeleteEventMutation: () => [vi.fn()],
  useSyncExternalCalendarMutation: () => [vi.fn()],
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
        <CalendarPage />
      </MemoryRouter>
    </Provider>,
  );
}

describe('CalendarPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockData = [];
    mockLoading = false;
    mockError = false;
  });

  it('renders the page title', () => {
    renderPage();
    expect(screen.getByText('Calendar')).toBeInTheDocument();
  });

  it('renders view mode toggle buttons', () => {
    renderPage();
    expect(screen.getByRole('button', { name: 'Daily' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Weekly' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Monthly' })).toBeInTheDocument();
  });

  it('renders navigation buttons with aria-labels', () => {
    renderPage();
    expect(screen.getByRole('button', { name: 'Previous' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Next' })).toBeInTheDocument();
  });

  it('renders weekday headers in weekly view', () => {
    renderPage();
    expect(screen.getByText('Sun')).toBeInTheDocument();
    expect(screen.getByText('Mon')).toBeInTheDocument();
    expect(screen.getByText('Sat')).toBeInTheDocument();
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

  it('renders events on the calendar', () => {
    const today = new Date();
    mockData = [
      {
        id: 'e1',
        userId: 'u1',
        eventType: 'workout',
        title: 'Leg Day',
        startsAt: today.toISOString(),
        endsAt: new Date(today.getTime() + 3600000).toISOString(),
        reminderMinutes: 30,
      },
    ];
    renderPage();
    expect(screen.getByText('Leg Day')).toBeInTheDocument();
  });

  it('has aria-label on event chips', () => {
    const today = new Date();
    mockData = [
      {
        id: 'e1',
        userId: 'u1',
        eventType: 'workout',
        title: 'Leg Day',
        startsAt: today.toISOString(),
        endsAt: new Date(today.getTime() + 3600000).toISOString(),
        reminderMinutes: 30,
      },
    ];
    renderPage();
    expect(screen.getByLabelText(/event leg day/i)).toBeInTheDocument();
  });
});
