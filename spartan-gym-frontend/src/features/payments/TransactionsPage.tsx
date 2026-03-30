import { useState, useCallback } from 'react';
import {
  Box,
  Typography,
  CircularProgress,
  Alert,
  Chip,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Pagination,
  Paper,
} from '@mui/material';
import { FixedSizeList, type ListChildComponentProps } from 'react-window';
import { useTranslation } from 'react-i18next';
import { useLocale } from '@/hooks/useLocale';
import {
  useGetTransactionsQuery,
  useRequestRefundMutation,
} from '@/api/paymentsApi';
import type { Transaction } from '@/types/payments';

const ROW_HEIGHT = 72;
const LIST_HEIGHT = 504; // 7 visible rows

function getStatusColor(status: Transaction['status']) {
  switch (status) {
    case 'completed': return 'success';
    case 'pending': return 'warning';
    case 'failed': return 'error';
    case 'refunded': return 'info';
    default: return 'default';
  }
}

function getTypeColor(type: Transaction['type']) {
  switch (type) {
    case 'subscription': return 'primary';
    case 'donation': return 'secondary';
    case 'refund': return 'info';
    default: return 'default';
  }
}

export default function TransactionsPage() {
  const { t } = useTranslation('payments');
  const { formatCurrency, formatDate } = useLocale();

  const [page, setPage] = useState(1);
  const [typeFilter, setTypeFilter] = useState<string>('all');
  const [statusFilter, setStatusFilter] = useState<string>('all');

  const { data, isLoading, isError } = useGetTransactionsQuery({ page: page - 1 });

  const [requestRefund, { isLoading: refunding }] = useRequestRefundMutation();
  const [refundDialogOpen, setRefundDialogOpen] = useState(false);
  const [refundTarget, setRefundTarget] = useState<Transaction | null>(null);
  const [refundReason, setRefundReason] = useState('');
  const [refundError, setRefundError] = useState<string | null>(null);

  const transactions = data?.content ?? [];

  const filtered = transactions.filter((tx) => {
    if (typeFilter !== 'all' && tx.type !== typeFilter) return false;
    if (statusFilter !== 'all' && tx.status !== statusFilter) return false;
    return true;
  });

  const handleOpenRefund = useCallback((tx: Transaction) => {
    setRefundTarget(tx);
    setRefundReason('');
    setRefundError(null);
    setRefundDialogOpen(true);
  }, []);

  const handleConfirmRefund = async () => {
    if (!refundTarget || !refundReason.trim()) return;
    setRefundError(null);
    try {
      await requestRefund({
        transactionId: refundTarget.id,
        reason: refundReason.trim(),
      }).unwrap();
      setRefundDialogOpen(false);
      setRefundTarget(null);
    } catch {
      setRefundError(t('error'));
    }
  };

  const Row = useCallback(
    ({ index, style }: ListChildComponentProps) => {
      const tx = filtered[index];
      if (!tx) return null;
      const canRefund = tx.status === 'completed' && tx.type !== 'refund';

      return (
        <Box
          style={style}
          sx={{
            display: 'flex',
            alignItems: 'center',
            gap: 2,
            px: 2,
            borderBottom: 1,
            borderColor: 'divider',
          }}
        >
          <Box sx={{ flex: 1, minWidth: 0 }}>
            <Typography variant="body2" noWrap>
              {tx.id}
            </Typography>
            <Typography variant="caption" color="text.secondary">
              {formatDate(tx.createdAt)}
            </Typography>
          </Box>
          <Chip
            label={t(`type_${tx.type}`)}
            color={getTypeColor(tx.type)}
            size="small"
            variant="outlined"
          />
          <Chip
            label={t(`status_${tx.status}`)}
            color={getStatusColor(tx.status)}
            size="small"
          />
          <Typography variant="body2" sx={{ minWidth: 90, textAlign: 'right' }}>
            {formatCurrency(tx.amount, tx.currency)}
          </Typography>
          {canRefund && (
            <Button size="small" onClick={() => handleOpenRefund(tx)}>
              {t('refund')}
            </Button>
          )}
        </Box>
      );
    },
    [filtered, formatCurrency, formatDate, handleOpenRefund, t],
  );

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        {t('transactions')}
      </Typography>

      {/* Filters */}
      <Box sx={{ display: 'flex', gap: 2, mb: 3, flexWrap: 'wrap' }}>
        <FormControl size="small" sx={{ minWidth: 160 }}>
          <InputLabel>{t('filterType')}</InputLabel>
          <Select
            value={typeFilter}
            label={t('filterType')}
            onChange={(e) => setTypeFilter(e.target.value)}
          >
            <MenuItem value="all">{t('all')}</MenuItem>
            <MenuItem value="subscription">{t('type_subscription')}</MenuItem>
            <MenuItem value="donation">{t('type_donation')}</MenuItem>
            <MenuItem value="refund">{t('type_refund')}</MenuItem>
          </Select>
        </FormControl>
        <FormControl size="small" sx={{ minWidth: 160 }}>
          <InputLabel>{t('filterStatus')}</InputLabel>
          <Select
            value={statusFilter}
            label={t('filterStatus')}
            onChange={(e) => setStatusFilter(e.target.value)}
          >
            <MenuItem value="all">{t('all')}</MenuItem>
            <MenuItem value="completed">{t('status_completed')}</MenuItem>
            <MenuItem value="pending">{t('status_pending')}</MenuItem>
            <MenuItem value="failed">{t('status_failed')}</MenuItem>
            <MenuItem value="refunded">{t('status_refunded')}</MenuItem>
          </Select>
        </FormControl>
      </Box>

      {isLoading && (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
          <CircularProgress />
        </Box>
      )}

      {isError && <Alert severity="error">{t('error')}</Alert>}

      {!isLoading && !isError && filtered.length === 0 && (
        <Alert severity="info">{t('noTransactions')}</Alert>
      )}

      {!isLoading && !isError && filtered.length > 0 && (
        <Paper variant="outlined">
          <FixedSizeList
            height={Math.min(LIST_HEIGHT, filtered.length * ROW_HEIGHT)}
            width="100%"
            itemCount={filtered.length}
            itemSize={ROW_HEIGHT}
          >
            {Row}
          </FixedSizeList>
        </Paper>
      )}

      {data && data.totalPages > 1 && (
        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 3 }}>
          <Pagination
            count={data.totalPages}
            page={page}
            onChange={(_, value) => setPage(value)}
            color="primary"
          />
        </Box>
      )}

      {/* Refund Dialog */}
      <Dialog
        open={refundDialogOpen}
        onClose={() => setRefundDialogOpen(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>{t('refund')}</DialogTitle>
        <DialogContent>
          {refundTarget && (
            <Box sx={{ mb: 2 }}>
              <Typography variant="body2" color="text.secondary">
                {t('refundAmount')}: {formatCurrency(refundTarget.amount, refundTarget.currency)}
              </Typography>
            </Box>
          )}
          <TextField
            label={t('refundReason')}
            value={refundReason}
            onChange={(e) => setRefundReason(e.target.value)}
            multiline
            rows={3}
            fullWidth
            sx={{ mt: 1 }}
          />
          {refundError && <Alert severity="error" sx={{ mt: 2 }}>{refundError}</Alert>}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setRefundDialogOpen(false)}>{t('cancel')}</Button>
          <Button
            variant="contained"
            onClick={handleConfirmRefund}
            disabled={!refundReason.trim() || refunding}
          >
            {refunding ? <CircularProgress size={20} /> : t('confirmRefund')}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
