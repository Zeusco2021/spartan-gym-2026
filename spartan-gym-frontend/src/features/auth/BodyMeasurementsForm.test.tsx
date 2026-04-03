import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import BodyMeasurementsForm from './BodyMeasurementsForm';

vi.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string) => {
      const translations: Record<string, string> = {
        'bodyMeasurements.title': 'Body Measurements',
        'bodyMeasurements.subtitle': 'Record your initial measurements to track progress.',
        'bodyMeasurements.measurements': 'Measurements',
        'bodyMeasurements.weight': 'Weight (kg)',
        'bodyMeasurements.height': 'Height (cm)',
        'bodyMeasurements.waist': 'Waist (cm)',
        'bodyMeasurements.chest': 'Chest (cm)',
        'bodyMeasurements.arms': 'Arms (cm)',
        'bodyMeasurements.legs': 'Legs (cm)',
        'bodyMeasurements.progressPhotos': 'Progress Photos',
        'bodyMeasurements.progressPhotosHint': 'Take photos from front, side, and back.',
        'bodyMeasurements.upload.front': 'Front Photo',
        'bodyMeasurements.upload.side': 'Side Photo',
        'bodyMeasurements.upload.back': 'Back Photo',
        'bodyMeasurements.photosComingSoon': 'Photo upload coming soon.',
        'bodyMeasurements.skip': 'Skip',
        'bodyMeasurements.save': 'Save Measurements',
        weightPositive: 'Weight must be positive',
        heightPositive: 'Height must be positive',
      };
      return translations[key] ?? key;
    },
    i18n: { language: 'en' },
  }),
}));

describe('BodyMeasurementsForm', () => {
  const mockOnSubmit = vi.fn();
  const mockOnSkip = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  function renderForm(props = {}) {
    return render(
      <BodyMeasurementsForm
        onSubmit={mockOnSubmit}
        onSkip={mockOnSkip}
        {...props}
      />,
    );
  }

  it('renders title, measurement fields, and photo upload placeholders', () => {
    renderForm();

    expect(screen.getByText('Body Measurements')).toBeInTheDocument();
    expect(screen.getByRole('spinbutton', { name: /weight/i })).toBeInTheDocument();
    expect(screen.getByRole('spinbutton', { name: /height/i })).toBeInTheDocument();
    expect(screen.getByRole('spinbutton', { name: /waist/i })).toBeInTheDocument();
    expect(screen.getByRole('spinbutton', { name: /chest/i })).toBeInTheDocument();
    expect(screen.getByRole('spinbutton', { name: /arms/i })).toBeInTheDocument();
    expect(screen.getByRole('spinbutton', { name: /legs/i })).toBeInTheDocument();
    expect(screen.getByText('Progress Photos')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Front Photo' })).toBeDisabled();
    expect(screen.getByRole('button', { name: 'Side Photo' })).toBeDisabled();
    expect(screen.getByRole('button', { name: 'Back Photo' })).toBeDisabled();
  });

  it('calls onSkip when skip button is clicked', async () => {
    const user = userEvent.setup();
    renderForm();

    await user.click(screen.getByRole('button', { name: 'Skip' }));
    expect(mockOnSkip).toHaveBeenCalledTimes(1);
  });

  it('submits form with measurement values', async () => {
    const user = userEvent.setup();
    renderForm();

    await user.type(screen.getByRole('spinbutton', { name: /weight/i }), '75');
    await user.type(screen.getByRole('spinbutton', { name: /height/i }), '180');
    await user.click(screen.getByRole('button', { name: 'Save Measurements' }));

    await waitFor(() => {
      expect(mockOnSubmit).toHaveBeenCalledTimes(1);
      const callArg = mockOnSubmit.mock.calls[0][0];
      expect(callArg.weight).toBe(75);
      expect(callArg.height).toBe(180);
    });
  });

  it('shows error alert when error prop is provided', () => {
    renderForm({ error: 'Something went wrong' });
    expect(screen.getByRole('alert')).toHaveTextContent('Something went wrong');
  });

  it('disables save button when isLoading is true', () => {
    renderForm({ isLoading: true });
    expect(screen.getByRole('button', { name: 'Save Measurements' })).toBeDisabled();
  });
});
