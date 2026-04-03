/**
 * useAccessibility — Custom hook for detecting system accessibility settings
 *
 * Detects VoiceOver/TalkBack, bold text, reduce motion preferences.
 * Provides announceForAccessibility helper for live region announcements.
 *
 * Validates: Requirements 29.1, 29.2, 29.5
 */

import { useEffect, useState, useCallback } from 'react';
import { AccessibilityInfo, Platform } from 'react-native';

export interface AccessibilityState {
  isScreenReaderEnabled: boolean;
  isBoldTextEnabled: boolean;
  isReduceMotionEnabled: boolean;
  announceForAccessibility: (message: string) => void;
}

export function useAccessibility(): AccessibilityState {
  const [isScreenReaderEnabled, setScreenReaderEnabled] = useState(false);
  const [isBoldTextEnabled, setBoldTextEnabled] = useState(false);
  const [isReduceMotionEnabled, setReduceMotionEnabled] = useState(false);

  useEffect(() => {
    AccessibilityInfo.isScreenReaderEnabled().then(setScreenReaderEnabled);
    AccessibilityInfo.isReduceMotionEnabled().then(setReduceMotionEnabled);

    if (Platform.OS === 'ios') {
      AccessibilityInfo.isBoldTextEnabled().then(setBoldTextEnabled);
    }

    const screenReaderSub = AccessibilityInfo.addEventListener(
      'screenReaderChanged',
      setScreenReaderEnabled,
    );
    const reduceMotionSub = AccessibilityInfo.addEventListener(
      'reduceMotionChanged',
      setReduceMotionEnabled,
    );
    const boldTextSub = Platform.OS === 'ios'
      ? AccessibilityInfo.addEventListener('boldTextChanged', setBoldTextEnabled)
      : null;

    return () => {
      screenReaderSub.remove();
      reduceMotionSub.remove();
      boldTextSub?.remove();
    };
  }, []);

  const announceForAccessibility = useCallback((message: string) => {
    AccessibilityInfo.announceForAccessibility(message);
  }, []);

  return {
    isScreenReaderEnabled,
    isBoldTextEnabled,
    isReduceMotionEnabled,
    announceForAccessibility,
  };
}
