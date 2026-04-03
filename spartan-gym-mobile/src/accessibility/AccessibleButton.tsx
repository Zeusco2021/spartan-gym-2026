/**
 * AccessibleButton — Button component meeting accessibility standards
 *
 * Has proper accessibilityRole="button", accessibilityLabel, accessibilityHint.
 * Meets minimum touch target size: 44x44 (Apple HIG) / 48x48 (Material).
 * Supports high contrast mode.
 *
 * Validates: Requirements 29.1, 29.4, 29.6, 29.7
 */

import React from 'react';
import {
  TouchableOpacity,
  TouchableOpacityProps,
  Text,
  StyleSheet,
  Platform,
} from 'react-native';
import { getContrastColors } from './highContrast';

export interface AccessibleButtonProps extends TouchableOpacityProps {
  /** Button label text */
  label: string;
  /** Accessibility hint describing what happens when pressed */
  accessibilityHint?: string;
  /** Visual variant */
  variant?: 'primary' | 'secondary' | 'danger';
}

/** Minimum touch target: 48dp (Android Material) / 44pt (iOS HIG) */
const MIN_TOUCH_TARGET = Platform.OS === 'ios' ? 44 : 48;

export const AccessibleButton: React.FC<AccessibleButtonProps> = ({
  label,
  accessibilityHint,
  variant = 'primary',
  style,
  disabled,
  ...rest
}) => {
  const colors = getContrastColors();

  const bgColor = disabled
    ? colors.disabled
    : variant === 'primary'
      ? colors.primary
      : variant === 'danger'
        ? colors.error
        : colors.surface;

  const textColor = variant === 'secondary' && !disabled
    ? colors.primary
    : colors.textOnPrimary;

  return (
    <TouchableOpacity
      accessibilityRole="button"
      accessibilityLabel={label}
      accessibilityHint={accessibilityHint}
      accessibilityState={{ disabled: !!disabled }}
      disabled={disabled}
      style={[
        styles.button,
        { backgroundColor: bgColor },
        variant === 'secondary' && styles.secondaryBorder,
        style,
      ]}
      {...rest}
    >
      <Text
        style={[styles.label, { color: textColor }]}
        allowFontScaling={true}
        maxFontSizeMultiplier={1.5}
      >
        {label}
      </Text>
    </TouchableOpacity>
  );
};

const styles = StyleSheet.create({
  button: {
    minHeight: MIN_TOUCH_TARGET,
    minWidth: MIN_TOUCH_TARGET,
    paddingHorizontal: 16,
    paddingVertical: 12,
    borderRadius: 8,
    alignItems: 'center',
    justifyContent: 'center',
  },
  secondaryBorder: {
    borderWidth: 1,
    borderColor: '#1565C0',
  },
  label: {
    fontSize: 16,
    fontWeight: '600',
  },
});
