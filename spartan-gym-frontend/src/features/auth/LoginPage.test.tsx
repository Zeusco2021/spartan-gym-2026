import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Provider } from 'react-redux';
import { configureStore } from '@reduxjs/toolkit';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import authReducer from '@/features/auth/authSlice';
import uiReducer from '@/features/settings/uiSlice';
import websocketReducer from '@/features/websocket/websocketSlice';
import { baseApi } from '@/api/baseApi';
import LoginPage from './LoginPage';

// Mock i18next
vi.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string) => {
      const translations: Record<string, string> = {
        loginTitle: 'Welcome Back',
        loginSubtitle: 'Sign in to your account',
        email: 'Email',
        password: 'Password',
        login: 'Log In',
        loginError: 'Invalid email or password',
        noAccount: "Don't have an account?",
        registerLink: 'Sign up',
        emailRequired: 'Email is required',
        emailInvalid: 'Please enter a valid email address',
        passwordRequired: 'Password is required',
        passwordMinLength: 'Password must be at least 8 characters',
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

// Mock the login mutation
const mockLoginTrigger = vi.fn();
const mockUnwrap = vi.fn();

vi.mock('@/api/usersApi', () => ({
  useLoginMutation: () => [
    (...args: unknown[]) => {
      mockLoginTrigger(...args);
      return { unwrap: mockUnwrap };
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
    middleware: (getDefaultMiddleware) =>
      getDefaultMiddleware().concat(baseApi.middleware),
  });
}

function TestApp({ store }: { store: ReturnType<typeof createTestStore> }) {
  return (
    <Provider store={store}>
      <MemoryRouter initialEntries={['/login']}>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/dashboard" element={<div>Dashboard Page</div>} />
          <Route path="/mfa" element={<div>MFA Page</div>} />
          <Route path="/register" element={<div>Register Page</div>} />
        </Routes>
      </MemoryRouter>
    </Provider>
  );
}

function renderLoginPage() {
  const store = createTestStore();
  render(<TestApp store={store} />);
  return store;
}

describe('LoginPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders login form with email and password fields', () => {
    renderLoginPage();

    expect(screen.getByText('Welcome Back')).toBeInTheDocument();
    expect(screen.getByText('Sign in to your account')).toBeInTheDocument();
    expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /log in/i })).toBeInTheDocument();
  });

  it('shows validation errors for empty fields on submit', async () => {
    const user = userEvent.setup();
    renderLoginPage();

    await user.click(screen.getByRole('button', { name: /log in/i }));

    await waitFor(() => {
      expect(screen.getByText('Email is required')).toBeInTheDocument();
    });
    await waitFor(() => {
      expect(screen.getByText('Password is required')).toBeInTheDocument();
    });
  });

  it('shows validation error for invalid email format', async () => {
    const user = userEvent.setup();
    renderLoginPage();

    await user.type(screen.getByLabelText(/email/i), 'notanemail');
    await user.type(screen.getByLabelText(/password/i), 'password123');
    await user.click(screen.getByRole('button', { name: /log in/i }));

    await waitFor(() => {
      expect(
        screen.getByText('Please enter a valid email address'),
      ).toBeInTheDocument();
    });
  });

  it('shows validation error for short password', async () => {
    const user = userEvent.setup();
    renderLoginPage();

    await user.type(screen.getByLabelText(/email/i), 'test@example.com');
    await user.type(screen.getByLabelText(/password/i), 'short');
    await user.click(screen.getByRole('button', { name: /log in/i }));

    await waitFor(() => {
      expect(
        screen.getByText('Password must be at least 8 characters'),
      ).toBeInTheDocument();
    });
  });

  it('calls login mutation and navigates to dashboard on success', async () => {
    const user = userEvent.setup();
    mockUnwrap.mockResolvedValue({
      token: 'jwt-token-123',
      user: {
        id: '1',
        email: 'test@example.com',
        name: 'Test',
        role: 'client',
      },
    });

    const store = renderLoginPage();

    await user.type(screen.getByLabelText(/email/i), 'test@example.com');
    await user.type(screen.getByLabelText(/password/i), 'password123');
    await user.click(screen.getByRole('button', { name: /log in/i }));

    await waitFor(() => {
      expect(mockLoginTrigger).toHaveBeenCalledWith({
        email: 'test@example.com',
        password: 'password123',
      });
    });

    await waitFor(() => {
      const state = store.getState();
      expect(state.auth.token).toBe('jwt-token-123');
      expect(state.auth.isAuthenticated).toBe(true);
    });

    await waitFor(() => {
      expect(screen.getByText('Dashboard Page')).toBeInTheDocument();
    });
  });

  it('navigates to /mfa when mfaRequired is true', async () => {
    const user = userEvent.setup();
    mockUnwrap.mockResolvedValue({
      mfaRequired: true,
      sessionToken: 'mfa-session-token',
      token: '',
      user: null,
    });

    const store = renderLoginPage();

    await user.type(screen.getByLabelText(/email/i), 'test@example.com');
    await user.type(screen.getByLabelText(/password/i), 'password123');
    await user.click(screen.getByRole('button', { name: /log in/i }));

    await waitFor(() => {
      const state = store.getState();
      expect(state.auth.mfaPending).toBe(true);
    });

    await waitFor(() => {
      expect(screen.getByText('MFA Page')).toBeInTheDocument();
    });
  });

  it('shows error alert on login failure', async () => {
    const user = userEvent.setup();
    mockUnwrap.mockRejectedValue(new Error('Unauthorized'));

    renderLoginPage();

    await user.type(screen.getByLabelText(/email/i), 'test@example.com');
    await user.type(screen.getByLabelText(/password/i), 'password123');
    await user.click(screen.getByRole('button', { name: /log in/i }));

    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent(
        'Invalid email or password',
      );
    });
  });

  it('stores JWT only in Redux memory, not in localStorage or sessionStorage', async () => {
    const user = userEvent.setup();
    mockUnwrap.mockResolvedValue({
      token: 'secure-jwt-token',
      user: {
        id: '1',
        email: 'test@example.com',
        name: 'Test',
        role: 'client',
      },
    });

    const store = renderLoginPage();

    await user.type(screen.getByLabelText(/email/i), 'test@example.com');
    await user.type(screen.getByLabelText(/password/i), 'password123');
    await user.click(screen.getByRole('button', { name: /log in/i }));

    await waitFor(() => {
      expect(store.getState().auth.token).toBe('secure-jwt-token');
    });

    // Verify JWT is NOT stored in localStorage or sessionStorage
    expect(localStorage.getItem('token')).toBeNull();
    expect(sessionStorage.getItem('token')).toBeNull();
    // Check no JWT-like values exist in storage at all
    expect(localStorage.length).toBe(0);
    expect(sessionStorage.length).toBe(0);
  });

  it('renders a link to the register page', () => {
    renderLoginPage();

    const registerLink = screen.getByRole('link', { name: /sign up/i });
    expect(registerLink).toBeInTheDocument();
    expect(registerLink).toHaveAttribute('href', '/register');
  });
});
