import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import DailyBalanceCard from './DailyBalanceCard';
import type { DailyBalance, NutritionPlan } from '@/types';

vi.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string) => {
      const translations: Record<string, string> = {
        dailyBalance: 'Daily Balance',
        calories: 'Calories',
        protein: 'Protein',
        carbs: 'Carbs',
        fat: 'Fat',
        kcal: 'kcal',
        grams: 'g',
        remaining: 'Remaining',
        error: 'Error',
        loading: 'Loading',
        muscle_gain: 'Muscle Gain',
      };
      return translations[key] ?? key;
    },
    i18n: { language: 'en' },
  }),
}));

const mockBalance: DailyBalance = {
  date: '2024-01-15',
  totalCalories: 1200,
  totalProtein: 80,
  totalCarbs: 150,
  totalFat: 40,
  targetCalories: 2000,
  targetProtein: 150,
  targetCarbs: 250,
  targetFat: 70,
};

const mockPlan: NutritionPlan = {
  id: '1',
  userId: 'u1',
  goal: 'muscle_gain',
  dailyCalories: 2000,
  proteinGrams: 150,
  carbsGrams: 250,
  fatGrams: 70,
};

describe('DailyBalanceCard', () => {
  it('renders all macro progress bars with labels', () => {
    render(<DailyBalanceCard balance={mockBalance} plan={mockPlan} />);

    expect(screen.getByText('Daily Balance')).toBeInTheDocument();
    expect(screen.getByText('Calories')).toBeInTheDocument();
    expect(screen.getByText('Protein')).toBeInTheDocument();
    expect(screen.getByText('Carbs')).toBeInTheDocument();
    expect(screen.getByText('Fat')).toBeInTheDocument();
  });

  it('shows consumed vs target values', () => {
    render(<DailyBalanceCard balance={mockBalance} />);

    expect(screen.getByText('1200 / 2000 kcal')).toBeInTheDocument();
    expect(screen.getByText('80 / 150 g')).toBeInTheDocument();
    expect(screen.getByText('150 / 250 g')).toBeInTheDocument();
    expect(screen.getByText('40 / 70 g')).toBeInTheDocument();
  });

  it('shows remaining values', () => {
    render(<DailyBalanceCard balance={mockBalance} />);

    expect(screen.getByText('Remaining: 800 kcal')).toBeInTheDocument();
    expect(screen.getByText('Remaining: 70 g')).toBeInTheDocument();
  });

  it('renders plan goal chip when plan is provided', () => {
    render(<DailyBalanceCard balance={mockBalance} plan={mockPlan} />);
    expect(screen.getByText('Muscle Gain')).toBeInTheDocument();
  });

  it('does not render goal chip when no plan', () => {
    render(<DailyBalanceCard balance={mockBalance} />);
    expect(screen.queryByText('Muscle Gain')).not.toBeInTheDocument();
  });

  it('shows loading state', () => {
    render(<DailyBalanceCard balance={undefined} isLoading />);
    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });

  it('shows error state', () => {
    render(<DailyBalanceCard balance={undefined} isError />);
    expect(screen.getByText('Error')).toBeInTheDocument();
  });

  it('renders nothing when no balance and not loading/error', () => {
    const { container } = render(<DailyBalanceCard balance={undefined} />);
    expect(container.innerHTML).toBe('');
  });

  it('has aria-label on the card', () => {
    render(<DailyBalanceCard balance={mockBalance} />);
    expect(screen.getByLabelText('Daily Balance')).toBeInTheDocument();
  });

  it('has aria-labels on progress bars', () => {
    render(<DailyBalanceCard balance={mockBalance} />);
    expect(screen.getByLabelText('Calories 60%')).toBeInTheDocument();
    expect(screen.getByLabelText('Protein 53%')).toBeInTheDocument();
  });

  it('caps progress at 100% when consumed exceeds target', () => {
    const overBalance: DailyBalance = {
      ...mockBalance,
      totalCalories: 2500,
      targetCalories: 2000,
    };
    render(<DailyBalanceCard balance={overBalance} />);
    // The progress bar should be capped at 100
    expect(screen.getByLabelText('Calories 100%')).toBeInTheDocument();
    // Remaining should be 0
    expect(screen.getByText('Remaining: 0 kcal')).toBeInTheDocument();
  });
});
