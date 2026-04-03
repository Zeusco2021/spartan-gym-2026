/**
 * Accessibility Utilities — Central export for all accessibility modules
 *
 * Validates: Requirements 29.1, 29.2, 29.4, 29.5, 29.6, 29.7
 */

export { useAccessibility } from './useAccessibility';
export type { AccessibilityState } from './useAccessibility';

export { AccessibleText } from './AccessibleText';
export type { AccessibleTextProps } from './AccessibleText';

export { AccessibleButton } from './AccessibleButton';
export type { AccessibleButtonProps } from './AccessibleButton';

export {
  accessibleColors,
  highContrastColors,
  isHighContrastEnabled,
  getContrastColors,
  relativeLuminance,
  contrastRatio,
  meetsNormalTextContrast,
  meetsLargeTextContrast,
} from './highContrast';
