import { describe, it, vi } from 'vitest';
import { render } from '@testing-library/react';
import { Provider } from 'react-redux';
import { configureStore } from '@reduxjs/toolkit';
import { MemoryRouter } from 'react-router-dom';
import authReducer from '@/features/auth/authSlice';
import uiReducer from '@/features/settings/uiSlice';
import websocketReducer from '@/features/websocket/websocketSlice';
import { baseApi } from '@/api/baseApi';
import LoginPage from './LoginPage';
import { checkAccessibility } from '@/test/axeHelper';

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

vi.mock('@/api/usersApi', () => ({
  useLoginMutation: () => [vi.fn(), { isLoading: false }],
}));

function renderLoginPage() {
  const store = configureStore({
    reducer: {
      auth: authReducer,
      ui: uiReducer,
      websocket: websocketReducer,
      [baseApi.reducerPath]: baseApi.reducer,
    },
    middleware: (gDM) => gDM().concat(baseApi.middleware),
  });

  return render(
    <Provider store={store}>
      <MemoryRouter>
        <LoginPage />
      </MemoryRouter>
    </Provider>,
  );
}

describe('LoginPage accessibility (axe-core)', () => {
  it('should have no WCAG 2.1 AA violations', async () => {
    const { container } = renderLoginPage();
    await checkAccessibility(container);
  });
});
