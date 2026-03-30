import { describe, it, expect } from 'vitest';
import { lightTheme, darkTheme } from '@/theme/muiTheme';

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
