import { createSlice, type PayloadAction } from '@reduxjs/toolkit';

interface UiState {
  theme: 'light' | 'dark';
  locale: string;
  sidebarOpen: boolean;
  measurementUnit: 'metric' | 'imperial';
}

const initialState: UiState = {
  theme: 'light',
  locale: 'en',
  sidebarOpen: true,
  measurementUnit: 'metric',
};

const uiSlice = createSlice({
  name: 'ui',
  initialState,
  reducers: {
    toggleTheme(state) {
      state.theme = state.theme === 'light' ? 'dark' : 'light';
    },
    setLocale(state, action: PayloadAction<string>) {
      state.locale = action.payload;
    },
    toggleSidebar(state) {
      state.sidebarOpen = !state.sidebarOpen;
    },
    setMeasurementUnit(state, action: PayloadAction<'metric' | 'imperial'>) {
      state.measurementUnit = action.payload;
    },
  },
});

export const { toggleTheme, setLocale, toggleSidebar, setMeasurementUnit } = uiSlice.actions;
export default uiSlice.reducer;
