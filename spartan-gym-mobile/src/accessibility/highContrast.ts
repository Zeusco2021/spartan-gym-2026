/**
 * High Contrast Utilities — Detect system high contrast and provide accessible colors
 *
 * Ensures minimum 4.5:1 contrast ratio for normal text, 3:1 for large text.
 * Provides high contrast color palette that respects OS settings.
 *
 * Validates: Requirements 29.4, 29.6
 */

import { Appearance } from 'react-native';

/** Standard accessible color palette */
export const accessibleColors = {
  primary: '#1565C0',
  primaryDark: '#0D47A1',
  error: '#C62828',
  success: '#2E7D32',
  warning: '#E65100',
  textPrimary: '#212121',
  textSecondary: '#424242',
  textOnPrimary: '#FFFFFF',
  background: '#FAFAFA',
  surface: '#FFFFFF',
  border: '#BDBDBD',
  disabled: '#9E9E9E',
};

/** High contrast color palette — increased contrast ratios */
export const highContrastColors = {
  primary: '#0D47A1',
  primaryDark: '#002171',
  error: '#B71C1C',
  success: '#1B5E20',
  warning: '#BF360C',
  textPrimary: '#000000',
  textSecondary: '#212121',
  textOnPrimary: '#FFFFFF',
  background: '#FFFFFF',
  surface: '#FFFFFF',
  border: '#000000',
  disabled: '#616161',
};

/**
 * Detect if the system is using a high contrast / dark mode setting.
 * On iOS, this maps to "Increase Contrast" accessibility setting.
 * On Android, this maps to "High contrast text" setting.
 */
export function isHighContrastEnabled(): boolean {
  // React Native exposes color scheme; high contrast is approximated
  // via dark mode detection. For full high contrast detection,
  // a native module would be needed.
  return Appearance.getColorScheme() === 'dark';
}

/**
 * Returns the appropriate color palette based on system contrast settings.
 */
export function getContrastColors() {
  return isHighContrastEnabled() ? highContrastColors : accessibleColors;
}

/**
 * Calculate relative luminance of a hex color (WCAG 2.1 formula).
 */
export function relativeLuminance(hex: string): number {
  const rgb = hexToRgb(hex);
  const [r, g, b] = rgb.map((c) => {
    const s = c / 255;
    return s <= 0.03928 ? s / 12.92 : Math.pow((s + 0.055) / 1.055, 2.4);
  });
  return 0.2126 * r + 0.7152 * g + 0.0722 * b;
}

/**
 * Calculate contrast ratio between two hex colors.
 */
export function contrastRatio(hex1: string, hex2: string): number {
  const l1 = relativeLuminance(hex1);
  const l2 = relativeLuminance(hex2);
  const lighter = Math.max(l1, l2);
  const darker = Math.min(l1, l2);
  return (lighter + 0.05) / (darker + 0.05);
}

/**
 * Check if contrast ratio meets WCAG AA for normal text (4.5:1).
 */
export function meetsNormalTextContrast(foreground: string, background: string): boolean {
  return contrastRatio(foreground, background) >= 4.5;
}

/**
 * Check if contrast ratio meets WCAG AA for large text (3:1).
 */
export function meetsLargeTextContrast(foreground: string, background: string): boolean {
  return contrastRatio(foreground, background) >= 3.0;
}

function hexToRgb(hex: string): number[] {
  const clean = hex.replace('#', '');
  return [
    parseInt(clean.substring(0, 2), 16),
    parseInt(clean.substring(2, 4), 16),
    parseInt(clean.substring(4, 6), 16),
  ];
}
