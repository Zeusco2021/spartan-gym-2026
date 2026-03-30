import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Provider } from 'react-redux';
import { configureStore } from '@reduxjs/toolkit';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import authReducer, { setMfaPending } from '@/features/auth/authSlice';
import uiReducer from '@/features/settings/uiSlice';
import websocketReducer from '@/features/websocket/websocketSlice';
import { baseApi } from '@/api/baseApi';
import MfaPage from './MfaPage';

// Mock i18next
vi.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string) => {
      const translations: Record<string, string> = {
        mfaTitle: 'Two-Factor Authentication',
        mfaSubtitle: 'Enter the 6-digit code from your authenticator app',
        mfaCode: 'Enter MFA Code',
        mfaCodeRequired: 'Code is required',
        mfaCodeInvalid: 'Code must be exactly 6 digits',
        mfaVerify: 'Verify',
        mfaError: 'Invalid verification code. Please try again.',
        mfaBackToLogin: 'Back to login',
      };
      return translations[key] ?? key;
    },
    i18n: { language: 'en' },
  }),
}));

// Mock socket.io-client
vi.mock('socket.io-client', () => ({
  io: vi.fn(() => ({
    on: vi.fn(),
    emit: vi.fn(),
    disconnect: vi.fn(),
    connected: false,
  })),
}));

// Mock socketService
vi.mock('@/websocket/socketService', () => ({
  socketService: {
    connect: vi.fn(),
    disconnect: vi.fn(),
  },
}));

// Mock the verifyMfa mutation
const mockVerifyMfaTrigger = vi.fn();
const mockUnwrap = vi.fn();

vi.mock('@/api/usersApi', () => ({
  useVerifyMfaMutation: () => [
    (...args: unknown[]) => {
      mockVerifyMfaTrigger(...args);
      return { unwrap: mockUnwrap };
    },
    { isLoading: false },
  ],
}));

function createTestStore(mfaPending = true) {
  const store = configureStore({
    reducer: {
      auth: authReducer,
      ui: uiReducer,
      websocket: websocketReducer,
      [baseApi.reducerPath]: baseApi.reducer,
    },
    middleware: (getDefaultMiddleware) =>
      getDefaultMiddleware().concat(baseApi.middleware),
  });

  if (mfaPending) {
    store.dispatch(setMfaPending('mfa-session-token-123'));
  }

  return store;
}

function TestApp({
  store,
  initialEntries = ['/mfa'],
}: {
  store: ReturnType<typeof createTestStore>;
  initialEntries?: string[];
}) {
  return (
    <Provider store={store}>
      <MemoryRouter initialEntries={initialEntries}>
        <Routes>
          <Route path="/mfa" element={<MfaPage />} />
          <Route path="/dashboard" element={<div>Dashboard Page</div>} />
          <Route path="/login" element={<div>Login Page</div>} />
        </Routes>
      </MemoryRouter>
    </Provider>
  );
}

function renderMfaPage(mfaPending = true) {
  const store = createTestStore(mfaPending);
  render(<TestApp store={store} />);
  return store;
}

describe('MfaPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders MFA form with code input when mfaPending is true', () => {
    renderMfaPage();

    expect(screen.getByText('Two-Factor Authentication')).toBeInTheDocument();
    expect(
      screen.getByText(
        'Enter the 6-digit code from your authenticator app',
      ),
    ).toBeInTheDocument();
    expect(screen.getByLabelText(/enter mfa code/i)).toBeInTheDocument();
    expect(
      screen.getByRole('button', { name: /verify/i }),
    ).toBeInTheDocument();
  });

  it('redirects to /login when mfaPending is false', async () => {
    renderMfaPage(false);

    await waitFor(() => {
      expect(screen.getByText('Login Page')).toBeInTheDocument();
    });
  });

  it('shows validation error for empty code on submit', async () => {
    const user = userEvent.setup();
    renderMfaPage();

    await user.click(screen.getByRole('button', { name: /verify/i }));

    await waitFor(() => {
      expect(screen.getByText('Code is required')).toBeInTheDocument();
    });
  });

  it('shows validation error for non-6-digit code', async () => {
    const user = userEvent.setup();
    renderMfaPage();

    await user.type(screen.getByLabelText(/enter mfa code/i), '123');
    await user.click(screen.getByRole('button', { name: /verify/i }));

    await waitFor(() => {
      expect(
        screen.getByText('Code must be exactly 6 digits'),
      ).toBeInTheDocument();
    });
  });

  it('shows validation error for non-numeric code', async () => {
    const user = userEvent.setup();
    renderMfaPage();

    await user.type(screen.getByLabelText(/enter mfa code/i), 'abcdef');
    await user.click(screen.getByRole('button', { name: /verify/i }));

    await waitFor(() => {
      expect(
        screen.getByText('Code must be exactly 6 digits'),
      ).toBeInTheDocument();
    });
  });

  it('calls verifyMfa and navigates to dashboard on success', async () => {
    const user = userEvent.setup();
    mockUnwrap.mockResolvedValue({
      token: 'jwt-token-after-mfa',
      user: {
        id: '1',
        email: 'test@example.com',
        name: 'Test',
        role: 'client',
      },
    });

    const store = renderMfaPage();

    await user.type(screen.getByLabelText(/enter mfa code/i), '123456');
    await user.click(screen.getByRole('button', { name: /verify/i }));

    await waitFor(() => {
      expect(mockVerifyMfaTrigger).toHaveBeenCalledWith({
        code: '123456',
        sessionToken: 'mfa-session-token-123',
      });
    });

    await waitFor(() => {
      const state = store.getState();
      expect(state.auth.token).toBe('jwt-token-after-mfa');
      expect(state.auth.isAuthenticated).toBe(true);
      expect(state.auth.mfaPending).toBe(false);
    });

    await waitFor(() => {
      expect(screen.getByText('Dashboard Page')).toBeInTheDocument();
    });
  });

  it('shows error alert on verification failure', async () => {
    const user = userEvent.setup();
    mockUnwrap.mockRejectedValue(new Error('Invalid code'));

    renderMfaPage();

    await user.type(screen.getByLabelText(/enter mfa code/i), '999999');
    await user.click(screen.getByRole('button', { name: /verify/i }));

    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent(
        'Invalid verification code. Please try again.',
      );
    });
  });

  it('renders a link back to login', () => {
    renderMfaPage();

    const backLink = screen.getByRole('link', { name: /back to login/i });
    expect(backLink).toBeInTheDocument();
    expect(backLink).toHaveAttribute('href', '/login');
  });
});
