import { describe, it, expect } from 'vitest';
import { lightTheme, darkTheme } from '@/theme/muiTheme';
import { contrastRatio, WCAG_AA_NORMAL, WCAG_AA_LARGE } from '@/utils/colorContrast';

describe('MUI Themes', () => {
  it('lightTheme should use light mode with Spartan gold primary', () => {
    expect(lightTheme.palette.mode).toBe('light');
    expect(lightTheme.palette.primary.main).toBe('#D4AF37');
  });

  it('darkTheme should use dark mode with bright gold primary', () => {
    expect(darkTheme.palette.mode).toBe('dark');
    expect(darkTheme.palette.primary.main).toBe('#FFD700');
  });

  it('darkTheme should have dark background colors', () => {
    expect(darkTheme.palette.background.default).toBe('#1A1A1A');
    expect(darkTheme.palette.background.paper).toBe('#2D2D2D');
  });

  it('both themes should use Inter/Roboto font family', () => {
    const expectedFont = 'Inter,Roboto,Helvetica,Arial,sans-serif';
    expect(lightTheme.typography.fontFamily).toBe(expectedFont);
    expect(darkTheme.typography.fontFamily).toBe(expectedFont);
  });
});

describe('WCAG 2.1 AA contrast ratios', () => {
  it('light theme: primary contrastText on primary.main meets AA for large text (≥ 3:1)', () => {
    const ratio = contrastRatio(
      lightTheme.palette.primary.contrastText,
      lightTheme.palette.primary.main,
    );
    expect(ratio).toBeGreaterThanOrEqual(WCAG_AA_LARGE);
  });

  it('dark theme: primary contrastText on primary.main meets AA for large text (≥ 3:1)', () => {
    const ratio = contrastRatio(
      darkTheme.palette.primary.contrastText,
      darkTheme.palette.primary.main,
    );
    expect(ratio).toBeGreaterThanOrEqual(WCAG_AA_LARGE);
  });

  it('light theme: default text on paper background meets AA for normal text (≥ 4.5:1)', () => {
    const textColor = lightTheme.palette.text.primary;
    const bgColor = lightTheme.palette.background.paper;
    const ratio = contrastRatio(textColor, bgColor);
    expect(ratio).toBeGreaterThanOrEqual(WCAG_AA_NORMAL);
  });

  it('dark theme: default text on paper background meets AA for normal text (≥ 4.5:1)', () => {
    const textColor = darkTheme.palette.text.primary;
    const bgColor = darkTheme.palette.background.paper;
    const ratio = contrastRatio(textColor, bgColor);
    expect(ratio).toBeGreaterThanOrEqual(WCAG_AA_NORMAL);
  });
});
