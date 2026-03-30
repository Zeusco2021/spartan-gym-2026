import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import React from 'react';
import { Provider } from 'react-redux';
import { configureStore } from '@reduxjs/toolkit';
import uiReducer from '@/features/settings/uiSlice';
import { useLocale } from './useLocale';

const mockChangeLanguage = vi.fn();
vi.mock('react-i18next', () => ({
  useTranslation: () => ({
    i18n: { changeLanguage: mockChangeLanguage },
    t: (key: string) => key,
  }),
}));

function createTestStore(overrides?: { locale?: string; measurementUnit?: 'metric' | 'imperial' }) {
  return configureStore({
    reducer: { ui: uiReducer },
    preloadedState: {
      ui: {
        theme: 'light' as const,
        locale: overrides?.locale ?? 'en',
        sidebarOpen: true,
        measurementUnit: overrides?.measurementUnit ?? 'metric',
      },
    },
  });
}

function createWrapper(store: ReturnType<typeof createTestStore>) {
  return function Wrapper({ children }: { children: React.ReactNode }) {
    return React.createElement(Provider, { store }, children);
  };
}

describe('useLocale', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('returns current locale from store', () => {
    const store = createTestStore({ locale: 'es' });
    const { result } = renderHook(() => useLocale(), { wrapper: createWrapper(store) });
    expect(result.current.locale).toBe('es');
  });

  it('changeLocale updates i18n and dispatches setLocale', () => {
    const store = createTestStore();
    const { result } = renderHook(() => useLocale(), { wrapper: createWrapper(store) });

    act(() => {
      result.current.changeLocale('fr');
    });

    expect(mockChangeLanguage).toHaveBeenCalledWith('fr');
    expect(result.current.locale).toBe('fr');
  });

  describe('formatWeight', () => {
    it('formats weight in kg when metric', () => {
      const store = createTestStore({ measurementUnit: 'metric' });
      const { result } = renderHook(() => useLocale(), { wrapper: createWrapper(store) });
      expect(result.current.formatWeight(80)).toBe('80 kg');
    });

    it('formats weight in lbs when imperial', () => {
      const store = createTestStore({ measurementUnit: 'imperial' });
      const { result } = renderHook(() => useLocale(), { wrapper: createWrapper(store) });
      expect(result.current.formatWeight(80)).toBe('176.4 lbs');
    });

    it('converts 0 kg correctly', () => {
      const store = createTestStore({ measurementUnit: 'imperial' });
      const { result } = renderHook(() => useLocale(), { wrapper: createWrapper(store) });
      expect(result.current.formatWeight(0)).toBe('0.0 lbs');
    });
  });

  describe('formatDistance', () => {
    it('formats distance in km when metric', () => {
      const store = createTestStore({ measurementUnit: 'metric' });
      const { result } = renderHook(() => useLocale(), { wrapper: createWrapper(store) });
      expect(result.current.formatDistance(10)).toBe('10 km');
    });

    it('formats distance in miles when imperial', () => {
      const store = createTestStore({ measurementUnit: 'imperial' });
      const { result } = renderHook(() => useLocale(), { wrapper: createWrapper(store) });
      expect(result.current.formatDistance(10)).toBe('6.2 mi');
    });
  });

  describe('formatDate', () => {
    it('formats a date string using Intl.DateTimeFormat', () => {
      const store = createTestStore({ locale: 'en' });
      const { result } = renderHook(() => useLocale(), { wrapper: createWrapper(store) });
      const formatted = result.current.formatDate('2024-06-15T12:00:00Z');
      expect(formatted).toBeTruthy();
      expect(typeof formatted).toBe('string');
    });
  });

  describe('formatCurrency', () => {
    it('formats currency using Intl.NumberFormat', () => {
      const store = createTestStore({ locale: 'en' });
      const { result } = renderHook(() => useLocale(), { wrapper: createWrapper(store) });
      const formatted = result.current.formatCurrency(29.99, 'USD');
      expect(formatted).toContain('29.99');
    });
  });
});
