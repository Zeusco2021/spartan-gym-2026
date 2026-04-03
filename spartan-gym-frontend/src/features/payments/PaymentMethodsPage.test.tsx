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
import PaymentMethodsPage from './PaymentMethodsPage';
import type { PaymentMethod } from '@/types/payments';

vi.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string, params?: Record<string, string>) => {
      const translations: Record<string, string> = {
        paymentMethods: 'Payment Methods',
        paymentMethodsList: 'Payment methods list',
        loading: 'Loading',
        error: 'An error occurred',
        noPaymentMethods: 'No payment methods added.',
        default: 'Default',
        addPaymentMethod: 'Add Payment Method',
        removeMethod: 'Remove',
        removeMethodTitle: 'Remove Payment Method',
        removeMethodConfirm: `Remove ${params?.method ?? ''}?`,
        cancel: 'Cancel',
        confirmRemove: 'Remove',
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

const mockMethods: PaymentMethod[] = [
  { id: 'pm-1', userId: 'u1', provider: 'stripe', type: 'card', last4: '4242', expiryMonth: 12, expiryYear: 2026, isDefault: true },
  { id: 'pm-2', userId: 'u1', provider: 'stripe', type: 'card', last4: '1234', expiryMonth: 6, expiryYear: 2025, isDefault: false },
];

const mockAddTrigger = vi.fn();
const mockAddUnwrap = vi.fn();
const mockRemoveTrigger = vi.fn();
const mockRemoveUnwrap = vi.fn();

vi.mock('@/api/paymentsApi', () => ({
  useGetPaymentMethodsQuery: () => ({
    data: mockMethods,
    isLoading: false,
    isError: false,
  }),
  useAddPaymentMethodMutation: () => [
    (...args: unknown[]) => {
      mockAddTrigger(...args);
      return { unwrap: mockAddUnwrap };
    },
    { isLoading: false },
  ],
  useRemovePaymentMethodMutation: () => [
    (...args: unknown[]) => {
      mockRemoveTrigger(...args);
      return { unwrap: mockRemoveUnwrap };
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
        <PaymentMethodsPage />
      </MemoryRouter>
    </Provider>,
  );
  return store;
}

describe('PaymentMethodsPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockAddUnwrap.mockResolvedValue({});
    mockRemoveUnwrap.mockResolvedValue({});
  });

  it('renders the heading and payment methods list', () => {
    renderPage();
    expect(screen.getByText('Payment Methods')).toBeInTheDocument();
    expect(screen.getByRole('list', { name: 'Payment methods list' })).toBeInTheDocument();
  });

  it('displays payment methods with last4 digits', () => {
    renderPage();
    expect(screen.getByText('•••• 4242')).toBeInTheDocument();
    expect(screen.getByText('•••• 1234')).toBeInTheDocument();
  });

  it('shows default chip on default payment method', () => {
    renderPage();
    expect(screen.getByText('Default')).toBeInTheDocument();
  });

  it('disables delete button for default payment method', () => {
    renderPage();
    const deleteButtons = screen.getAllByRole('button', { name: /remove/i });
    // First method is default, its delete button should be disabled
    expect(deleteButtons[0]).toBeDisabled();
    expect(deleteButtons[1]).not.toBeDisabled();
  });

  it('opens delete confirmation dialog and removes method', async () => {
    const user = userEvent.setup();
    renderPage();

    const deleteButtons = screen.getAllByRole('button', { name: /remove/i });
    await user.click(deleteButtons[1]); // non-default method

    expect(screen.getByText('Remove Payment Method')).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'Remove' }));

    await waitFor(() => {
      expect(mockRemoveTrigger).toHaveBeenCalledWith('pm-2');
    });
  });

  it('has accessible add payment method button', () => {
    renderPage();
    expect(screen.getByRole('button', { name: 'Add Payment Method' })).toBeInTheDocument();
  });
});
