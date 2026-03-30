import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { Provider } from 'react-redux';
import { configureStore } from '@reduxjs/toolkit';
import { MemoryRouter } from 'react-router-dom';
import authReducer from '@/features/auth/authSlice';
import uiReducer from '@/features/settings/uiSlice';
import websocketReducer from '@/features/websocket/websocketSlice';
import { baseApi } from '@/api/baseApi';
import WorkoutLivePage from './WorkoutLivePage';
import type { OvertrainingResult } from '@/types';

vi.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string, params?: Record<string, unknown>) => {
      const translations: Record<string, string> = {
        workout: 'Workout',
        noActiveWorkout: 'No active workout. Start a new session to begin tracking.',
        startWorkoutSession: 'Start Workout Session',
        overtrainingWarning: 'Overtraining Warning',
        overtrainingMessage: `Signs of fatigue detected. We recommend ${params?.days ?? ''} rest day(s). ${params?.recommendation ?? ''}`,
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

let mockOvertrainingData: OvertrainingResult | undefined;

vi.mock('@/api/aiCoachApi', () => ({
  useCheckOvertrainingQuery: () => ({
    data: mockOvertrainingData,
    isLoading: false,
    isError: false,
  }),
}));

vi.mock('@/api/workoutsApi', () => ({
  useStartWorkoutMutation: () => [
    vi.fn().mockReturnValue({ unwrap: () => Promise.resolve({ id: 's1' }) }),
    { isLoading: false },
  ],
  useAddSetMutation: () => [vi.fn(), { isLoading: false }],
  useCompleteWorkoutMutation: () => [vi.fn(), { isLoading: false }],
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

function renderPage() {
  const store = createTestStore();
  render(
    <Provider store={store}>
      <MemoryRouter>
        <WorkoutLivePage />
      </MemoryRouter>
    </Provider>,
  );
  return store;
}

describe('WorkoutLivePage - Overtraining Check', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockOvertrainingData = undefined;
  });

  it('does not show overtraining warning when no data', () => {
    renderPage();

    expect(screen.queryByText('Overtraining Warning')).not.toBeInTheDocument();
    expect(screen.getByText('Start Workout Session')).toBeInTheDocument();
  });

  it('does not show overtraining warning when isOvertraining is false', () => {
    mockOvertrainingData = {
      isOvertraining: false,
      fatigueScore: 20,
      recommendation: 'Keep going!',
      suggestedRestDays: 0,
    };
    renderPage();

    expect(screen.queryByText('Overtraining Warning')).not.toBeInTheDocument();
  });

  it('shows overtraining warning when isOvertraining is true', () => {
    mockOvertrainingData = {
      isOvertraining: true,
      fatigueScore: 85,
      recommendation: 'Take a break to recover.',
      suggestedRestDays: 2,
    };
    renderPage();

    expect(screen.getByText('Overtraining Warning')).toBeInTheDocument();
    expect(
      screen.getByText('Signs of fatigue detected. We recommend 2 rest day(s). Take a break to recover.'),
    ).toBeInTheDocument();
  });

  it('still shows start workout button when overtraining is detected', () => {
    mockOvertrainingData = {
      isOvertraining: true,
      fatigueScore: 90,
      recommendation: 'Rest recommended.',
      suggestedRestDays: 3,
    };
    renderPage();

    expect(screen.getByText('Start Workout Session')).toBeInTheDocument();
    expect(screen.getByText('Start Workout Session').closest('button')).not.toBeDisabled();
  });
});
