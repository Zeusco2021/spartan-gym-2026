import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook } from '@testing-library/react';
import React from 'react';
import { Provider } from 'react-redux';
import { configureStore } from '@reduxjs/toolkit';
import websocketReducer from '@/features/websocket/websocketSlice';
import authReducer from '@/features/auth/authSlice';
import uiReducer from '@/features/settings/uiSlice';
import { baseApi } from '@/api/baseApi';
import { useGymOccupancy } from './useGymOccupancy';

vi.mock('socket.io-client', () => ({
  io: vi.fn(() => ({
    on: vi.fn(),
    emit: vi.fn(),
    disconnect: vi.fn(),
  })),
}));

const mockJoin = vi.fn();
const mockLeave = vi.fn();

vi.mock('@/websocket/socketService', () => ({
  socketService: {
    joinGymOccupancy: (...args: unknown[]) => mockJoin(...args),
    leaveGymOccupancy: (...args: unknown[]) => mockLeave(...args),
  },
}));

function createTestStore(occupancyUpdates: Record<string, number> = {}) {
  return configureStore({
    reducer: {
      auth: authReducer,
      ui: uiReducer,
      websocket: websocketReducer,
      [baseApi.reducerPath]: baseApi.reducer,
    },
    preloadedState: {
      websocket: {
        connected: true,
        activeConversation: null,
        incomingMessages: [],
        occupancyUpdates,
        typingUsers: {},
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

describe('useGymOccupancy', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('subscribes to gym occupancy on mount', () => {
    const store = createTestStore();
    renderHook(() => useGymOccupancy('gym-1'), {
      wrapper: createWrapper(store),
    });
    expect(mockJoin).toHaveBeenCalledWith('gym-1');
    expect(mockJoin).toHaveBeenCalledTimes(1);
  });

  it('unsubscribes from gym occupancy on unmount', () => {
    const store = createTestStore();
    const { unmount } = renderHook(() => useGymOccupancy('gym-1'), {
      wrapper: createWrapper(store),
    });
    unmount();
    expect(mockLeave).toHaveBeenCalledWith('gym-1');
    expect(mockLeave).toHaveBeenCalledTimes(1);
  });

  it('returns occupancy value from Redux state', () => {
    const store = createTestStore({ 'gym-1': 42 });
    const { result } = renderHook(() => useGymOccupancy('gym-1'), {
      wrapper: createWrapper(store),
    });
    expect(result.current).toBe(42);
  });

  it('returns undefined when no occupancy data exists for gymId', () => {
    const store = createTestStore({});
    const { result } = renderHook(() => useGymOccupancy('gym-unknown'), {
      wrapper: createWrapper(store),
    });
    expect(result.current).toBeUndefined();
  });

  it('re-subscribes when gymId changes', () => {
    const store = createTestStore({ 'gym-1': 10, 'gym-2': 20 });
    const { result, rerender } = renderHook(
      ({ gymId }: { gymId: string }) => useGymOccupancy(gymId),
      {
        wrapper: createWrapper(store),
        initialProps: { gymId: 'gym-1' },
      },
    );

    expect(result.current).toBe(10);
    expect(mockJoin).toHaveBeenCalledWith('gym-1');

    rerender({ gymId: 'gym-2' });

    expect(mockLeave).toHaveBeenCalledWith('gym-1');
    expect(mockJoin).toHaveBeenCalledWith('gym-2');
    expect(result.current).toBe(20);
  });
});
