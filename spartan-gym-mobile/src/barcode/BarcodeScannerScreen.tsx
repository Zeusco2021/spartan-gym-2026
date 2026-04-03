/**
 * Barcode Scanner Screen — Scan food barcodes for nutritional info
 *
 * Uses react-native-vision-camera for scanning.
 * On scan, calls GET /api/nutrition/foods/barcode/{code} and displays nutritional info.
 *
 * Validates: Requirement 5.3
 */

import React, { useCallback, useRef, useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, ActivityIndicator, ScrollView, Platform } from 'react-native';
import { useAccessibility } from '../accessibility';

const API_BASE_URL = 'https://api.spartangoldengym.com';

interface NutritionalInfo {
  name: string;
  barcode: string;
  caloriesPer100g: number;
  proteinPer100g: number;
  carbsPer100g: number;
  fatPer100g: number;
  micronutrients?: Record<string, number>;
}

type ScanStatus = 'scanning' | 'loading' | 'result' | 'not_found' | 'error';

async function fetchFoodByBarcode(code: string): Promise<NutritionalInfo | null> {
  try {
    const response = await fetch(`${API_BASE_URL}/api/nutrition/foods/barcode/${code}`, {
      headers: { 'Content-Type': 'application/json' },
    });
    if (response.status === 404) return null;
    if (!response.ok) throw new Error('API error');
    return await response.json();
  } catch {
    throw new Error('Error de conexión');
  }
}

export const BarcodeScannerScreen: React.FC = () => {
  const [status, setStatus] = useState<ScanStatus>('scanning');
  const [food, setFood] = useState<NutritionalInfo | null>(null);
  const [errorMsg, setErrorMsg] = useState('');
  const processingRef = useRef(false);
  const { announceForAccessibility } = useAccessibility();
  const minTarget = Platform.OS === 'ios' ? 44 : 48;

  const handleBarcodeScanned = useCallback(async (codes: Array<{ value?: string }>) => {
    if (processingRef.current || !codes.length) return;
    const barcode = codes[0].value;
    if (!barcode) return;

    processingRef.current = true;
    setStatus('loading');

    try {
      const result = await fetchFoodByBarcode(barcode);
      if (result) {
        setFood(result);
        setStatus('result');
        announceForAccessibility(`Producto encontrado: ${result.name}`);
      } else {
        setStatus('not_found');
        announceForAccessibility('Producto no encontrado');
      }
    } catch {
      setErrorMsg('No se pudo obtener la información nutricional');
      setStatus('error');
      announceForAccessibility('Error al obtener información nutricional');
    }

    processingRef.current = false;
  }, []);

  const handleReset = useCallback(() => {
    setStatus('scanning');
    setFood(null);
    setErrorMsg('');
    processingRef.current = false;
  }, []);

  return (
    <View style={styles.container}>
      {status === 'scanning' && (
        <View style={styles.cameraPlaceholder}>
          <Text style={styles.scanText} allowFontScaling maxFontSizeMultiplier={2.0}>Escanea el código de barras del alimento</Text>
          <View style={styles.scanFrame} />
        </View>
      )}

      {status === 'loading' && (
        <View style={styles.centered} accessibilityLiveRegion="polite">
          <ActivityIndicator size="large" color="#1976D2" />
          <Text style={styles.loadingText} allowFontScaling maxFontSizeMultiplier={2.0}>Buscando producto...</Text>
        </View>
      )}

      {status === 'result' && food && (
        <ScrollView style={styles.resultContainer} contentContainerStyle={styles.resultContent}>
          <Text style={styles.foodName} allowFontScaling maxFontSizeMultiplier={2.0} accessibilityRole="header">{food.name}</Text>
          <Text style={styles.barcodeLabel} allowFontScaling maxFontSizeMultiplier={2.0}>Código: {food.barcode}</Text>

          <Text style={styles.sectionTitle} allowFontScaling maxFontSizeMultiplier={2.0}>Por 100g</Text>

          <View style={styles.macroRow}>
            <MacroCard label="Calorías" value={`${food.caloriesPer100g}`} unit="kcal" color="#FF9800" />
            <MacroCard label="Proteínas" value={`${food.proteinPer100g}`} unit="g" color="#F44336" />
          </View>
          <View style={styles.macroRow}>
            <MacroCard label="Carbohidratos" value={`${food.carbsPer100g}`} unit="g" color="#4CAF50" />
            <MacroCard label="Grasas" value={`${food.fatPer100g}`} unit="g" color="#2196F3" />
          </View>

          <TouchableOpacity style={[styles.scanAgainBtn, { minHeight: minTarget }]} onPress={handleReset} accessibilityRole="button" accessibilityLabel="Escanear otro producto" accessibilityHint="Vuelve al modo de escaneo de código de barras">
            <Text style={styles.scanAgainText} allowFontScaling maxFontSizeMultiplier={1.5}>Escanear otro producto</Text>
          </TouchableOpacity>
        </ScrollView>
      )}

      {status === 'not_found' && (
        <View style={styles.centered} accessibilityLiveRegion="assertive" accessibilityRole="alert">
          <Text style={styles.notFoundIcon}>🔍</Text>
          <Text style={styles.notFoundText} allowFontScaling maxFontSizeMultiplier={2.0}>Producto no encontrado</Text>
          <TouchableOpacity style={[styles.scanAgainBtn, { minHeight: minTarget }]} onPress={handleReset} accessibilityRole="button" accessibilityLabel="Escanear otro producto">
            <Text style={styles.scanAgainText} allowFontScaling maxFontSizeMultiplier={1.5}>Escanear otro</Text>
          </TouchableOpacity>
        </View>
      )}

      {status === 'error' && (
        <View style={styles.centered} accessibilityLiveRegion="assertive" accessibilityRole="alert">
          <Text style={styles.errorText} allowFontScaling maxFontSizeMultiplier={2.0}>{errorMsg}</Text>
          <TouchableOpacity style={[styles.scanAgainBtn, { minHeight: minTarget }]} onPress={handleReset} accessibilityRole="button" accessibilityLabel="Intentar de nuevo" accessibilityHint="Vuelve al modo de escaneo para reintentar">
            <Text style={styles.scanAgainText} allowFontScaling maxFontSizeMultiplier={1.5}>Intentar de nuevo</Text>
          </TouchableOpacity>
        </View>
      )}
    </View>
  );
};

const MacroCard: React.FC<{ label: string; value: string; unit: string; color: string }> = ({ label, value, unit, color }) => (
  <View style={[styles.macroCard, { borderLeftColor: color }]} accessibilityRole="summary" accessibilityLabel={`${label}: ${value} ${unit}`}>
    <Text style={styles.macroLabel} allowFontScaling maxFontSizeMultiplier={2.0}>{label}</Text>
    <Text style={styles.macroValue} allowFontScaling maxFontSizeMultiplier={2.0}>{value}</Text>
    <Text style={styles.macroUnit} allowFontScaling maxFontSizeMultiplier={2.0}>{unit}</Text>
  </View>
);

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#f5f5f5' },
  cameraPlaceholder: { flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: '#000' },
  scanText: { color: '#fff', fontSize: 16, marginBottom: 24 },
  scanFrame: { width: 280, height: 120, borderWidth: 2, borderColor: '#fff', borderRadius: 8 },
  centered: { flex: 1, justifyContent: 'center', alignItems: 'center', padding: 24 },
  loadingText: { marginTop: 12, fontSize: 16, color: '#616161' },
  resultContainer: { flex: 1 },
  resultContent: { padding: 16 },
  foodName: { fontSize: 22, fontWeight: 'bold', color: '#212121', marginBottom: 4 },
  barcodeLabel: { fontSize: 14, color: '#9E9E9E', marginBottom: 16 },
  sectionTitle: { fontSize: 16, fontWeight: '600', color: '#424242', marginBottom: 12 },
  macroRow: { flexDirection: 'row', gap: 8, marginBottom: 8 },
  macroCard: { flex: 1, backgroundColor: '#fff', padding: 12, borderRadius: 8, borderLeftWidth: 4 },
  macroLabel: { fontSize: 12, color: '#757575', marginBottom: 4 },
  macroValue: { fontSize: 20, fontWeight: 'bold', color: '#212121' },
  macroUnit: { fontSize: 12, color: '#9E9E9E' },
  notFoundIcon: { fontSize: 48, marginBottom: 12 },
  notFoundText: { fontSize: 18, color: '#616161', marginBottom: 24 },
  errorText: { fontSize: 16, color: '#D32F2F', marginBottom: 24, textAlign: 'center' },
  scanAgainBtn: { backgroundColor: '#1976D2', paddingHorizontal: 24, paddingVertical: 12, borderRadius: 8, marginTop: 16, alignSelf: 'center' },
  scanAgainText: { color: '#fff', fontSize: 16, fontWeight: '600' },
});
