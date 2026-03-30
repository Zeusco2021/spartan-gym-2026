import {
  Box,
  Typography,
  CircularProgress,
  Alert,
  Button,
  Card,
  CardContent,
  Chip,
  LinearProgress,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
} from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import { useTranslation } from 'react-i18next';
import { useNavigate, useParams } from 'react-router-dom';

import { useGetGymByIdQuery, useGetGymEquipmentQuery } from '@/api/gymsApi';
import { useGymOccupancy } from '@/hooks/useGymOccupancy';
import type { GymEquipment } from '@/types';

const DAY_KEYS = [
  'monday',
  'tuesday',
  'wednesday',
  'thursday',
  'friday',
  'saturday',
  'sunday',
] as const;

const STATUS_COLORS: Record<GymEquipment['status'], string> = {
  available: '#4caf50',
  maintenance: '#ff9800',
  out_of_order: '#f44336',
};

function getOccupancyColor(ratio: number): string {
  if (ratio < 0.5) return '#4caf50';
  if (ratio <= 0.8) return '#ff9800';
  return '#f44336';
}

export default function GymDetailPage() {
  const { t } = useTranslation('gym');
  const navigate = useNavigate();
  const { gymId } = useParams<{ gymId: string }>();

  const {
    data: gym,
    isLoading: gymLoading,
    isError: gymError,
  } = useGetGymByIdQuery(gymId!, { skip: !gymId });

  const {
    data: equipment,
    isLoading: equipmentLoading,
    isError: equipmentError,
  } = useGetGymEquipmentQuery(gymId!, { skip: !gymId });

  const wsOccupancy = useGymOccupancy(gymId ?? '');

  const isLoading = gymLoading || equipmentLoading;
  const isError = gymError || equipmentError;

  const maxCapacity = gym?.maxCapacity ?? 1;
  const rawOccupancy = wsOccupancy ?? gym?.currentOccupancy ?? 0;
  const occupancy = Math.max(0, Math.min(rawOccupancy, maxCapacity));
  const occupancyRatio = maxCapacity > 0 ? occupancy / maxCapacity : 0;
  const occupancyPercent = occupancyRatio * 100;
  const occupancyColor = getOccupancyColor(occupancyRatio);

  const statusLabel = (status: GymEquipment['status']): string => {
    if (status === 'out_of_order') return t('outOfOrder');
    return t(status);
  };

  return (
    <Box>
      <Button
        startIcon={<ArrowBackIcon />}
        onClick={() => navigate('/gym')}
        sx={{ mb: 2 }}
      >
        {t('backToMap')}
      </Button>

      {isLoading && (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
          <CircularProgress />
        </Box>
      )}

      {isError && <Alert severity="error">{t('loadError')}</Alert>}

      {gym && (
        <>
          <Typography variant="h4" gutterBottom>
            {gym.name}
          </Typography>
          <Typography variant="body1" color="text.secondary" gutterBottom>
            {gym.address}
          </Typography>

          {/* Occupancy indicator */}
          <Card sx={{ mb: 3 }}>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                {t('currentOccupancy')}
              </Typography>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                <Typography variant="body2">
                  {occupancy} / {maxCapacity}
                </Typography>
                <Typography variant="body2">
                  {Math.round(occupancyPercent)}%
                </Typography>
              </Box>
              <LinearProgress
                variant="determinate"
                value={occupancyPercent}
                aria-label={`${t('occupancy')}: ${occupancy}/${maxCapacity}`}
                sx={{
                  height: 12,
                  borderRadius: 6,
                  backgroundColor: '#e0e0e0',
                  '& .MuiLinearProgress-bar': {
                    backgroundColor: occupancyColor,
                    borderRadius: 6,
                  },
                }}
              />
            </CardContent>
          </Card>

          {/* Operating hours */}
          <Card sx={{ mb: 3 }}>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                {t('hours')}
              </Typography>
              <TableContainer>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell />
                      <TableCell>{t('open')}</TableCell>
                      <TableCell>{t('close')}</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {DAY_KEYS.map((day) => {
                      const hours = gym.operatingHours[day];
                      if (!hours) return null;
                      return (
                        <TableRow key={day}>
                          <TableCell>{t(`day_${day}`)}</TableCell>
                          <TableCell>{hours.open}</TableCell>
                          <TableCell>{hours.close}</TableCell>
                        </TableRow>
                      );
                    })}
                  </TableBody>
                </Table>
              </TableContainer>
            </CardContent>
          </Card>

          {/* Equipment */}
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                {t('equipment')}
              </Typography>
              {equipment && equipment.length > 0 ? (
                <TableContainer component={Paper} variant="outlined">
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>{t('equipment')}</TableCell>
                        <TableCell>{t('quantity')}</TableCell>
                        <TableCell align="right" />
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {equipment.map((item) => (
                        <TableRow key={item.id}>
                          <TableCell>
                            <Typography variant="body2">{item.name}</Typography>
                            <Typography variant="caption" color="text.secondary">
                              {item.category}
                            </Typography>
                          </TableCell>
                          <TableCell>{item.quantity}</TableCell>
                          <TableCell align="right">
                            <Chip
                              label={statusLabel(item.status)}
                              size="small"
                              sx={{
                                backgroundColor: STATUS_COLORS[item.status],
                                color: '#fff',
                                fontWeight: 600,
                              }}
                            />
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              ) : (
                !equipmentLoading && (
                  <Alert severity="info">{t('noEquipment')}</Alert>
                )
              )}
            </CardContent>
          </Card>
        </>
      )}
    </Box>
  );
}
