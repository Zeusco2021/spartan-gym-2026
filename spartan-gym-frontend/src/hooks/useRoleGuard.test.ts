import { describe, it, expect, vi } from 'vitest';
import { renderHook } from '@testing-library/react';
import React from 'react';
import { Provider } from 'react-redux';
import { configureStore } from '@reduxjs/toolkit';
import authReducer from '@/features/auth/authSlice';
import uiReducer from '@/features/settings/uiSlice';
import { baseApi } from '@/api/baseApi';
import { useRoleGuard } from './useRoleGuard';
import type { User } from '@/types';

// Mock socket.io-client to prevent socketService from failing
vi.mock('socket.io-client', () => ({
  io: vi.fn(() => ({
    on: vi.fn(),
    emit: vi.fn(),
    disconnect: vi.fn(),
  })),
}));

const mockUser: User = {
  id: 'u1',
  email: 'test@example.com',
  name: 'Test User',
  dateOfBirth: '1990-01-01',
  role: 'client',
  locale: 'en',
  mfaEnabled: false,
  onboardingCompleted: true,
};

function createTestStore(user: User | null, isAuthenticated: boolean) {
  return configureStore({
    reducer: {
      auth: authReducer,
      ui: uiReducer,
      [baseApi.reducerPath]: baseApi.reducer,
    },
    preloadedState: {
      auth: {
        user,
        token: isAuthenticated ? 'test-token' : null,
        isAuthenticated,
        mfaPending: false,
        mfaSessionToken: null,
      },
    },
    middleware: (getDefaultMiddleware) =>
      getDefaultMiddleware().concat(baseApi.middleware),
  });
}

function createWrapper(store: ReturnType<typeof createTestStore>) {
  return function Wrapper({ children }: { children: React.ReactNode }) {
    return React.createElement(Provider, { store }, children);
  };
}

describe('useRoleGuard', () => {
  it('grants access when user role is in allowedRoles', () => {
    const store = createTestStore(mockUser, true);
    const { result } = renderHook(() => useRoleGuard(['client', 'trainer']), {
      wrapper: createWrapper(store),
    });
    expect(result.current.hasAccess).toBe(true);
    expect(result.current.userRole).toBe('client');
  });

  it('denies access when user role is not in allowedRoles', () => {
    const store = createTestStore(mockUser, true);
    const { result } = renderHook(() => useRoleGuard(['admin']), {
      wrapper: createWrapper(store),
    });
    expect(result.current.hasAccess).toBe(false);
    expect(result.current.userRole).toBe('client');
  });

  it('denies access when user is not authenticated', () => {
    const store = createTestStore(null, false);
    const { result } = renderHook(() => useRoleGuard(['client']), {
      wrapper: createWrapper(store),
    });
    expect(result.current.hasAccess).toBe(false);
    expect(result.current.userRole).toBeUndefined();
  });

  it('grants access to admin for admin-only routes', () => {
    const adminUser = { ...mockUser, role: 'admin' as const };
    const store = createTestStore(adminUser, true);
    const { result } = renderHook(() => useRoleGuard(['admin']), {
      wrapper: createWrapper(store),
    });
    expect(result.current.hasAccess).toBe(true);
    expect(result.current.userRole).toBe('admin');
  });

  it('grants access to trainer for trainer-only routes', () => {
    const trainerUser = { ...mockUser, role: 'trainer' as const };
    const store = createTestStore(trainerUser, true);
    const { result } = renderHook(() => useRoleGuard(['trainer']), {
      wrapper: createWrapper(store),
    });
    expect(result.current.hasAccess).toBe(true);
    expect(result.current.userRole).toBe('trainer');
  });
});
