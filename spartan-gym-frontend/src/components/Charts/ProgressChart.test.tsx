import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import ProgressChart from './ProgressChart';
import type { ProgressDataPoint } from '@/types';

vi.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string) => {
      const translations: Record<string, string> = {
        selectPeriod: 'Select period',
        day: 'Day',
        month: 'Month',
        year: 'Year',
        noData: 'No data available',
        progressChart: 'Progress chart',
        volume: 'Volume',
        duration: 'Duration',
        calories: 'Calories',
      };
      return translations[key] ?? key;
    },
    i18n: { language: 'en' },
  }),
}));

// Mock Recharts to avoid rendering issues in jsdom
vi.mock('recharts', () => ({
  ResponsiveContainer: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="responsive-container">{children}</div>
  ),
  LineChart: ({ children, ...props }: Record<string, unknown>) => (
    <div data-testid="line-chart" role="img" aria-label={props['aria-label'] as string}>
      {children as React.ReactNode}
    </div>
  ),
  Line: () => <div data-testid="line" />,
  XAxis: () => <div />,
  YAxis: () => <div />,
  CartesianGrid: () => <div />,
  Tooltip: () => <div />,
  Legend: () => <div />,
}));

const mockData: ProgressDataPoint[] = [
  { date: '2024-01-01', volume: 5000, duration: 60, caloriesBurned: 400 },
  { date: '2024-01-02', volume: 6000, duration: 75, caloriesBurned: 500 },
  { date: '2024-01-03', volume: 4500, duration: 50, caloriesBurned: 350 },
];

describe('ProgressChart', () => {
  it('renders period toggle buttons with aria-labels', () => {
    render(<ProgressChart data={mockData} />);

    expect(screen.getByRole('button', { name: 'Day' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Month' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Year' })).toBeInTheDocument();
  });

  it('renders the toggle group with aria-label', () => {
    render(<ProgressChart data={mockData} />);

    expect(screen.getByRole('group', { name: 'Select period' })).toBeInTheDocument();
  });

  it('renders chart when data is provided', () => {
    render(<ProgressChart data={mockData} />);

    expect(screen.getByTestId('responsive-container')).toBeInTheDocument();
    expect(screen.getByTestId('line-chart')).toBeInTheDocument();
  });

  it('shows no data message when data is empty', () => {
    render(<ProgressChart data={[]} />);

    expect(screen.getByText('No data available')).toBeInTheDocument();
    expect(screen.queryByTestId('line-chart')).not.toBeInTheDocument();
  });

  it('renders title when provided', () => {
    render(<ProgressChart data={mockData} title="My Progress" />);

    expect(screen.getByText('My Progress')).toBeInTheDocument();
  });

  it('calls onPeriodChange when period is toggled', async () => {
    const onPeriodChange = vi.fn();
    const user = userEvent.setup();

    render(
      <ProgressChart data={mockData} onPeriodChange={onPeriodChange} />,
    );

    await user.click(screen.getByRole('button', { name: 'Year' }));

    expect(onPeriodChange).toHaveBeenCalledWith('year');
  });

  it('defaults to month period', () => {
    render(<ProgressChart data={mockData} />);

    const monthButton = screen.getByRole('button', { name: 'Month' });
    expect(monthButton).toHaveAttribute('aria-pressed', 'true');
  });

  it('respects defaultPeriod prop', () => {
    render(<ProgressChart data={mockData} defaultPeriod="day" />);

    const dayButton = screen.getByRole('button', { name: 'Day' });
    expect(dayButton).toHaveAttribute('aria-pressed', 'true');
  });
});
