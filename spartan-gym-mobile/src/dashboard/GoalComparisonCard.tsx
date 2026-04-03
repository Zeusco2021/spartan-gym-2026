/**
 * Goal Comparison Card — Weekly/daily/monthly goal progress with visual indicators
 *
 * Shows progress bars, percentages, and color coding:
 * - Green: on track or ahead (≥80%)
 * - Yellow: slightly behind (50-79%)
 * - Red: significantly behind (<50%)
 *
 * Validates: Requirement 4.8
 */

import React from 'react';
import { View, Text, StyleSheet, Platform } from 'react-native';

export type GoalPeriod = 'daily' | 'weekly' | 'monthly';

export interface GoalData {
  label: string;
  current: number;
  target: number;
  unit: string;
}

export interface GoalComparisonCardProps {
  period: GoalPeriod;
  goals: GoalData[];
}

function getPeriodLabel(period: GoalPeriod): string {
  switch (period) {
    case 'daily': return 'Hoy';
    case 'weekly': return 'Esta Semana';
    case 'monthly': return 'Este Mes';
  }
}

function getProgressColor(percent: number): string {
  if (percent >= 80) return '#4CAF50';
  if (percent >= 50) return '#FF9800';
  return '#F44336';
}

function getStatusLabel(percent: number): string {
  if (percent >= 100) return 'Completado';
  if (percent >= 80) return 'En camino';
  if (percent >= 50) return 'Ligeramente atrás';
  return 'Atrás';
}

export const GoalComparisonCard: React.FC<GoalComparisonCardProps> = ({ period, goals }) => {
  const minTarget = Platform.OS === 'ios' ? 44 : 48;

  return (
    <View style={styles.card} accessibilityRole="summary" accessibilityLabel={`Objetivos ${getPeriodLabel(period)}`}>
      <Text style={styles.periodTitle} allowFontScaling maxFontSizeMultiplier={2.0} accessibilityRole="header">{getPeriodLabel(period)}</Text>

      {goals.map((goal, index) => {
        const percent = goal.target > 0 ? Math.min(100, Math.round((goal.current / goal.target) * 100)) : 0;
        const color = getProgressColor(percent);
        const statusLabel = getStatusLabel(percent);

        return (
          <View key={index} style={styles.goalRow} accessibilityLabel={`${goal.label}: ${goal.current} de ${goal.target} ${goal.unit}, ${percent} por ciento, ${statusLabel}`}>
            <View style={styles.goalHeader}>
              <Text style={styles.goalLabel} allowFontScaling maxFontSizeMultiplier={2.0}>{goal.label}</Text>
              <Text style={[styles.goalPercent, { color }]} allowFontScaling maxFontSizeMultiplier={2.0}>{percent}%</Text>
            </View>

            <View
              style={styles.progressBarBg}
              accessibilityRole="progressbar"
              accessibilityValue={{ min: 0, max: 100, now: percent }}
              accessibilityLabel={`Progreso de ${goal.label}: ${percent} por ciento`}
            >
              <View style={[styles.progressBarFill, { width: `${percent}%`, backgroundColor: color }]} />
            </View>

            <View style={styles.goalFooter}>
              <Text style={styles.goalValues} allowFontScaling maxFontSizeMultiplier={2.0}>
                {goal.current} / {goal.target} {goal.unit}
              </Text>
              <Text style={[styles.statusBadge, { color }]} allowFontScaling maxFontSizeMultiplier={2.0}>{statusLabel}</Text>
            </View>
          </View>
        );
      })}
    </View>
  );
};

const styles = StyleSheet.create({
  card: {
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 16,
    marginBottom: 12,
    elevation: 2,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 3,
  },
  periodTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#212121',
    marginBottom: 12,
  },
  goalRow: {
    marginBottom: 14,
  },
  goalHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 6,
  },
  goalLabel: {
    fontSize: 14,
    color: '#424242',
    fontWeight: '500',
  },
  goalPercent: {
    fontSize: 14,
    fontWeight: 'bold',
  },
  progressBarBg: {
    height: 8,
    backgroundColor: '#E0E0E0',
    borderRadius: 4,
    overflow: 'hidden',
  },
  progressBarFill: {
    height: '100%',
    borderRadius: 4,
  },
  goalFooter: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginTop: 4,
  },
  goalValues: {
    fontSize: 12,
    color: '#757575',
  },
  statusBadge: {
    fontSize: 12,
    fontWeight: '600',
  },
});
