import { useState } from 'react';
import {
  Box,
  Typography,
  CircularProgress,
  Alert,
  Card,
  CardContent,
  Button,
  Chip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  MenuItem,
  LinearProgress,
} from '@mui/material';
import { useTranslation } from 'react-i18next';
import {
  useGetClassesQuery,
  useReserveClassMutation,
  useCancelReservationMutation,
} from '@/api/bookingsApi';
import type { GroupClass } from '@/types/bookings';

const DIFFICULTY_OPTIONS = ['beginner', 'intermediate', 'advanced'] as const;

export default function ClassesListPage() {
  const { t } = useTranslation('bookings');

  const [typeFilter, setTypeFilter] = useState('');
  const [difficultyFilter, setDifficultyFilter] = useState('');

  const { data, isLoading, isError } = useGetClassesQuery({
    type: typeFilter || undefined,
    difficulty: difficultyFilter || undefined,
  });

  const [reserveClass, { isLoading: reserving }] = useReserveClassMutation();
  const [cancelReservation, { isLoading: cancelling }] = useCancelReservationMutation();

  const [confirmDialog, setConfirmDialog] = useState<{
    open: boolean;
    action: 'reserve' | 'cancel';
    classItem: GroupClass | null;
  }>({ open: false, action: 'reserve', classItem: null });

  const classes = data?.content ?? [];

  const handleReserveClick = (classItem: GroupClass) => {
    setConfirmDialog({ open: true, action: 'reserve', classItem });
  };

  const handleCancelClick = (classItem: GroupClass) => {
    setConfirmDialog({ open: true, action: 'cancel', classItem });
  };

  const handleConfirm = async () => {
    if (!confirmDialog.classItem) return;
    const id = confirmDialog.classItem.id;
    if (confirmDialog.action === 'reserve') {
      await reserveClass(id);
    } else {
      await cancelReservation(id);
    }
    setConfirmDialog({ open: false, action: 'reserve', classItem: null });
  };

  const handleCloseDialog = () => {
    setConfirmDialog({ open: false, action: 'reserve', classItem: null });
  };

  const isFull = (c: GroupClass) => c.currentCapacity >= c.maxCapacity;
  const occupancyPercent = (c: GroupClass) =>
    c.maxCapacity > 0 ? Math.round((c.currentCapacity / c.maxCapacity) * 100) : 0;

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        {t('classesTitle')}
      </Typography>

      {/* Filters */}
      <Box sx={{ display: 'flex', gap: 2, mb: 3, flexWrap: 'wrap' }}>
        <TextField
          select
          label={t('filterType')}
          value={typeFilter}
          onChange={(e) => setTypeFilter(e.target.value)}
          size="small"
          sx={{ minWidth: 160 }}
          aria-label={t('filterType')}
        >
          <MenuItem value="">{t('all')}</MenuItem>
          <MenuItem value="yoga">Yoga</MenuItem>
          <MenuItem value="spinning">Spinning</MenuItem>
          <MenuItem value="crossfit">CrossFit</MenuItem>
          <MenuItem value="pilates">Pilates</MenuItem>
        </TextField>

        <TextField
          select
          label={t('filterDifficulty')}
          value={difficultyFilter}
          onChange={(e) => setDifficultyFilter(e.target.value)}
          size="small"
          sx={{ minWidth: 160 }}
          aria-label={t('filterDifficulty')}
        >
          <MenuItem value="">{t('all')}</MenuItem>
          {DIFFICULTY_OPTIONS.map((d) => (
            <MenuItem key={d} value={d}>
              {t(d)}
            </MenuItem>
          ))}
        </TextField>
      </Box>

      {isLoading && (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
          <CircularProgress aria-label={t('loading')} />
        </Box>
      )}

      {isError && <Alert severity="error">{t('loadError')}</Alert>}

      {!isLoading && !isError && classes.length === 0 && (
        <Alert severity="info">{t('noClasses')}</Alert>
      )}

      {!isLoading && !isError && classes.length > 0 && (
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          {classes.map((classItem) => (
            <Card key={classItem.id} data-testid={`class-card-${classItem.id}`}>
              <CardContent>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: 1 }}>
                  <Box>
                    <Typography variant="h6">{classItem.name}</Typography>
                    <Typography variant="body2" color="text.secondary">
                      {classItem.instructorName}
                      {classItem.room ? ` — ${classItem.room}` : ''}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      {new Date(classItem.scheduledAt).toLocaleString()} · {classItem.durationMinutes} min
                    </Typography>
                  </Box>
                  <Box sx={{ display: 'flex', gap: 1, alignItems: 'center' }}>
                    <Chip
                      label={t(classItem.difficultyLevel)}
                      size="small"
                      color={
                        classItem.difficultyLevel === 'advanced'
                          ? 'error'
                          : classItem.difficultyLevel === 'intermediate'
                            ? 'warning'
                            : 'success'
                      }
                      variant="outlined"
                    />
                    {isFull(classItem) && (
                      <Chip label={t('waitlist')} size="small" color="warning" />
                    )}
                  </Box>
                </Box>

                {/* Capacity bar */}
                <Box sx={{ mt: 2 }}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5 }}>
                    <Typography variant="caption">
                      {t('capacity')}: {classItem.currentCapacity}/{classItem.maxCapacity}
                    </Typography>
                    <Typography variant="caption">{occupancyPercent(classItem)}%</Typography>
                  </Box>
                  <LinearProgress
                    variant="determinate"
                    value={occupancyPercent(classItem)}
                    color={isFull(classItem) ? 'error' : 'primary'}
                    aria-label={`${t('capacity')}: ${occupancyPercent(classItem)}%`}
                  />
                </Box>

                {/* Actions */}
                <Box sx={{ mt: 2, display: 'flex', gap: 1 }}>
                  <Button
                    variant="contained"
                    size="small"
                    onClick={() => handleReserveClick(classItem)}
                    disabled={reserving}
                    aria-label={`${isFull(classItem) ? t('joinWaitlist') : t('reserve')}: ${classItem.name}`}
                  >
                    {isFull(classItem) ? t('joinWaitlist') : t('reserve')}
                  </Button>
                  <Button
                    variant="outlined"
                    size="small"
                    color="error"
                    onClick={() => handleCancelClick(classItem)}
                    disabled={cancelling}
                    aria-label={`${t('cancelReservation')}: ${classItem.name}`}
                  >
                    {t('cancelReservation')}
                  </Button>
                </Box>
              </CardContent>
            </Card>
          ))}
        </Box>
      )}

      {/* Confirmation dialog */}
      <Dialog open={confirmDialog.open} onClose={handleCloseDialog}>
        <DialogTitle>
          {confirmDialog.action === 'reserve' ? t('confirmReserve') : t('confirmCancel')}
        </DialogTitle>
        <DialogContent>
          <Typography>
            {confirmDialog.action === 'reserve'
              ? t('confirmReserveMsg', { name: confirmDialog.classItem?.name ?? '' })
              : t('confirmCancelMsg', { name: confirmDialog.classItem?.name ?? '' })}
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog} aria-label={t('dismiss')}>
            {t('dismiss')}
          </Button>
          <Button
            variant="contained"
            color={confirmDialog.action === 'reserve' ? 'primary' : 'error'}
            onClick={handleConfirm}
            aria-label={t('confirm')}
          >
            {t('confirm')}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
