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
import DonationButton from './DonationButton';

vi.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string, params?: Record<string, string>) => {
      const translations: Record<string, string> = {
        donate: 'Donate',
        donateToCreator: `Donate to ${params?.name ?? ''}`,
        selectAmount: 'Select an amount',
        suggestedAmounts: 'Suggested amounts',
        customAmount: 'Custom amount',
        donationMessage: 'Message (optional)',
        donationError: 'Donation failed',
        donationSuccess: 'Thank you for your donation!',
        cancel: 'Cancel',
        close: 'Close',
        confirmDonation: 'Confirm Donation',
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

const mockDonateTrigger = vi.fn();
const mockDonateUnwrap = vi.fn();

vi.mock('@/api/paymentsApi', () => ({
  useDonateMutation: () => [
    (...args: unknown[]) => {
      mockDonateTrigger(...args);
      return { unwrap: mockDonateUnwrap };
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

function renderButton() {
  const store = createTestStore();
  render(
    <Provider store={store}>
      <MemoryRouter>
        <DonationButton creatorId="creator-1" creatorName="Coach Mike" />
      </MemoryRouter>
    </Provider>,
  );
  return store;
}

describe('DonationButton', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockDonateUnwrap.mockResolvedValue({});
  });

  it('renders the donate button with creator name in aria-label', () => {
    renderButton();
    const btn = screen.getByRole('button', { name: /donate coach mike/i });
    expect(btn).toBeInTheDocument();
  });

  it('opens donation dialog on click', async () => {
    const user = userEvent.setup();
    renderButton();

    await user.click(screen.getByRole('button', { name: /donate coach mike/i }));

    expect(screen.getByText(/Donate to Coach Mike/)).toBeInTheDocument();
  });

  it('allows selecting a suggested amount and confirming donation', async () => {
    const user = userEvent.setup();
    renderButton();

    await user.click(screen.getByRole('button', { name: /donate coach mike/i }));

    // Select $10
    await user.click(screen.getByRole('button', { name: /\$10/ }));

    // Confirm
    await user.click(screen.getByRole('button', { name: /confirm donation/i }));

    await waitFor(() => {
      expect(mockDonateTrigger).toHaveBeenCalledWith({
        creatorId: 'creator-1',
        amount: 10,
        currency: 'USD',
        message: undefined,
      });
    });
  });

  it('shows success message after donation', async () => {
    const user = userEvent.setup();
    renderButton();

    await user.click(screen.getByRole('button', { name: /donate coach mike/i }));
    await user.click(screen.getByRole('button', { name: /\$25/ }));
    await user.click(screen.getByRole('button', { name: /confirm donation/i }));

    await waitFor(() => {
      expect(screen.getByText(/thank you/i)).toBeInTheDocument();
    });
  });

  it('shows error when donation fails', async () => {
    mockDonateUnwrap.mockRejectedValue(new Error('fail'));
    const user = userEvent.setup();
    renderButton();

    await user.click(screen.getByRole('button', { name: /donate coach mike/i }));
    await user.click(screen.getByRole('button', { name: '$5.00' }));
    await user.click(screen.getByRole('button', { name: /confirm donation/i }));

    await waitFor(() => {
      expect(screen.getByText(/donation failed/i)).toBeInTheDocument();
    });
  });

  it('disables confirm button when no amount is selected', async () => {
    const user = userEvent.setup();
    renderButton();

    await user.click(screen.getByRole('button', { name: /donate coach mike/i }));

    expect(screen.getByRole('button', { name: /confirm donation/i })).toBeDisabled();
  });
});
