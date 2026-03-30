import { useEffect, useMemo, useState, useCallback } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  CircularProgress,
  Alert,
  Button,
  Chip,
} from '@mui/material';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import Map, { Marker, Popup } from 'react-map-gl';
import 'mapbox-gl/dist/mapbox-gl.css';

import { useGetNearbyGymsQuery } from '@/api/gymsApi';
import { useAppSelector } from '@/app/hooks';
import { socketService } from '@/websocket/socketService';
import type { Gym } from '@/types';

const MAPBOX_TOKEN = import.meta.env.VITE_MAPBOX_TOKEN as string;

const DEFAULT_CENTER = { latitude: 40.4168, longitude: -3.7038 }; // Madrid
const DEFAULT_ZOOM = 12;

type OccupancyLevel = 'low' | 'medium' | 'high';

function getOccupancyLevel(current: number, max: number): OccupancyLevel {
  if (max <= 0) return 'low';
  const ratio = current / max;
  if (ratio < 0.5) return 'low';
  if (ratio < 0.8) return 'medium';
  return 'high';
}

const OCCUPANCY_COLORS: Record<OccupancyLevel, string> = {
  low: '#4caf50',
  medium: '#ff9800',
  high: '#f44336',
};

export default function GymMapPage() {
  const { t } = useTranslation('gym');
  const navigate = useNavigate();

  const [userLocation, setUserLocation] = useState(DEFAULT_CENTER);
  const [selectedGym, setSelectedGym] = useState<Gym | null>(null);

  const occupancyUpdates = useAppSelector(
    (state) => state.websocket.occupancyUpdates,
  );

  // Get user geolocation on mount
  useEffect(() => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (pos) => {
          setUserLocation({
            latitude: pos.coords.latitude,
            longitude: pos.coords.longitude,
          });
        },
        () => {
          // Keep default location on error
        },
      );
    }
  }, []);

  const {
    data: gyms,
    isLoading,
    isError,
  } = useGetNearbyGymsQuery(
    { lat: userLocation.latitude, lng: userLocation.longitude, radius: 10 },
    { refetchOnMountOrArgChange: true },
  );

  // Subscribe to occupancy updates for all gyms on mount, unsubscribe on unmount
  useEffect(() => {
    if (!gyms || gyms.length === 0) return;

    const gymIds = gyms.map((g) => g.id);
    gymIds.forEach((id) => socketService.joinGymOccupancy(id));

    return () => {
      gymIds.forEach((id) => socketService.leaveGymOccupancy(id));
    };
  }, [gyms]);

  const getOccupancy = useCallback(
    (gym: Gym): number => {
      return occupancyUpdates[gym.id] ?? gym.currentOccupancy ?? 0;
    },
    [occupancyUpdates],
  );

  const viewState = useMemo(
    () => ({
      latitude: userLocation.latitude,
      longitude: userLocation.longitude,
      zoom: DEFAULT_ZOOM,
    }),
    [userLocation],
  );

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        {t('nearbyGyms')}
      </Typography>

      {isLoading && (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
          <CircularProgress />
        </Box>
      )}

      {isError && <Alert severity="error">{t('mapError')}</Alert>}

      {!isLoading && !isError && (
        <Box sx={{ height: 500, borderRadius: 2, overflow: 'hidden' }}>
          <Map
            initialViewState={viewState}
            style={{ width: '100%', height: '100%' }}
            mapStyle="mapbox://styles/mapbox/streets-v12"
            mapboxAccessToken={MAPBOX_TOKEN}
          >
            {gyms?.map((gym) => {
              const occ = getOccupancy(gym);
              const level = getOccupancyLevel(occ, gym.maxCapacity);
              const color = OCCUPANCY_COLORS[level];

              return (
                <Marker
                  key={gym.id}
                  latitude={gym.latitude}
                  longitude={gym.longitude}
                  anchor="bottom"
                  onClick={(e) => {
                    e.originalEvent.stopPropagation();
                    setSelectedGym(gym);
                  }}
                >
                  <Box
                    sx={{
                      width: 24,
                      height: 24,
                      borderRadius: '50%',
                      backgroundColor: color,
                      border: '3px solid #fff',
                      boxShadow: '0 2px 6px rgba(0,0,0,0.3)',
                      cursor: 'pointer',
                    }}
                    aria-label={`${gym.name} - ${t('occupancy')}: ${t(level)}`}
                  />
                </Marker>
              );
            })}

            {selectedGym && (
              <Popup
                latitude={selectedGym.latitude}
                longitude={selectedGym.longitude}
                anchor="bottom"
                onClose={() => setSelectedGym(null)}
                closeOnClick={false}
                offset={30}
              >
                <GymPopupContent
                  gym={selectedGym}
                  occupancy={getOccupancy(selectedGym)}
                  onViewDetail={() => navigate(`/gym/${selectedGym.id}`)}
                />
              </Popup>
            )}
          </Map>
        </Box>
      )}
    </Box>
  );
}

interface GymPopupContentProps {
  gym: Gym;
  occupancy: number;
  onViewDetail: () => void;
}

function GymPopupContent({ gym, occupancy, onViewDetail }: GymPopupContentProps) {
  const { t } = useTranslation('gym');
  const level = getOccupancyLevel(occupancy, gym.maxCapacity);

  return (
    <Card elevation={0} sx={{ minWidth: 200 }}>
      <CardContent sx={{ p: 1, '&:last-child': { pb: 1 } }}>
        <Typography variant="subtitle2" gutterBottom>
          {gym.name}
        </Typography>
        <Typography variant="caption" color="text.secondary" display="block">
          {gym.address}
        </Typography>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 1 }}>
          <Chip
            label={`${t('occupancy')}: ${occupancy}/${gym.maxCapacity}`}
            size="small"
            sx={{
              backgroundColor: OCCUPANCY_COLORS[level],
              color: '#fff',
              fontWeight: 600,
            }}
          />
        </Box>
        <Button
          size="small"
          variant="outlined"
          onClick={onViewDetail}
          sx={{ mt: 1, width: '100%' }}
        >
          {t('viewDetail')}
        </Button>
      </CardContent>
    </Card>
  );
}
