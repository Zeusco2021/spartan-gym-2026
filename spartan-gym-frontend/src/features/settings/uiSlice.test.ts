import { describe, it, expect } from 'vitest';
import uiReducer, {
  toggleTheme,
  setLocale,
  toggleSidebar,
  setMeasurementUnit,
} from '@/features/settings/uiSlice';

describe('uiSlice', () => {
  const initialState = {
    theme: 'light' as const,
    locale: 'en',
    sidebarOpen: true,
    measurementUnit: 'metric' as const,
  };

  it('should return the initial state', () => {
    expect(uiReducer(undefined, { type: 'unknown' })).toEqual(initialState);
  });

  it('toggleTheme should switch from light to dark', () => {
    const state = uiReducer(initialState, toggleTheme());
    expect(state.theme).toBe('dark');
  });

  it('toggleTheme should switch from dark to light', () => {
    const darkState = { ...initialState, theme: 'dark' as const };
    const state = uiReducer(darkState, toggleTheme());
    expect(state.theme).toBe('light');
  });

  it('setLocale should update the locale', () => {
    const state = uiReducer(initialState, setLocale('es'));
    expect(state.locale).toBe('es');
  });

  it('toggleSidebar should toggle sidebarOpen', () => {
    const state = uiReducer(initialState, toggleSidebar());
    expect(state.sidebarOpen).toBe(false);
    const state2 = uiReducer(state, toggleSidebar());
    expect(state2.sidebarOpen).toBe(true);
  });

  it('setMeasurementUnit should update the unit', () => {
    const state = uiReducer(initialState, setMeasurementUnit('imperial'));
    expect(state.measurementUnit).toBe('imperial');
  });
});
