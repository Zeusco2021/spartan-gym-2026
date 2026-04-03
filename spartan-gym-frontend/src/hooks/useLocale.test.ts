import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { Provider } from 'react-redux';
import { configureStore } from '@reduxjs/toolkit';
import { createElement } from 'react';
import uiReducer from '@/features/settings/uiSlice';
import { baseApi } from '@/api/baseApi';
import { useLocale } from './useLocale';

const mockChangeLanguage = vi.fn();

vi.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string) => key,
    i18n: { changeLanguage: mockChangeLanguage, language: 'en' },
  }),
}));

vi.mock('socket.io-client', () => ({
  io: vi.fn(() => ({ on: vi.fn(), emit: vi.fn(), disconnect: vi.fn(), connected: false })),
}));
vi.mock('@/websocket/socketService', () => ({
  socketService: { connect: vi.fn(), disconnect: vi.fn() },
}));

function createTestStore(locale = 'en', measurementUnit: 'metric' | 'imperial' = 'metric') {
  return configureStore({
    reducer: {
      ui: uiReducer,
      [baseApi.reducerPath]: baseApi.reducer,
    },
    middleware: (gDM) => gDM().concat(baseApi.middleware),
    preloadedState: {
      ui: { theme: 'light' as const, locale, sidebarOpen: true, measurementUnit },
    },
  });
}

function renderUseLocale(locale = 'en', measurementUnit: 'metric' | 'imperial' = 'metric') {
  const store = createTestStore(locale, measurementUnit);
  return renderHook(() => useLocale(), {
    wrapper: ({ children }) => createElement(Provider, { store }, children),
  });
}

describe('useLocale', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('returns the current locale', () => {
    const { result } = renderUseLocale('es');
    expect(result.current.locale).toBe('es');
  });

  it('changeLocale calls i18n.changeLanguage and dispatches setLocale', () => {
    const { result } = renderUseLocale();
    act(() => {
      result.current.changeLocale('fr');
    });
    expect(mockChangeLanguage).toHaveBeenCalledWith('fr');
    expect(result.current.locale).toBe('fr');
  });

  it('formatDate returns locale-formatted date string', () => {
    const { result } = renderUseLocale('en');
    const formatted = result.current.formatDate('2025-06-15T12:00:00Z');
    expect(formatted).toBeTruthy();
    expect(typeof formatted).toBe('string');
  });

  it('formatTime returns locale-formatted time string', () => {
    const { result } = renderUseLocale('en');
    const formatted = result.current.formatTime('2025-06-15T14:30:00Z');
    expect(formatted).toBeTruthy();
    expect(typeof formatted).toBe('string');
  });

  it('formatCurrency returns locale-formatted currency string', () => {
    const { result } = renderUseLocale('en');
    const formatted = result.current.formatCurrency(99.99, 'USD');
    expect(formatted).toContain('99.99');
  });

  it('formatNumber returns locale-formatted number string', () => {
    const { result } = renderUseLocale('en');
    const formatted = result.current.formatNumber(1234567.89);
    expect(formatted).toBeTruthy();
    expect(typeof formatted).toBe('string');
  });

  it('formatWeight returns metric format by default', () => {
    const { result } = renderUseLocale('en', 'metric');
    expect(result.current.formatWeight(80)).toBe('80 kg');
  });

  it('formatWeight returns imperial format when set', () => {
    const { result } = renderUseLocale('en', 'imperial');
    expect(result.current.formatWeight(80)).toBe('176.4 lbs');
  });

  it('formatDistance returns metric format by default', () => {
    const { result } = renderUseLocale('en', 'metric');
    expect(result.current.formatDistance(5)).toBe('5 km');
  });

  it('formatDistance returns imperial format when set', () => {
    const { result } = renderUseLocale('en', 'imperial');
    expect(result.current.formatDistance(5)).toBe('3.1 mi');
  });

  it('returns measurementUnit from state', () => {
    const { result } = renderUseLocale('en', 'imperial');
    expect(result.current.measurementUnit).toBe('imperial');
  });
});
