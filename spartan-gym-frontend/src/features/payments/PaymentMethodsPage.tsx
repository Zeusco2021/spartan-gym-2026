import { useState } from 'react';
import {
  Box,
  Typography,
  CircularProgress,
  Alert,
  Card,
  CardContent,
  Button,
  IconButton,
  Chip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  List,
  ListItem,
  ListItemText,
  ListItemSecondaryAction,
} from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import CreditCardIcon from '@mui/icons-material/CreditCard';
import { useTranslation } from 'react-i18next';
import {
  useGetPaymentMethodsQuery,
  useAddPaymentMethodMutation,
  useRemovePaymentMethodMutation,
} from '@/api/paymentsApi';
import type { PaymentMethod } from '@/types/payments';

export default function PaymentMethodsPage() {
  const { t } = useTranslation('payments');

  const { data: methods = [], isLoading, isError } = useGetPaymentMethodsQuery();
  const [addPaymentMethod, { isLoading: adding }] = useAddPaymentMethodMutation();
  const [removePaymentMethod] = useRemovePaymentMethodMutation();

  const [deleteTarget, setDeleteTarget] = useState<PaymentMethod | null>(null);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleAddMock = async () => {
    setError(null);
    try {
      await addPaymentMethod({ token: 'tok_mock', provider: 'stripe' }).unwrap();
    } catch {
      setError(t('error'));
    }
  };

  const handleDeleteClick = (pm: PaymentMethod) => {
    setDeleteTarget(pm);
    setDeleteDialogOpen(true);
  };

  const handleConfirmDelete = async () => {
    if (!deleteTarget) return;
    setError(null);
    try {
      await removePaymentMethod(deleteTarget.id).unwrap();
      setDeleteDialogOpen(false);
      setDeleteTarget(null);
    } catch {
      setError(t('error'));
    }
  };

  const providerLabel = (pm: PaymentMethod) => {
    if (pm.type === 'card') return `•••• ${pm.last4 ?? '****'}`;
    if (pm.type === 'paypal') return 'PayPal';
    return pm.type;
  };

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        {t('paymentMethods')}
      </Typography>

      {isLoading && (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
          <CircularProgress aria-label={t('loading')} />
        </Box>
      )}

      {isError && <Alert severity="error">{t('error')}</Alert>}
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      {!isLoading && !isError && (
        <Card>
          <CardContent>
            {methods.length === 0 ? (
              <Alert severity="info">{t('noPaymentMethods')}</Alert>
            ) : (
              <List aria-label={t('paymentMethodsList')}>
                {methods.map((pm) => (
                  <ListItem key={pm.id} divider>
                    <CreditCardIcon sx={{ mr: 2, color: 'text.secondary' }} />
                    <ListItemText
                      primary={providerLabel(pm)}
                      secondary={
                        pm.expiryMonth && pm.expiryYear
                          ? `${String(pm.expiryMonth).padStart(2, '0')}/${pm.expiryYear}`
                          : pm.provider
                      }
                    />
                    <ListItemSecondaryAction>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        {pm.isDefault && (
                          <Chip label={t('default')} size="small" color="primary" variant="outlined" />
                        )}
                        <IconButton
                          edge="end"
                          onClick={() => handleDeleteClick(pm)}
                          aria-label={`${t('removeMethod')}: ${providerLabel(pm)}`}
                          disabled={pm.isDefault}
                        >
                          <DeleteIcon />
                        </IconButton>
                      </Box>
                    </ListItemSecondaryAction>
                  </ListItem>
                ))}
              </List>
            )}

            <Button
              variant="contained"
              onClick={handleAddMock}
              disabled={adding}
              sx={{ mt: 2 }}
              aria-label={t('addPaymentMethod')}
            >
              {adding ? <CircularProgress size={20} /> : t('addPaymentMethod')}
            </Button>
          </CardContent>
        </Card>
      )}

      {/* Delete confirmation dialog */}
      <Dialog open={deleteDialogOpen} onClose={() => setDeleteDialogOpen(false)}>
        <DialogTitle>{t('removeMethodTitle')}</DialogTitle>
        <DialogContent>
          <Typography>
            {t('removeMethodConfirm', { method: deleteTarget ? providerLabel(deleteTarget) : '' })}
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialogOpen(false)} aria-label={t('cancel')}>
            {t('cancel')}
          </Button>
          <Button
            variant="contained"
            color="error"
            onClick={handleConfirmDelete}
            aria-label={t('confirmRemove')}
          >
            {t('confirmRemove')}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
