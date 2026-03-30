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
import TransactionsPage from './TransactionsPage';

// Mock i18next
vi.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string) => {
      const translations: Record<string, string> = {
        transactions: 'Transactions',
        filterType: 'Type',
        filterStatus: 'Status',
        all: 'All',
        type_subscription: 'Subscription',
        type_donation: 'Donation',
        type_refund: 'Refund',
        status_completed: 'Completed',
        status_pending: 'Pending',
        status_failed: 'Failed',
        status_refunded: 'Refunded',
        noTransactions: 'No transactions found.',
        refund: 'Request Refund',
        refundAmount: 'Amount',
        refundReason: 'Reason for refund',
        confirmRefund: 'Confirm Refund',
        cancel: 'Cancel',
        error: 'An error occurred. Please try again.',
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
  socketService: { connect: vi.fn(), disconnect: vi.fn() },
}));

const mockTransactions = {
  content: [
    { id: 'tx-1', userId: 'u1', amount: 19.99, currency: 'USD', type: 'subscription' as const, status: 'completed' as const, createdAt: '2024-01-15T10:00:00Z' },
    { id: 'tx-2', userId: 'u1', amount: 5.0, currency: 'USD', type: 'donation' as const, status: 'completed' as const, createdAt: '2024-01-14T10:00:00Z' },
    { id: 'tx-3', userId: 'u1', amount: 19.99, currency: 'USD', type: 'refund' as const, status: 'pending' as const, createdAt: '2024-01-13T10:00:00Z' },
    { id: 'tx-4', userId: 'u1', amount: 9.99, currency: 'USD', type: 'subscription' as const, status: 'failed' as const, createdAt: '2024-01-12T10:00:00Z' },
  ],
  page: 0,
  size: 20,
  totalElements: 4,
  totalPages: 1,
};

const mockRefundTrigger = vi.fn();
const mockRefundUnwrap = vi.fn();

vi.mock('@/api/paymentsApi', () => ({
  useGetTransactionsQuery: () => ({
    data: mockTransactions,
    isLoading: false,
    isError: false,
  }),
  useRequestRefundMutation: () => [
    (...args: unknown[]) => {
      mockRefundTrigger(...args);
      return { unwrap: mockRefundUnwrap };
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
        <TransactionsPage />
      </MemoryRouter>
    </Provider>,
  );
  return store;
}

describe('TransactionsPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders the transactions heading and filter controls', () => {
    renderPage();
    expect(screen.getByText('Transactions')).toBeInTheDocument();
    const comboboxes = screen.getAllByRole('combobox');
    expect(comboboxes).toHaveLength(2);
  });

  it('displays all transactions in the virtualized list', () => {
    renderPage();
    expect(screen.getByText('tx-1')).toBeInTheDocument();
    expect(screen.getByText('tx-2')).toBeInTheDocument();
    expect(screen.getByText('tx-3')).toBeInTheDocument();
    expect(screen.getByText('tx-4')).toBeInTheDocument();
  });

  it('shows refund button only for completed non-refund transactions', () => {
    renderPage();
    // tx-1 (subscription, completed) and tx-2 (donation, completed) should have refund buttons
    const refundButtons = screen.getAllByRole('button', { name: /request refund/i });
    expect(refundButtons).toHaveLength(2);
  });

  it('opens refund dialog and submits refund request', async () => {
    const user = userEvent.setup();
    mockRefundUnwrap.mockResolvedValue({});
    renderPage();

    const refundButtons = screen.getAllByRole('button', { name: /request refund/i });
    await user.click(refundButtons[0]);

    // Dialog should be open - use role-based query for the textarea
    const reasonField = await screen.findByRole('textbox');
    await user.type(reasonField, 'Duplicate charge');

    // Confirm
    await user.click(screen.getByRole('button', { name: /confirm refund/i }));

    await waitFor(() => {
      expect(mockRefundTrigger).toHaveBeenCalledWith({
        transactionId: 'tx-1',
        reason: 'Duplicate charge',
      });
    });
  });

  it('disables confirm button when reason is empty', async () => {
    const user = userEvent.setup();
    renderPage();

    const refundButtons = screen.getAllByRole('button', { name: /request refund/i });
    await user.click(refundButtons[0]);

    const confirmBtn = screen.getByRole('button', { name: /confirm refund/i });
    expect(confirmBtn).toBeDisabled();
  });

  it('shows error when refund request fails', async () => {
    const user = userEvent.setup();
    mockRefundUnwrap.mockRejectedValue(new Error('fail'));
    renderPage();

    const refundButtons = screen.getAllByRole('button', { name: /request refund/i });
    await user.click(refundButtons[0]);

    await user.type(screen.getByLabelText('Reason for refund'), 'Test reason');
    await user.click(screen.getByRole('button', { name: /confirm refund/i }));

    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent('An error occurred');
    });
  });
});
