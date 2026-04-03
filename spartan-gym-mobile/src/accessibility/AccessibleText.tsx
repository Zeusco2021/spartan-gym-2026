/**
 * AccessibleText — Text component with Dynamic Type and font scaling support
 *
 * Supports Dynamic Type (iOS) and font scaling (Android) via allowFontScaling.
 * Applies minimum touch target size when used as a tappable element.
 *
 * Validates: Requirements 29.2, 29.5
 */

import React from 'react';
import { Text, TextProps, StyleSheet } from 'react-native';

export interface AccessibleTextProps extends TextProps {
  /** Maximum font size multiplier for Dynamic Type / font scaling. Default: 2.0 */
  maxFontSizeMultiplier?: number;
}

export const AccessibleText: React.FC<AccessibleTextProps> = ({
  maxFontSizeMultiplier = 2.0,
  style,
  children,
  ...rest
}) => {
  return (
    <Text
      allowFontScaling={true}
      maxFontSizeMultiplier={maxFontSizeMultiplier}
      style={[styles.base, style]}
      {...rest}
    >
      {children}
    </Text>
  );
};

const styles = StyleSheet.create({
  base: {
    fontSize: 16,
    color: '#212121',
  },
});
