/**
 * Wearable Screen — Shows connected device status and recent biometric data
 *
 * Validates: Requirement 8.1
 */

import React, { useCallback, useEffect, useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, ActivityIndicator, ScrollView, Platform } from 'react-native';
import { useAccessibility } from '../accessibility';
import {
  createHealthService,
  type IHealthService,
  type HeartRateReading,
  type StepsReading,
  type CaloriesReading,
  type SleepReading,
} from './healthService';

interface BiometricData {
  heartRate: HeartRateReading[];
  steps: StepsReading | null;
  calories: CaloriesReading | null;
  sleep: SleepReading | null;
}

export const WearableScreen: React.FC = () => {
  const [service] = useState<IHealthService>(() => createHealthService());
  const [connected, setConnected] = useState(false);
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState<BiometricData>({ heartRate: [], steps: null, calories: null, sleep: null });

  const handleConnect = useCallback(async () => {
    setLoading(true);
    const ok = await service.connect();
    setConnected(ok);
    setLoading(false);
  }, [service]);

  const handleDisconnect = useCallback(async () => {
    await service.disconnect();
    setConnected(false);
    setData({ heartRate: [], steps: null, calories: null, sleep: null });
  }, [service]);

  const loadData = useCallback(async () => {
    if (!service.isConnected()) return;
    setLoading(true);
    const [heartRate, steps, calories, sleep] = await Promise.all([
      service.readHeartRate(),
      service.readSteps(),
      service.readCalories(),
      service.readSleep(),
    ]);
    setData({ heartRate, steps, calories, sleep });
    setLoading(false);
  }, [service]);

  useEffect(() => {
    if (connected) { loadData(); }
  }, [connected, loadData]);

  const latestHR = data.heartRate.length > 0 ? data.heartRate[data.heartRate.length - 1] : null;
  const minTarget = Platform.OS === 'ios' ? 44 : 48;

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <Text style={styles.title} allowFontScaling maxFontSizeMultiplier={2.0}>Dispositivos Wearable</Text>

      <View style={styles.statusCard} accessibilityRole="summary" accessibilityLabel={`Estado del dispositivo: ${connected ? 'Conectado' : 'Desconectado'}`}>
        <View style={[styles.statusDot, { backgroundColor: connected ? '#4CAF50' : '#9E9E9E' }]} />
        <Text style={styles.statusText} allowFontScaling maxFontSizeMultiplier={2.0}>{connected ? 'Conectado' : 'Desconectado'}</Text>
      </View>

      <TouchableOpacity
        style={[styles.button, { minHeight: minTarget }, connected ? styles.disconnectBtn : styles.connectBtn]}
        onPress={connected ? handleDisconnect : handleConnect}
        disabled={loading}
        accessibilityRole="button"
        accessibilityLabel={connected ? 'Desconectar dispositivo' : 'Conectar dispositivo'}
        accessibilityHint={connected ? 'Desconecta el wearable vinculado' : 'Conecta un dispositivo wearable'}
        accessibilityState={{ disabled: loading }}
      >
        {loading ? (
          <ActivityIndicator color="#fff" />
        ) : (
          <Text style={styles.buttonText} allowFontScaling maxFontSizeMultiplier={1.5}>{connected ? 'Desconectar' : 'Conectar'}</Text>
        )}
      </TouchableOpacity>

      {connected && (
        <View style={styles.dataSection}>
          <Text style={styles.sectionTitle} allowFontScaling maxFontSizeMultiplier={2.0}>Datos Biométricos</Text>

          <View style={styles.metricCard} accessibilityRole="summary" accessibilityLabel={`Frecuencia Cardíaca: ${latestHR ? `${latestHR.bpm} latidos por minuto` : 'sin datos'}`}>
            <Text style={styles.metricLabel} allowFontScaling maxFontSizeMultiplier={2.0}>❤️ Frecuencia Cardíaca</Text>
            <Text style={styles.metricValue} allowFontScaling maxFontSizeMultiplier={2.0}>
              {latestHR ? `${latestHR.bpm} bpm` : '—'}
            </Text>
          </View>

          <View style={styles.metricCard} accessibilityRole="summary" accessibilityLabel={`Pasos hoy: ${data.steps ? data.steps.count.toLocaleString() : 'sin datos'}`}>
            <Text style={styles.metricLabel} allowFontScaling maxFontSizeMultiplier={2.0}>🚶 Pasos Hoy</Text>
            <Text style={styles.metricValue} allowFontScaling maxFontSizeMultiplier={2.0}>
              {data.steps ? data.steps.count.toLocaleString() : '—'}
            </Text>
          </View>

          <View style={styles.metricCard} accessibilityRole="summary" accessibilityLabel={`Calorías quemadas: ${data.calories ? `${data.calories.kcal} kilocalorías` : 'sin datos'}`}>
            <Text style={styles.metricLabel} allowFontScaling maxFontSizeMultiplier={2.0}>🔥 Calorías Quemadas</Text>
            <Text style={styles.metricValue} allowFontScaling maxFontSizeMultiplier={2.0}>
              {data.calories ? `${data.calories.kcal} kcal` : '—'}
            </Text>
          </View>

          <View style={styles.metricCard} accessibilityRole="summary" accessibilityLabel={`Sueño: ${data.sleep ? `${Math.floor(data.sleep.durationMinutes / 60)} horas ${data.sleep.durationMinutes % 60} minutos, calidad ${data.sleep.quality}` : 'sin datos'}`}>
            <Text style={styles.metricLabel} allowFontScaling maxFontSizeMultiplier={2.0}>😴 Sueño</Text>
            <Text style={styles.metricValue} allowFontScaling maxFontSizeMultiplier={2.0}>
              {data.sleep ? `${Math.floor(data.sleep.durationMinutes / 60)}h ${data.sleep.durationMinutes % 60}m (${data.sleep.quality})` : '—'}
            </Text>
          </View>

          <TouchableOpacity style={[styles.refreshBtn, { minHeight: minTarget }]} onPress={loadData} disabled={loading} accessibilityRole="button" accessibilityLabel="Actualizar datos" accessibilityHint="Recarga los datos biométricos del wearable" accessibilityState={{ disabled: loading }}>
            <Text style={styles.refreshText} allowFontScaling maxFontSizeMultiplier={1.5}>Actualizar datos</Text>
          </TouchableOpacity>
        </View>
      )}
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#f5f5f5' },
  content: { padding: 16 },
  title: { fontSize: 24, fontWeight: 'bold', marginBottom: 16, color: '#212121' },
  statusCard: { flexDirection: 'row', alignItems: 'center', backgroundColor: '#fff', padding: 16, borderRadius: 8, marginBottom: 16 },
  statusDot: { width: 12, height: 12, borderRadius: 6, marginRight: 8 },
  statusText: { fontSize: 16, color: '#424242' },
  button: { padding: 14, borderRadius: 8, alignItems: 'center', marginBottom: 16 },
  connectBtn: { backgroundColor: '#1976D2' },
  disconnectBtn: { backgroundColor: '#D32F2F' },
  buttonText: { color: '#fff', fontSize: 16, fontWeight: '600' },
  dataSection: { marginTop: 8 },
  sectionTitle: { fontSize: 18, fontWeight: '600', marginBottom: 12, color: '#212121' },
  metricCard: { backgroundColor: '#fff', padding: 16, borderRadius: 8, marginBottom: 8, flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  metricLabel: { fontSize: 14, color: '#616161' },
  metricValue: { fontSize: 18, fontWeight: '600', color: '#212121' },
  refreshBtn: { padding: 12, borderRadius: 8, backgroundColor: '#E3F2FD', alignItems: 'center', marginTop: 8 },
  refreshText: { color: '#1976D2', fontSize: 14, fontWeight: '600' },
});
