import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Provider } from 'react-redux';
import { configureStore } from '@reduxjs/toolkit';
import uiReducer from '@/features/settings/uiSlice';
import { baseApi } from '@/api/baseApi';
import LanguageSettingsPage from './LanguageSettingsPage';

const mockChangeLanguage = vi.fn();

vi.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string) => {
      const m: Record<string, string> = {
        language: 'Language',
        selectLanguage: 'Select Language',
        measurementUnits: 'Measurement Units',
        metric: 'Metric',
        imperial: 'Imperial',
        formatPreview: 'Format Preview',
        dateFormat: 'Date',
        timeFormat: 'Time',
        currencyFormat: 'Currency',
        numberFormat: 'Number',
        weightFormat: 'Weight',
        distanceFormat: 'Distance',
      };
      return m[key] ?? key;
    },
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

function renderPage(locale = 'en', measurementUnit: 'metric' | 'imperial' = 'metric') {
  return render(
    <Provider store={createTestStore(locale, measurementUnit)}>
      <LanguageSettingsPage />
    </Provider>,
  );
}

describe('LanguageSettingsPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders the page heading', () => {
    renderPage();
    expect(screen.getByRole('heading', { name: 'Language', level: 4 })).toBeInTheDocument();
  });

  it('renders language selector with aria-label', () => {
    renderPage();
    expect(screen.getByLabelText('Select Language')).toBeInTheDocument();
  });

  it('renders measurement unit radio group with aria-label', () => {
    renderPage();
    expect(screen.getByRole('radiogroup', { name: 'Measurement Units' })).toBeInTheDocument();
  });

  it('renders metric and imperial radio options', () => {
    renderPage();
    // MUI Radio with aria-label combines with FormControlLabel label
    const radios = screen.getAllByRole('radio');
    expect(radios).toHaveLength(2);
    expect(radios[0]).toBeChecked(); // metric is default
    expect(radios[1]).not.toBeChecked();
  });

  it('renders format preview section with date, time, currency, number, weight, distance', () => {
    renderPage();
    expect(screen.getByText('Format Preview')).toBeInTheDocument();
    expect(screen.getByText('Date')).toBeInTheDocument();
    expect(screen.getByText('Time')).toBeInTheDocument();
    expect(screen.getByText('Currency')).toBeInTheDocument();
    expect(screen.getByText('Number')).toBeInTheDocument();
    expect(screen.getByText('Weight')).toBeInTheDocument();
    expect(screen.getByText('Distance')).toBeInTheDocument();
  });

  it('shows metric weight format by default', () => {
    renderPage();
    expect(screen.getByText('75 kg')).toBeInTheDocument();
  });

  it('shows imperial weight format when imperial is selected', () => {
    renderPage('en', 'imperial');
    expect(screen.getByText('165.3 lbs')).toBeInTheDocument();
  });

  it('shows metric distance format by default', () => {
    renderPage();
    expect(screen.getByText('10 km')).toBeInTheDocument();
  });

  it('shows imperial distance format when imperial is selected', () => {
    renderPage('en', 'imperial');
    expect(screen.getByText('6.2 mi')).toBeInTheDocument();
  });

  it('switches measurement unit when radio is clicked', async () => {
    const user = userEvent.setup();
    renderPage();
    const radios = screen.getAllByRole('radio');
    const imperialRadio = radios[1]; // second radio is imperial
    await user.click(imperialRadio);
    expect(imperialRadio).toBeChecked();
  });
});
