import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { Provider } from 'react-redux';
import { configureStore } from '@reduxjs/toolkit';
import { MemoryRouter } from 'react-router-dom';
import authReducer from '@/features/auth/authSlice';
import uiReducer from '@/features/settings/uiSlice';
import websocketReducer from '@/features/websocket/websocketSlice';
import { baseApi } from '@/api/baseApi';
import MealLogPage from './MealLogPage';
vi.mock('react-i18next', () => ({ useTranslation: () => ({ t: (key: string) => { const m: Record<string, string> = { logMeal: 'Log Meal', searchFoods: 'Search Foods', quantity: 'Quantity', mealType: 'Meal Type', addMeal: 'Add Meal', lunch: 'Lunch', kcal: 'kcal', grams: 'g', protein: 'Protein', carbs: 'Carbs', fat: 'Fat', foodInfo: 'per 100g', error: 'Error', loading: 'Loading', foodResults: 'Food Results', selectFood: 'Select' }; return m[key] ?? key; }, i18n: { language: 'en' } }) }));
vi.mock('socket.io-client', () => ({ io: vi.fn(() => ({ on: vi.fn(), emit: vi.fn(), disconnect: vi.fn(), connected: false })) }));
vi.mock('@/websocket/socketService', () => ({ socketService: { connect: vi.fn(), disconnect: vi.fn() } }));
vi.mock('@/api/nutritionApi', () => ({ useSearchFoodsQuery: () => ({ data: undefined, isLoading: false }), useLogMealMutation: () => [() => ({ unwrap: vi.fn().mockResolvedValue({}) }), { isLoading: false, isError: false }] }));
function createTestStore() { return configureStore({ reducer: { auth: authReducer, ui: uiReducer, websocket: websocketReducer, [baseApi.reducerPath]: baseApi.reducer }, middleware: (gDM) => gDM().concat(baseApi.middleware) }); }
function renderPage() { render(<Provider store={createTestStore()}><MemoryRouter><MealLogPage /></MemoryRouter></Provider>); }
describe('MealLogPage', () => {
  beforeEach(() => vi.clearAllMocks());
  it('renders the page title', () => { renderPage(); expect(screen.getByText('Log Meal')).toBeInTheDocument(); });
  it('renders search input with accessible label', () => { renderPage(); expect(screen.getByRole('textbox', { name: /search foods/i })).toBeInTheDocument(); });
  it('renders quantity input', () => { renderPage(); expect(screen.getByRole('spinbutton', { name: /quantity/i })).toBeInTheDocument(); });
  it('renders meal type combobox', () => { renderPage(); expect(screen.getByRole('combobox', { name: /meal type/i })).toBeInTheDocument(); });
  it('renders add meal button with aria-label', () => { renderPage(); expect(screen.getByRole('button', { name: /add meal/i })).toBeInTheDocument(); });
  it('disables add meal button when no food selected', () => { renderPage(); expect(screen.getByRole('button', { name: /add meal/i })).toBeDisabled(); });
});
