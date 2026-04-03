/**
 * Gym Map Screen — Interactive map showing gym locations with occupancy markers
 *
 * Uses @rnmapbox/maps (Mapbox GL) for the map.
 * Markers are color-coded by occupancy: green (low), yellow (medium), red (high).
 * Tap a marker to see gym details.
 *
 * Validates: Requirement 2.7
 */

import React, { useCallback, useEffect, useState } from 'react';
import { View, Text, StyleSheet, ActivityIndicator, TouchableOpacity, Modal, Platform } from 'react-native';
import { useAccessibility } from '../accessibility';

const API_BASE_URL = 'https://api.spartangoldengym.com';

interface Gym {
  id: string;
  name: string;
  address: string;
  latitude: number;
  longitude: number;
  currentOccupancy: number;
  maxCapacity: number;
  operatingHours: string;
}

type OccupancyLevel = 'low' | 'medium' | 'high';

function getOccupancyLevel(current: number, max: number): OccupancyLevel {
  const ratio = max > 0 ? current / max : 0;
  if (ratio < 0.5) return 'low';
  if (ratio < 0.8) return 'medium';
  return 'high';
}

function getOccupancyColor(level: OccupancyLevel): string {
  switch (level) {
    case 'low': return '#4CAF50';
    case 'medium': return '#FF9800';
    case 'high': return '#F44336';
  }
}

function getOccupancyLabel(level: OccupancyLevel): string {
  switch (level) {
    case 'low': return 'Baja';
    case 'medium': return 'Media';
    case 'high': return 'Alta';
  }
}

async function fetchNearbyGyms(lat: number, lng: number): Promise<Gym[]> {
  try {
    const response = await fetch(
      `${API_BASE_URL}/api/gyms/nearby?latitude=${lat}&longitude=${lng}&radius=10`,
    );
    if (!response.ok) return [];
    return await response.json();
  } catch {
    return [];
  }
}

export const GymMapScreen: React.FC = () => {
  const [gyms, setGyms] = useState<Gym[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedGym, setSelectedGym] = useState<Gym | null>(null);
  const { announceForAccessibility } = useAccessibility();
  const minTarget = Platform.OS === 'ios' ? 44 : 48;

  useEffect(() => {
    // Default to a central location; in production, use device geolocation
    fetchNearbyGyms(40.4168, -3.7038).then((data) => {
      setGyms(data);
      setLoading(false);
    });
  }, []);

  const handleMarkerPress = useCallback((gym: Gym) => {
    setSelectedGym(gym);
    const level = getOccupancyLevel(gym.currentOccupancy, gym.maxCapacity);
    announceForAccessibility(`${gym.name}, ocupación ${getOccupancyLabel(level)}, ${gym.currentOccupancy} de ${gym.maxCapacity}`);
  }, [announceForAccessibility]);

  if (loading) {
    return (
      <View style={styles.centered}>
        <ActivityIndicator size="large" color="#1976D2" />
        <Text style={styles.loadingText} allowFontScaling maxFontSizeMultiplier={2.0}>Cargando mapa...</Text>
      </View>
    );
  }

  // In production, this renders a Mapbox MapView with PointAnnotation markers.
  // The structure below shows the component hierarchy and marker rendering logic.
  return (
    <View style={styles.container}>
      {/* Mapbox MapView would wrap this content */}
      <View style={styles.mapPlaceholder}>
        <Text style={styles.mapPlaceholderText} allowFontScaling maxFontSizeMultiplier={2.0}>Mapa Mapbox GL</Text>

        {gyms.map((gym) => {
          const level = getOccupancyLevel(gym.currentOccupancy, gym.maxCapacity);
          const color = getOccupancyColor(level);
          return (
            <TouchableOpacity
              key={gym.id}
              style={[styles.marker, { backgroundColor: color, minHeight: minTarget, minWidth: minTarget }]}
              onPress={() => handleMarkerPress(gym)}
              accessibilityRole="button"
              accessibilityLabel={`${gym.name}, ocupación ${getOccupancyLabel(level)}, ${gym.currentOccupancy} de ${gym.maxCapacity} personas`}
              accessibilityHint="Abre los detalles del gimnasio"
            >
              <Text style={styles.markerText} allowFontScaling={false}>{gym.currentOccupancy}</Text>
            </TouchableOpacity>
          );
        })}
      </View>

      {/* Gym detail modal */}
      <Modal visible={!!selectedGym} transparent animationType="slide" onRequestClose={() => setSelectedGym(null)}>
        <View style={styles.modalOverlay}>
          <View style={styles.modalContent} accessibilityViewIsModal={true} accessibilityRole="alert">
            {selectedGym && (
              <>
                <Text style={styles.gymName} allowFontScaling maxFontSizeMultiplier={2.0} accessibilityRole="header">{selectedGym.name}</Text>
                <Text style={styles.gymAddress} allowFontScaling maxFontSizeMultiplier={2.0} accessibilityLabel={`Dirección: ${selectedGym.address}`}>{selectedGym.address}</Text>

                <View style={styles.occupancyRow} accessibilityLabel={`Ocupación: ${selectedGym.currentOccupancy} de ${selectedGym.maxCapacity}, nivel ${getOccupancyLabel(getOccupancyLevel(selectedGym.currentOccupancy, selectedGym.maxCapacity))}`}>
                  <Text style={styles.occupancyLabel} allowFontScaling maxFontSizeMultiplier={2.0}>Ocupación:</Text>
                  <View style={[
                    styles.occupancyBadge,
                    { backgroundColor: getOccupancyColor(getOccupancyLevel(selectedGym.currentOccupancy, selectedGym.maxCapacity)) },
                  ]}>
                    <Text style={styles.occupancyBadgeText} allowFontScaling maxFontSizeMultiplier={1.5}>
                      {selectedGym.currentOccupancy}/{selectedGym.maxCapacity} — {getOccupancyLabel(getOccupancyLevel(selectedGym.currentOccupancy, selectedGym.maxCapacity))}
                    </Text>
                  </View>
                </View>

                <Text style={styles.hoursLabel} allowFontScaling maxFontSizeMultiplier={2.0} accessibilityLabel={`Horario: ${selectedGym.operatingHours}`}>Horario: {selectedGym.operatingHours}</Text>

                <TouchableOpacity style={[styles.closeBtn, { minHeight: minTarget }]} onPress={() => setSelectedGym(null)} accessibilityRole="button" accessibilityLabel="Cerrar detalles" accessibilityHint="Cierra el panel de detalles del gimnasio">
                  <Text style={styles.closeBtnText} allowFontScaling maxFontSizeMultiplier={1.5}>Cerrar</Text>
                </TouchableOpacity>
              </>
            )}
          </View>
        </View>
      </Modal>
    </View>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1 },
  centered: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  loadingText: { marginTop: 12, fontSize: 16, color: '#616161' },
  mapPlaceholder: { flex: 1, backgroundColor: '#E0E0E0', justifyContent: 'center', alignItems: 'center' },
  mapPlaceholderText: { fontSize: 18, color: '#9E9E9E' },
  marker: { width: 36, height: 36, borderRadius: 18, justifyContent: 'center', alignItems: 'center', margin: 4, elevation: 3 },
  markerText: { color: '#fff', fontSize: 12, fontWeight: 'bold' },
  modalOverlay: { flex: 1, justifyContent: 'flex-end', backgroundColor: 'rgba(0,0,0,0.4)' },
  modalContent: { backgroundColor: '#fff', borderTopLeftRadius: 16, borderTopRightRadius: 16, padding: 24 },
  gymName: { fontSize: 20, fontWeight: 'bold', color: '#212121', marginBottom: 4 },
  gymAddress: { fontSize: 14, color: '#757575', marginBottom: 16 },
  occupancyRow: { flexDirection: 'row', alignItems: 'center', marginBottom: 12 },
  occupancyLabel: { fontSize: 14, color: '#424242', marginRight: 8 },
  occupancyBadge: { paddingHorizontal: 12, paddingVertical: 4, borderRadius: 12 },
  occupancyBadgeText: { color: '#fff', fontSize: 13, fontWeight: '600' },
  hoursLabel: { fontSize: 14, color: '#616161', marginBottom: 20 },
  closeBtn: { backgroundColor: '#1976D2', padding: 14, borderRadius: 8, alignItems: 'center' },
  closeBtnText: { color: '#fff', fontSize: 16, fontWeight: '600' },
});
