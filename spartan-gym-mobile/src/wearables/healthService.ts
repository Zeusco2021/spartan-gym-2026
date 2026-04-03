/**
 * Wearable Health Service — Abstracted interface for biometric data
 *
 * Platform-specific implementations for HealthKit (iOS) and Google Fit (Android).
 * Uses react-native-health (iOS) and react-native-google-fit (Android).
 *
 * Validates: Requirement 8.1
 */

import { Platform } from 'react-native';

// ─── Types ──────────────────────────────────────────────────────

export interface HeartRateReading {
  bpm: number;
  timestamp: string;
}

export interface StepsReading {
  count: number;
  date: string;
}

export interface CaloriesReading {
  kcal: number;
  date: string;
}

export interface SleepReading {
  durationMinutes: number;
  quality: 'poor' | 'fair' | 'good' | 'excellent';
  date: string;
}

export interface WorkoutData {
  type: string;
  startTime: string;
  endTime: string;
  caloriesBurned: number;
  heartRateAvg?: number;
}

export interface IHealthService {
  connect(): Promise<boolean>;
  disconnect(): Promise<void>;
  isConnected(): boolean;
  readHeartRate(): Promise<HeartRateReading[]>;
  readSteps(): Promise<StepsReading>;
  readCalories(): Promise<CaloriesReading>;
  readSleep(): Promise<SleepReading | null>;
  writeWorkout(data: WorkoutData): Promise<boolean>;
}

// ─── HealthKit Service (iOS) ────────────────────────────────────

export class HealthKitService implements IHealthService {
  private connected = false;

  async connect(): Promise<boolean> {
    try {
      const AppleHealthKit = require('react-native-health').default;
      const permissions = {
        permissions: {
          read: [
            AppleHealthKit.Constants.Permissions.HeartRate,
            AppleHealthKit.Constants.Permissions.StepCount,
            AppleHealthKit.Constants.Permissions.ActiveEnergyBurned,
            AppleHealthKit.Constants.Permissions.SleepAnalysis,
          ],
          write: [AppleHealthKit.Constants.Permissions.Workout],
        },
      };

      return new Promise((resolve) => {
        AppleHealthKit.initHealthKit(permissions, (err: unknown) => {
          this.connected = !err;
          resolve(!err);
        });
      });
    } catch {
      this.connected = false;
      return false;
    }
  }

  async disconnect(): Promise<void> {
    this.connected = false;
  }

  isConnected(): boolean {
    return this.connected;
  }

  async readHeartRate(): Promise<HeartRateReading[]> {
    if (!this.connected) return [];
    try {
      const AppleHealthKit = require('react-native-health').default;
      const options = {
        startDate: new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString(),
        endDate: new Date().toISOString(),
      };
      return new Promise((resolve) => {
        AppleHealthKit.getHeartRateSamples(options, (err: unknown, results: Array<{ value: number; startDate: string }>) => {
          if (err || !results) return resolve([]);
          resolve(results.map((r) => ({ bpm: r.value, timestamp: r.startDate })));
        });
      });
    } catch {
      return [];
    }
  }

  async readSteps(): Promise<StepsReading> {
    if (!this.connected) return { count: 0, date: new Date().toISOString() };
    try {
      const AppleHealthKit = require('react-native-health').default;
      return new Promise((resolve) => {
        AppleHealthKit.getStepCount({}, (err: unknown, result: { value: number }) => {
          resolve({
            count: err ? 0 : result?.value ?? 0,
            date: new Date().toISOString(),
          });
        });
      });
    } catch {
      return { count: 0, date: new Date().toISOString() };
    }
  }

  async readCalories(): Promise<CaloriesReading> {
    if (!this.connected) return { kcal: 0, date: new Date().toISOString() };
    try {
      const AppleHealthKit = require('react-native-health').default;
      return new Promise((resolve) => {
        AppleHealthKit.getActiveEnergyBurned(
          { startDate: new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString() },
          (err: unknown, results: Array<{ value: number }>) => {
            const total = err ? 0 : results?.reduce((sum, r) => sum + r.value, 0) ?? 0;
            resolve({ kcal: Math.round(total), date: new Date().toISOString() });
          },
        );
      });
    } catch {
      return { kcal: 0, date: new Date().toISOString() };
    }
  }

  async readSleep(): Promise<SleepReading | null> {
    if (!this.connected) return null;
    try {
      const AppleHealthKit = require('react-native-health').default;
      return new Promise((resolve) => {
        AppleHealthKit.getSleepSamples(
          { startDate: new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString() },
          (err: unknown, results: Array<{ value: string; startDate: string; endDate: string }>) => {
            if (err || !results?.length) return resolve(null);
            const last = results[results.length - 1];
            const mins = (new Date(last.endDate).getTime() - new Date(last.startDate).getTime()) / 60000;
            resolve({
              durationMinutes: Math.round(mins),
              quality: mins >= 420 ? 'excellent' : mins >= 360 ? 'good' : mins >= 300 ? 'fair' : 'poor',
              date: last.startDate,
            });
          },
        );
      });
    } catch {
      return null;
    }
  }

  async writeWorkout(data: WorkoutData): Promise<boolean> {
    if (!this.connected) return false;
    try {
      const AppleHealthKit = require('react-native-health').default;
      return new Promise((resolve) => {
        AppleHealthKit.saveWorkout(
          { type: data.type, startDate: data.startTime, endDate: data.endTime, energyBurned: data.caloriesBurned },
          (err: unknown) => resolve(!err),
        );
      });
    } catch {
      return false;
    }
  }
}

// ─── Google Fit Service (Android) ───────────────────────────────

export class GoogleFitService implements IHealthService {
  private connected = false;

  async connect(): Promise<boolean> {
    try {
      const GoogleFit = require('react-native-google-fit').default;
      const options = { scopes: ['https://www.googleapis.com/auth/fitness.activity.read', 'https://www.googleapis.com/auth/fitness.body.read'] };
      const result = await GoogleFit.authorize(options);
      this.connected = result.success;
      return result.success;
    } catch {
      this.connected = false;
      return false;
    }
  }

  async disconnect(): Promise<void> {
    try {
      const GoogleFit = require('react-native-google-fit').default;
      await GoogleFit.disconnect();
    } catch { /* ignore */ }
    this.connected = false;
  }

  isConnected(): boolean {
    return this.connected;
  }

  async readHeartRate(): Promise<HeartRateReading[]> {
    if (!this.connected) return [];
    try {
      const GoogleFit = require('react-native-google-fit').default;
      const options = {
        startDate: new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString(),
        endDate: new Date().toISOString(),
      };
      const results = await GoogleFit.getHeartRateSamples(options);
      return (results ?? []).map((r: { value: number; startDate: string }) => ({
        bpm: r.value,
        timestamp: r.startDate,
      }));
    } catch {
      return [];
    }
  }

  async readSteps(): Promise<StepsReading> {
    if (!this.connected) return { count: 0, date: new Date().toISOString() };
    try {
      const GoogleFit = require('react-native-google-fit').default;
      const results = await GoogleFit.getDailyStepCountSamples({
        startDate: new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString(),
        endDate: new Date().toISOString(),
      });
      const estimated = results?.find((r: { source: string }) => r.source === 'com.google.android.gms:estimated_steps');
      const steps = estimated?.steps?.[0]?.value ?? 0;
      return { count: steps, date: new Date().toISOString() };
    } catch {
      return { count: 0, date: new Date().toISOString() };
    }
  }

  async readCalories(): Promise<CaloriesReading> {
    if (!this.connected) return { kcal: 0, date: new Date().toISOString() };
    try {
      const GoogleFit = require('react-native-google-fit').default;
      const results = await GoogleFit.getDailyCalorieSamples({
        startDate: new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString(),
        endDate: new Date().toISOString(),
      });
      const total = results?.reduce((sum: number, r: { calorie: number }) => sum + (r.calorie ?? 0), 0) ?? 0;
      return { kcal: Math.round(total), date: new Date().toISOString() };
    } catch {
      return { kcal: 0, date: new Date().toISOString() };
    }
  }

  async readSleep(): Promise<SleepReading | null> {
    if (!this.connected) return null;
    try {
      const GoogleFit = require('react-native-google-fit').default;
      const results = await GoogleFit.getSleepSamples({
        startDate: new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString(),
        endDate: new Date().toISOString(),
      });
      if (!results?.length) return null;
      const last = results[results.length - 1];
      const mins = (new Date(last.endDate).getTime() - new Date(last.startDate).getTime()) / 60000;
      return {
        durationMinutes: Math.round(mins),
        quality: mins >= 420 ? 'excellent' : mins >= 360 ? 'good' : mins >= 300 ? 'fair' : 'poor',
        date: last.startDate,
      };
    } catch {
      return null;
    }
  }

  async writeWorkout(data: WorkoutData): Promise<boolean> {
    if (!this.connected) return false;
    try {
      const GoogleFit = require('react-native-google-fit').default;
      await GoogleFit.saveWorkout({
        startDate: data.startTime,
        endDate: data.endTime,
        calories: data.caloriesBurned,
        activityType: data.type,
      });
      return true;
    } catch {
      return false;
    }
  }
}

// ─── Factory ────────────────────────────────────────────────────

export function createHealthService(): IHealthService {
  return Platform.OS === 'ios' ? new HealthKitService() : new GoogleFitService();
}
