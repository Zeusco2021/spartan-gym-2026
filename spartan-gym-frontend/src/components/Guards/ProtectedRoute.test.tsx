import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { Provider } from 'react-redux';
import { configureStore } from '@reduxjs/toolkit';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import authReducer from '@/features/auth/authSlice';
import uiReducer from '@/features/settings/uiSlice';
import websocketReducer from '@/features/websocket/websocketSlice';
import { baseApi } from '@/api/baseApi';
import { setCredentials } from '@/features/auth/authSlice';
import ProtectedRoute from './ProtectedRoute';

// Mock socket.io-client
vi.mock('socket.io-client', () => ({
  io: vi.fn(() => ({
    on: vi.fn(),
    emit: vi.fn(),
    disconnect: vi.fn(),
    connected: false,
  })),
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

const mockUser = {
  id: '1',
  email: 'test@example.com',
  name: 'Test User',
  dateOfBirth: '1990-01-01',
  role: 'client' as const,
  locale: 'en',
  mfaEnabled: false,
  onboardingCompleted: true,
};

function renderWithProtectedRoute(
  store: ReturnType<typeof createTestStore>,
  initialEntries: string[],
  allowedRoles?: Array<'client' | 'trainer' | 'admin'>,
) {
  return render(
    <Provider store={store}>
      <MemoryRouter initialEntries={initialEntries}>
        <Routes>
          <Route path="/login" element={<div>Login Page</div>} />
          <Route path="/unauthorized" element={<div>Unauthorized Page</div>} />
          <Route element={<ProtectedRoute allowedRoles={allowedRoles} />}>
            <Route path="/dashboard" element={<div>Dashboard Page</div>} />
            <Route path="/trainer" element={<div>Trainer Page</div>} />
            <Route path="/analytics" element={<div>Analytics Page</div>} />
          </Route>
        </Routes>
      </MemoryRouter>
    </Provider>,
  );
}

describe('ProtectedRoute', () => {
  it('redirects unauthenticated user to /login', () => {
    const store = createTestStore();
    renderWithProtectedRoute(store, ['/dashboard']);
    expect(screen.getByText('Login Page')).toBeInTheDocument();
  });

  it('preserves original route in location state when redirecting to /login', () => {
    const store = createTestStore();
    let capturedState: unknown = null;

    render(
      <Provider store={store}>
        <MemoryRouter initialEntries={['/dashboard']}>
          <Routes>
            <Route
              path="/login"
              element={
                <CaptureLocationState onCapture={(s) => { capturedState = s; }} />
              }
            />
            <Route element={<ProtectedRoute />}>
              <Route path="/dashboard" element={<div>Dashboard Page</div>} />
            </Route>
          </Routes>
        </MemoryRouter>
      </Provider>,
    );

    expect(capturedState).toEqual(
      expect.objectContaining({ from: expect.objectContaining({ pathname: '/dashboard' }) }),
    );
  });

  it('renders child route when user is authenticated with no role restriction', () => {
    const store = createTestStore();
    store.dispatch(setCredentials({ user: mockUser, token: 'test-token' }));
    renderWithProtectedRoute(store, ['/dashboard']);
    expect(screen.getByText('Dashboard Page')).toBeInTheDocument();
  });

  it('renders child route when user role is in allowedRoles', () => {
    const store = createTestStore();
    store.dispatch(setCredentials({ user: mockUser, token: 'test-token' }));
    renderWithProtectedRoute(store, ['/dashboard'], ['client', 'admin']);
    expect(screen.getByText('Dashboard Page')).toBeInTheDocument();
  });

  it('redirects to /unauthorized when user role is not in allowedRoles', () => {
    const store = createTestStore();
    store.dispatch(setCredentials({ user: mockUser, token: 'test-token' }));
    // mockUser has role 'client', but only 'trainer' is allowed
    renderWithProtectedRoute(store, ['/trainer'], ['trainer']);
    expect(screen.getByText('Unauthorized Page')).toBeInTheDocument();
  });

  it('redirects admin to /unauthorized for trainer-only routes', () => {
    const store = createTestStore();
    store.dispatch(
      setCredentials({ user: { ...mockUser, role: 'admin' }, token: 'test-token' }),
    );
    renderWithProtectedRoute(store, ['/trainer'], ['trainer']);
    expect(screen.getByText('Unauthorized Page')).toBeInTheDocument();
  });

  it('allows trainer to access trainer-only routes', () => {
    const store = createTestStore();
    store.dispatch(
      setCredentials({ user: { ...mockUser, role: 'trainer' }, token: 'test-token' }),
    );
    renderWithProtectedRoute(store, ['/trainer'], ['trainer']);
    expect(screen.getByText('Trainer Page')).toBeInTheDocument();
  });

  it('allows admin to access admin-only routes', () => {
    const store = createTestStore();
    store.dispatch(
      setCredentials({ user: { ...mockUser, role: 'admin' }, token: 'test-token' }),
    );
    renderWithProtectedRoute(store, ['/analytics'], ['admin']);
    expect(screen.getByText('Analytics Page')).toBeInTheDocument();
  });
});

// Helper component to capture location state
import { useLocation } from 'react-router-dom';

function CaptureLocationState({ onCapture }: { onCapture: (state: unknown) => void }) {
  const location = useLocation();
  onCapture(location.state);
  return <div>Login Page</div>;
}
