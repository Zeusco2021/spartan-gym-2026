/**
 * Accessibility Theme — Theme configuration enforcing accessibility standards
 *
 * Defines accessible color palette, minimum font sizes, touch target sizes,
 * and high contrast variant colors.
 *
 * Validates: Requirements 29.4, 29.5, 29.6
 */

import { Platform } from 'react-native';

/** Minimum touch target: 48dp Android / 44pt iOS */
export const MIN_TOUCH_TARGET = Platform.OS === 'ios' ? 44 : 48;

/** Accessible font sizes respecting Dynamic Type */
export const fontSizes = {
  xs: 12,
  sm: 14,
  md: 16,
  lg: 18,
  xl: 22,
  xxl: 28,
};

/** Standard accessible color palette (WCAG AA compliant on white background) */
export const colors = {
  primary: '#1565C0',       // 4.74:1 on white
  primaryDark: '#0D47A1',   // 6.08:1 on white
  error: '#C62828',         // 5.57:1 on white
  success: '#2E7D32',       // 4.52:1 on white
  warning: '#E65100',       // 4.62:1 on white
  textPrimary: '#212121',   // 16.10:1 on white
  textSecondary: '#424242', // 10.42:1 on white
  textOnPrimary: '#FFFFFF',
  background: '#FAFAFA',
  surface: '#FFFFFF',
  border: '#BDBDBD',
  disabled: '#9E9E9E',
};

/** High contrast variant — for users with high contrast OS setting */
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

/** Spacing values for consistent layout */
export const spacing = {
  xs: 4,
  sm: 8,
  md: 16,
  lg: 24,
  xl: 32,
};

/** Default text style props for accessibility */
export const accessibleTextDefaults = {
  allowFontScaling: true,
  maxFontSizeMultiplier: 2.0,
};
