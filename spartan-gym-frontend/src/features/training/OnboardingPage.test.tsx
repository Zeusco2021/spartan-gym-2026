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
import OnboardingPage from './OnboardingPage';
import type { TrainingPlan } from '@/types';

vi.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string) => {
      const translations: Record<string, string> = {
        'onboarding.title': 'Welcome to Spartan Gym',
        'onboarding.subtitle': 'Let\'s set up your personalized training plan.',
        'onboarding.optional': 'Optional',
        'onboarding.back': 'Back',
        'onboarding.next': 'Next',
        'onboarding.skip': 'Skip',
        'onboarding.complete': 'Generate My Plan',
        'onboarding.selectFitnessLevel': 'What is your current fitness level?',
        'onboarding.selectGoals': 'What are your training goals?',
        'onboarding.selectMedicalConditions': 'Do you have any medical conditions?',
        'onboarding.selectEquipment': 'What equipment do you have access to?',
        'onboarding.planGenerated': 'Your Plan is Ready!',
        'onboarding.generateError': 'Failed to generate your plan.',
        'onboarding.steps.fitnessLevel': 'Fitness Level',
        'onboarding.steps.goals': 'Goals',
        'onboarding.steps.medicalConditions': 'Medical Conditions',
        'onboarding.steps.equipment': 'Equipment',
        'onboarding.goals.weight_loss': 'Weight Loss',
        'onboarding.goals.muscle_gain': 'Muscle Gain',
        'onboarding.goals.endurance': 'Endurance',
        'onboarding.goals.flexibility': 'Flexibility',
        'onboarding.goals.general_fitness': 'General Fitness',
        'onboarding.medical.back_pain': 'Back Pain',
        'onboarding.medical.knee_injury': 'Knee Injury',
        'onboarding.medical.heart_condition': 'Heart Condition',
        'onboarding.medical.asthma': 'Asthma',
        'onboarding.medical.diabetes': 'Diabetes',
        'onboarding.medical.hypertension': 'Hypertension',
        'onboarding.equipment.dumbbells': 'Dumbbells',
        'onboarding.equipment.barbell': 'Barbell',
        'onboarding.equipment.pull_up_bar': 'Pull-up Bar',
        'onboarding.equipment.resistance_bands': 'Resistance Bands',
        'onboarding.equipment.treadmill': 'Treadmill',
        'onboarding.equipment.stationary_bike': 'Stationary Bike',
        'onboarding.equipment.kettlebell': 'Kettlebell',
        'onboarding.equipment.bench': 'Bench',
        beginner: 'Beginner',
        intermediate: 'Intermediate',
        advanced: 'Advanced',
        aiGenerated: 'AI Generated',
        routines: 'Routines',
        exercises: 'Exercises',
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

const mockGeneratedPlan: TrainingPlan = {
  id: 'plan-1',
  userId: 'u1',
  name: 'AI Beginner Plan',
  description: 'A personalized plan for beginners',
  aiGenerated: true,
  status: 'active',
  routines: [
    {
      id: 'r1',
      planId: 'plan-1',
      name: 'Day 1 - Upper Body',
      sortOrder: 0,
      exercises: [
        {
          id: 're1',
          exercise: {
            id: 'e1',
            name: 'Push-ups',
            muscleGroups: ['chest'],
            difficulty: 'beginner',
          },
          sets: 3,
          reps: '10',
          restSeconds: 60,
          sortOrder: 0,
        },
      ],
    },
  ],
  createdAt: '2024-01-01T00:00:00Z',
};

const mockSubmitOnboarding = vi.fn();
const mockGeneratePlan = vi.fn();

vi.mock('@/api/usersApi', () => ({
  useSubmitOnboardingMutation: () => [
    (data: unknown) => ({ unwrap: () => mockSubmitOnboarding(data) }),
    { isLoading: false },
  ],
}));

vi.mock('@/api/aiCoachApi', () => ({
  useGeneratePlanMutation: () => [
    (data: unknown) => ({ unwrap: () => mockGeneratePlan(data) }),
    { isLoading: false, error: null },
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
    middleware: (getDefaultMiddleware) =>
      getDefaultMiddleware().concat(baseApi.middleware),
  });
}

function renderOnboardingPage() {
  const store = createTestStore();
  render(
    <Provider store={store}>
      <MemoryRouter>
        <OnboardingPage />
      </MemoryRouter>
    </Provider>,
  );
  return store;
}

describe('OnboardingPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockSubmitOnboarding.mockResolvedValue({});
    mockGeneratePlan.mockResolvedValue(mockGeneratedPlan);
  });

  it('renders the onboarding title and stepper', () => {
    renderOnboardingPage();

    expect(screen.getByText('Welcome to Spartan Gym')).toBeInTheDocument();
    expect(screen.getByText('Fitness Level')).toBeInTheDocument();
    expect(screen.getByText('Goals')).toBeInTheDocument();
    expect(screen.getByText('Medical Conditions')).toBeInTheDocument();
    expect(screen.getByText('Equipment')).toBeInTheDocument();
  });

  it('shows fitness level options on step 1', () => {
    renderOnboardingPage();

    expect(screen.getByText('What is your current fitness level?')).toBeInTheDocument();
    expect(screen.getByLabelText('Beginner')).toBeInTheDocument();
    expect(screen.getByLabelText('Intermediate')).toBeInTheDocument();
    expect(screen.getByLabelText('Advanced')).toBeInTheDocument();
  });

  it('disables Next button until fitness level is selected', () => {
    renderOnboardingPage();

    const nextButton = screen.getByRole('button', { name: 'Next' });
    expect(nextButton).toBeDisabled();
  });

  it('enables Next button after selecting fitness level', async () => {
    const user = userEvent.setup();
    renderOnboardingPage();

    await user.click(screen.getByLabelText('Beginner'));

    const nextButton = screen.getByRole('button', { name: 'Next' });
    expect(nextButton).toBeEnabled();
  });

  it('navigates to goals step when Next is clicked', async () => {
    const user = userEvent.setup();
    renderOnboardingPage();

    await user.click(screen.getByLabelText('Beginner'));
    await user.click(screen.getByRole('button', { name: 'Next' }));

    expect(screen.getByText('What are your training goals?')).toBeInTheDocument();
    expect(screen.getByLabelText('Weight Loss')).toBeInTheDocument();
  });

  it('allows selecting multiple goals', async () => {
    const user = userEvent.setup();
    renderOnboardingPage();

    await user.click(screen.getByLabelText('Beginner'));
    await user.click(screen.getByRole('button', { name: 'Next' }));

    await user.click(screen.getByLabelText('Weight Loss'));
    await user.click(screen.getByLabelText('Muscle Gain'));

    expect(screen.getByLabelText('Weight Loss')).toBeChecked();
    expect(screen.getByLabelText('Muscle Gain')).toBeChecked();
  });

  it('shows Skip button on optional steps (medical conditions)', async () => {
    const user = userEvent.setup();
    renderOnboardingPage();

    await user.click(screen.getByLabelText('Beginner'));
    await user.click(screen.getByRole('button', { name: 'Next' }));
    await user.click(screen.getByLabelText('Weight Loss'));
    await user.click(screen.getByRole('button', { name: 'Next' }));

    expect(screen.getByText('Do you have any medical conditions?')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Skip' })).toBeInTheDocument();
  });

  it('allows skipping optional steps', async () => {
    const user = userEvent.setup();
    renderOnboardingPage();

    await user.click(screen.getByLabelText('Beginner'));
    await user.click(screen.getByRole('button', { name: 'Next' }));
    await user.click(screen.getByLabelText('Weight Loss'));
    await user.click(screen.getByRole('button', { name: 'Next' }));
    await user.click(screen.getByRole('button', { name: 'Skip' }));

    expect(screen.getByText('What equipment do you have access to?')).toBeInTheDocument();
  });

  it('navigates back to previous step', async () => {
    const user = userEvent.setup();
    renderOnboardingPage();

    await user.click(screen.getByLabelText('Beginner'));
    await user.click(screen.getByRole('button', { name: 'Next' }));

    expect(screen.getByText('What are your training goals?')).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'Back' }));

    expect(screen.getByText('What is your current fitness level?')).toBeInTheDocument();
  });

  it('shows Generate My Plan button on last step', async () => {
    const user = userEvent.setup();
    renderOnboardingPage();

    await user.click(screen.getByLabelText('Beginner'));
    await user.click(screen.getByRole('button', { name: 'Next' }));
    await user.click(screen.getByLabelText('Weight Loss'));
    await user.click(screen.getByRole('button', { name: 'Next' }));
    await user.click(screen.getByRole('button', { name: 'Skip' }));

    expect(screen.getByRole('button', { name: 'Generate My Plan' })).toBeInTheDocument();
  });

  it('calls submitOnboarding and generatePlan on completion', async () => {
    const user = userEvent.setup();
    renderOnboardingPage();

    await user.click(screen.getByLabelText('Beginner'));
    await user.click(screen.getByRole('button', { name: 'Next' }));
    await user.click(screen.getByLabelText('Weight Loss'));
    await user.click(screen.getByRole('button', { name: 'Next' }));
    await user.click(screen.getByRole('button', { name: 'Skip' }));
    await user.click(screen.getByRole('button', { name: 'Generate My Plan' }));

    await waitFor(() => {
      expect(mockSubmitOnboarding).toHaveBeenCalledWith({
        fitnessLevel: 'beginner',
        goals: ['weight_loss'],
        medicalConditions: undefined,
        availableEquipment: undefined,
      });
    });

    await waitFor(() => {
      expect(mockGeneratePlan).toHaveBeenCalledWith({
        fitnessLevel: 'beginner',
        goals: ['weight_loss'],
        medicalConditions: undefined,
        availableEquipment: undefined,
      });
    });
  });

  it('displays generated plan after completion', async () => {
    const user = userEvent.setup();
    renderOnboardingPage();

    await user.click(screen.getByLabelText('Beginner'));
    await user.click(screen.getByRole('button', { name: 'Next' }));
    await user.click(screen.getByLabelText('Weight Loss'));
    await user.click(screen.getByRole('button', { name: 'Next' }));
    await user.click(screen.getByRole('button', { name: 'Skip' }));
    await user.click(screen.getByRole('button', { name: 'Generate My Plan' }));

    await waitFor(() => {
      expect(screen.getByText('Your Plan is Ready!')).toBeInTheDocument();
      expect(screen.getByText('AI Beginner Plan')).toBeInTheDocument();
      expect(screen.getByText('A personalized plan for beginners')).toBeInTheDocument();
      expect(screen.getByText('Day 1 - Upper Body')).toBeInTheDocument();
    });
  });

  it('includes medical conditions and equipment when selected', async () => {
    const user = userEvent.setup();
    renderOnboardingPage();

    await user.click(screen.getByLabelText('Advanced'));
    await user.click(screen.getByRole('button', { name: 'Next' }));
    await user.click(screen.getByLabelText('Muscle Gain'));
    await user.click(screen.getByRole('button', { name: 'Next' }));
    await user.click(screen.getByLabelText('Back Pain'));
    await user.click(screen.getByRole('button', { name: 'Next' }));
    await user.click(screen.getByLabelText('Dumbbells'));
    await user.click(screen.getByLabelText('Barbell'));
    await user.click(screen.getByRole('button', { name: 'Generate My Plan' }));

    await waitFor(() => {
      expect(mockSubmitOnboarding).toHaveBeenCalledWith({
        fitnessLevel: 'advanced',
        goals: ['muscle_gain'],
        medicalConditions: ['back_pain'],
        availableEquipment: ['dumbbells', 'barbell'],
      });
    });
  });
});
