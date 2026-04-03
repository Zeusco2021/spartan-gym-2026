/**
 * QR Scanner Screen — Scan QR codes for gym check-in
 *
 * Uses react-native-vision-camera for scanning.
 * On successful scan, calls POST /api/gyms/{id}/checkin.
 * Must process scan and verify membership within 3 seconds (Req 9.6).
 *
 * Validates: Requirements 9.6, 2.4
 */

import React, { useCallback, useEffect, useRef, useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, ActivityIndicator, Alert, AccessibilityInfo } from 'react-native';
import { useAccessibility } from '../accessibility';

type ScanStatus = 'scanning' | 'processing' | 'success' | 'error';

const API_BASE_URL = 'https://api.spartangoldengym.com';

interface CheckinResponse {
  success: boolean;
  gymName?: string;
  message?: string;
}

async function performCheckin(gymId: string): Promise<CheckinResponse> {
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), 3000); // 3s max (Req 9.6)

  try {
    const response = await fetch(`${API_BASE_URL}/api/gyms/${gymId}/checkin`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      signal: controller.signal,
    });
    clearTimeout(timeout);

    if (!response.ok) {
      const body = await response.json().catch(() => ({}));
      return { success: false, message: body.message ?? 'Membresía no válida' };
    }
    return await response.json();
  } catch (err) {
    clearTimeout(timeout);
    const message = err instanceof Error && err.name === 'AbortError'
      ? 'Tiempo de espera agotado'
      : 'Error de conexión';
    return { success: false, message };
  }
}

export const QRScannerScreen: React.FC = () => {
  const [status, setStatus] = useState<ScanStatus>('scanning');
  const [resultMessage, setResultMessage] = useState('');
  const processingRef = useRef(false);
  const { announceForAccessibility } = useAccessibility();

  const handleCodeScanned = useCallback(async (codes: Array<{ value?: string }>) => {
    if (processingRef.current || !codes.length) return;
    const qrValue = codes[0].value;
    if (!qrValue) return;

    processingRef.current = true;
    setStatus('processing');
    announceForAccessibility('Verificando membresía');

    // Expected QR format: "spartan-gym:{gymId}"
    const match = qrValue.match(/^spartan-gym:(.+)$/);
    if (!match) {
      setStatus('error');
      setResultMessage('Código QR no válido');
      announceForAccessibility('Error: Código QR no válido');
      processingRef.current = false;
      return;
    }

    const gymId = match[1];
    const result = await performCheckin(gymId);

    if (result.success) {
      const msg = result.gymName ? `Check-in en ${result.gymName}` : 'Check-in exitoso';
      setStatus('success');
      setResultMessage(msg);
      announceForAccessibility(msg);
    } else {
      const msg = result.message ?? 'Error al hacer check-in';
      setStatus('error');
      setResultMessage(msg);
      announceForAccessibility(`Error: ${msg}`);
    }
  }, [announceForAccessibility]);

  const handleReset = useCallback(() => {
    setStatus('scanning');
    setResultMessage('');
    processingRef.current = false;
  }, []);

  // Camera component would be rendered here in production using react-native-vision-camera
  // For now, we render the UI structure with a placeholder for the camera view
  return (
    <View style={styles.container}>
      <View style={styles.cameraPlaceholder}>
        {status === 'scanning' && (
          <>
            <Text style={styles.scanText} allowFontScaling maxFontSizeMultiplier={2.0}>Apunta al código QR del gimnasio</Text>
            <View style={styles.scanFrame} />
          </>
        )}

        {status === 'processing' && (
          <View style={styles.overlay} accessibilityLiveRegion="assertive" accessibilityRole="alert">
            <ActivityIndicator size="large" color="#fff" />
            <Text style={styles.overlayText} allowFontScaling maxFontSizeMultiplier={2.0}>Verificando membresía...</Text>
          </View>
        )}

        {status === 'success' && (
          <View style={[styles.overlay, styles.successOverlay]} accessibilityLiveRegion="assertive" accessibilityRole="alert">
            <Text style={styles.resultIcon}>✅</Text>
            <Text style={styles.resultText} allowFontScaling maxFontSizeMultiplier={2.0}>{resultMessage}</Text>
            <TouchableOpacity style={styles.resetBtn} onPress={handleReset} accessibilityRole="button" accessibilityLabel="Escanear otro código" accessibilityHint="Vuelve al modo de escaneo">
              <Text style={styles.resetBtnText} allowFontScaling maxFontSizeMultiplier={1.5}>Escanear otro</Text>
            </TouchableOpacity>
          </View>
        )}

        {status === 'error' && (
          <View style={[styles.overlay, styles.errorOverlay]} accessibilityLiveRegion="assertive" accessibilityRole="alert">
            <Text style={styles.resultIcon}>❌</Text>
            <Text style={styles.resultText} allowFontScaling maxFontSizeMultiplier={2.0}>{resultMessage}</Text>
            <TouchableOpacity style={styles.resetBtn} onPress={handleReset} accessibilityRole="button" accessibilityLabel="Intentar de nuevo" accessibilityHint="Vuelve al modo de escaneo para reintentar">
              <Text style={styles.resetBtnText} allowFontScaling maxFontSizeMultiplier={1.5}>Intentar de nuevo</Text>
            </TouchableOpacity>
          </View>
        )}
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#000' },
  cameraPlaceholder: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  scanText: { color: '#fff', fontSize: 16, marginBottom: 24 },
  scanFrame: { width: 250, height: 250, borderWidth: 2, borderColor: '#fff', borderRadius: 12 },
  overlay: { ...StyleSheet.absoluteFillObject, justifyContent: 'center', alignItems: 'center', backgroundColor: 'rgba(0,0,0,0.7)' },
  successOverlay: { backgroundColor: 'rgba(46,125,50,0.85)' },
  errorOverlay: { backgroundColor: 'rgba(198,40,40,0.85)' },
  overlayText: { color: '#fff', fontSize: 16, marginTop: 12 },
  resultIcon: { fontSize: 48, marginBottom: 12 },
  resultText: { color: '#fff', fontSize: 18, fontWeight: '600', textAlign: 'center', marginBottom: 24, paddingHorizontal: 32 },
  resetBtn: { backgroundColor: 'rgba(255,255,255,0.2)', paddingHorizontal: 24, paddingVertical: 12, borderRadius: 8 },
  resetBtnText: { color: '#fff', fontSize: 16 },
});
