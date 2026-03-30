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
import RegisterPage from './RegisterPage';

// Mock i18next
vi.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string) => {
      const translations: Record<string, string> = {
        registerTitle: 'Create Account',
        registerSubtitle: 'Sign up to get started',
        name: 'Full Name',
        email: 'Email',
        password: 'Password',
        dateOfBirth: 'Date of Birth',
        register: 'Register',
        nameRequired: 'Name is required',
        emailRequired: 'Email is required',
        emailInvalid: 'Please enter a valid email address',
        passwordRequired: 'Password is required',
        passwordMinLength: 'Password must be at least 8 characters',
        dateOfBirthRequired: 'Date of birth is required',
        emailAlreadyRegistered: 'This email is already registered',
        registerError: 'Registration failed. Please try again.',
        hasAccount: 'Already have an account?',
        loginLink: 'Log in',
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

// Mock the register mutation
const mockRegisterTrigger = vi.fn();
const mockUnwrap = vi.fn();

vi.mock('@/api/usersApi', () => ({
  useRegisterMutation: () => [
    (...args: unknown[]) => {
      mockRegisterTrigger(...args);
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
      <MemoryRouter initialEntries={['/register']}>
        <Routes>
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/login" element={<div>Login Page</div>} />
        </Routes>
      </MemoryRouter>
    </Provider>
  );
}

function renderRegisterPage() {
  const store = createTestStore();
  render(<TestApp store={store} />);
  return store;
}

describe('RegisterPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders register form with all fields', () => {
    renderRegisterPage();

    expect(screen.getByText('Create Account')).toBeInTheDocument();
    expect(screen.getByText('Sign up to get started')).toBeInTheDocument();
    expect(screen.getByLabelText(/full name/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/date of birth/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /register/i })).toBeInTheDocument();
  });

  it('shows validation errors for empty fields on submit', async () => {
    const user = userEvent.setup();
    renderRegisterPage();

    await user.click(screen.getByRole('button', { name: /register/i }));

    await waitFor(() => {
      expect(screen.getByText('Name is required')).toBeInTheDocument();
    });
    await waitFor(() => {
      expect(screen.getByText('Email is required')).toBeInTheDocument();
    });
    await waitFor(() => {
      expect(screen.getByText('Password is required')).toBeInTheDocument();
    });
    await waitFor(() => {
      expect(screen.getByText('Date of birth is required')).toBeInTheDocument();
    });
  });

  it('shows validation error for invalid email format', async () => {
    const user = userEvent.setup();
    renderRegisterPage();

    await user.type(screen.getByLabelText(/full name/i), 'John Doe');
    await user.type(screen.getByLabelText(/email/i), 'notanemail');
    await user.type(screen.getByLabelText(/password/i), 'password123');
    await user.type(screen.getByLabelText(/date of birth/i), '1990-01-01');
    await user.click(screen.getByRole('button', { name: /register/i }));

    await waitFor(() => {
      expect(
        screen.getByText('Please enter a valid email address'),
      ).toBeInTheDocument();
    });
  });

  it('shows validation error for short password', async () => {
    const user = userEvent.setup();
    renderRegisterPage();

    await user.type(screen.getByLabelText(/full name/i), 'John Doe');
    await user.type(screen.getByLabelText(/email/i), 'test@example.com');
    await user.type(screen.getByLabelText(/password/i), 'short');
    await user.type(screen.getByLabelText(/date of birth/i), '1990-01-01');
    await user.click(screen.getByRole('button', { name: /register/i }));

    await waitFor(() => {
      expect(
        screen.getByText('Password must be at least 8 characters'),
      ).toBeInTheDocument();
    });
  });

  it('calls register mutation and navigates to login on success', async () => {
    const user = userEvent.setup();
    mockUnwrap.mockResolvedValue({
      id: '1',
      email: 'test@example.com',
      name: 'John Doe',
      role: 'client',
    });

    renderRegisterPage();

    await user.type(screen.getByLabelText(/full name/i), 'John Doe');
    await user.type(screen.getByLabelText(/email/i), 'test@example.com');
    await user.type(screen.getByLabelText(/password/i), 'password123');
    await user.type(screen.getByLabelText(/date of birth/i), '1990-01-01');
    await user.click(screen.getByRole('button', { name: /register/i }));

    await waitFor(() => {
      expect(mockRegisterTrigger).toHaveBeenCalledWith({
        name: 'John Doe',
        email: 'test@example.com',
        password: 'password123',
        dateOfBirth: '1990-01-01',
      });
    });

    await waitFor(() => {
      expect(screen.getByText('Login Page')).toBeInTheDocument();
    });
  });

  it('shows email already registered error on 409 response', async () => {
    const user = userEvent.setup();
    mockUnwrap.mockRejectedValue({ status: 409 });

    renderRegisterPage();

    await user.type(screen.getByLabelText(/full name/i), 'John Doe');
    await user.type(screen.getByLabelText(/email/i), 'existing@example.com');
    await user.type(screen.getByLabelText(/password/i), 'password123');
    await user.type(screen.getByLabelText(/date of birth/i), '1990-01-01');
    await user.click(screen.getByRole('button', { name: /register/i }));

    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent(
        'This email is already registered',
      );
    });
  });

  it('shows generic error on non-409 failure', async () => {
    const user = userEvent.setup();
    mockUnwrap.mockRejectedValue({ status: 500 });

    renderRegisterPage();

    await user.type(screen.getByLabelText(/full name/i), 'John Doe');
    await user.type(screen.getByLabelText(/email/i), 'test@example.com');
    await user.type(screen.getByLabelText(/password/i), 'password123');
    await user.type(screen.getByLabelText(/date of birth/i), '1990-01-01');
    await user.click(screen.getByRole('button', { name: /register/i }));

    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent(
        'Registration failed. Please try again.',
      );
    });
  });

  it('renders a link to the login page', () => {
    renderRegisterPage();

    const loginLink = screen.getByRole('link', { name: /log in/i });
    expect(loginLink).toBeInTheDocument();
    expect(loginLink).toHaveAttribute('href', '/login');
  });
});
