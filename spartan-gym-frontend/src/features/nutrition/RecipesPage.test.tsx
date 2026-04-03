import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { Provider } from 'react-redux';
import { configureStore } from '@reduxjs/toolkit';
import { MemoryRouter } from 'react-router-dom';
import authReducer from '@/features/auth/authSlice';
import uiReducer from '@/features/settings/uiSlice';
import websocketReducer from '@/features/websocket/websocketSlice';
import { baseApi } from '@/api/baseApi';
import RecipesPage from './RecipesPage';

vi.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string) => {
      const translations: Record<string, string> = {
        recipesTitle: 'Recipes',
        recipesDescription: 'Browse healthy recipes',
        goalFilter: 'Filter by Goal',
        allGoals: 'All Goals',
        weight_loss: 'Weight Loss',
        muscle_gain: 'Muscle Gain',
        maintenance: 'Maintenance',
        kcal: 'kcal',
        grams: 'g',
        ingredients: 'Ingredients',
        instructions: 'Instructions',
        noRecipes: 'No recipes found',
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

const mockRecipes = {
  content: [
    {
      id: 'r1',
      name: 'Grilled Chicken Salad',
      description: 'A healthy salad with grilled chicken',
      calories: 350,
      proteinGrams: 35,
      carbsGrams: 15,
      fatGrams: 12,
      ingredients: ['Chicken breast', 'Mixed greens', 'Olive oil'],
      instructions: ['Grill the chicken', 'Toss with greens', 'Drizzle oil'],
      goal: 'weight_loss',
    },
  ],
  totalElements: 1,
};

let mockRecipesData: unknown = undefined;
let mockRecipesLoading = false;
let mockRecipesError = false;

vi.mock('@/api/nutritionApi', () => ({
  useGetRecipesQuery: () => ({
    data: mockRecipesData,
    isLoading: mockRecipesLoading,
    isError: mockRecipesError,
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
        <RecipesPage />
      </MemoryRouter>
    </Provider>,
  );
}

describe('RecipesPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockRecipesData = mockRecipes;
    mockRecipesLoading = false;
    mockRecipesError = false;
  });

  it('renders the page title and description', () => {
    renderPage();
    expect(screen.getByText('Recipes')).toBeInTheDocument();
    expect(screen.getByText('Browse healthy recipes')).toBeInTheDocument();
  });

  it('renders the goal filter dropdown', () => {
    renderPage();
    expect(screen.getByRole('combobox', { name: /filter by goal/i })).toBeInTheDocument();
  });

  it('renders recipe cards with nutritional info', () => {
    renderPage();
    expect(screen.getByText('Grilled Chicken Salad')).toBeInTheDocument();
    expect(screen.getByText('A healthy salad with grilled chicken')).toBeInTheDocument();
    expect(screen.getByText('350 kcal')).toBeInTheDocument();
    expect(screen.getByText('P: 35g')).toBeInTheDocument();
  });

  it('renders ingredients and instructions accordions', () => {
    renderPage();
    const ingredientButtons = screen.getAllByText('Ingredients');
    expect(ingredientButtons.length).toBeGreaterThan(0);
    const instructionButtons = screen.getAllByText('Instructions');
    expect(instructionButtons.length).toBeGreaterThan(0);
  });

  it('shows loading state', () => {
    mockRecipesData = undefined;
    mockRecipesLoading = true;
    renderPage();
    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });

  it('shows error state', () => {
    mockRecipesData = undefined;
    mockRecipesError = true;
    renderPage();
    expect(screen.getByText('Error')).toBeInTheDocument();
  });

  it('shows no recipes message when empty', () => {
    mockRecipesData = { content: [], totalElements: 0 };
    renderPage();
    expect(screen.getByText('No recipes found')).toBeInTheDocument();
  });

  it('has aria-labels on recipe cards', () => {
    renderPage();
    expect(screen.getByLabelText('Grilled Chicken Salad')).toBeInTheDocument();
  });
});
