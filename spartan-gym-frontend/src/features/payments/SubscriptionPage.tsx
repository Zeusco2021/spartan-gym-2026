import { useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  CircularProgress,
  Alert,
  Grid2 as Grid,
  Button,
  Chip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Radio,
  RadioGroup,
  FormControlLabel,
} from '@mui/material';
import { useTranslation } from 'react-i18next';
import { loadStripe } from '@stripe/stripe-js';
import { Elements, CardElement, useStripe, useElements } from '@stripe/react-stripe-js';
import { useLocale } from '@/hooks/useLocale';
import {
  useGetSubscriptionQuery,
  useSubscribeMutation,
  useGetPaymentMethodsQuery,
  useAddPaymentMethodMutation,
} from '@/api/paymentsApi';
import type { PaymentMethod } from '@/types/payments';

const stripePromise = loadStripe(import.meta.env.VITE_STRIPE_PUBLISHABLE_KEY ?? '');

interface PlanOption {
  id: string;
  price: number;
  currency: string;
}

const PLANS: PlanOption[] = [
  { id: 'basic', price: 9.99, currency: 'USD' },
  { id: 'premium', price: 19.99, currency: 'USD' },
  { id: 'pro', price: 29.99, currency: 'USD' },
];

function getStatusColor(status: string) {
  switch (status) {
    case 'active': return 'success';
    case 'suspended': return 'warning';
    case 'cancelled': return 'error';
    case 'expired': return 'default';
    default: return 'default';
  }
}

function PaymentForm({
  onPaymentMethodCreated,
  isProcessing,
}: {
  onPaymentMethodCreated: (paymentMethodId: string) => void;
  isProcessing: boolean;
}) {
  const { t } = useTranslation('payments');
  const stripe = useStripe();
  const elements = useElements();
  const [addPaymentMethod] = useAddPaymentMethodMutation();
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async () => {
    if (!stripe || !elements) return;
    setError(null);

    const cardElement = elements.getElement(CardElement);
    if (!cardElement) return;

    const { error: stripeError, paymentMethod } = await stripe.createPaymentMethod({
      type: 'card',
      card: cardElement,
    });

    if (stripeError) {
      setError(stripeError.message ?? t('error'));
      return;
    }

    if (paymentMethod) {
      await addPaymentMethod({ token: paymentMethod.id, provider: 'stripe' });
      onPaymentMethodCreated(paymentMethod.id);
    }
  };

  return (
    <Box sx={{ mt: 2 }}>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
        {t('paymentSecure')}
      </Typography>
      <Box sx={{ border: 1, borderColor: 'divider', borderRadius: 1, p: 2, mb: 2 }}>
        <CardElement options={{ hidePostalCode: true }} />
      </Box>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      <Typography variant="caption" color="text.secondary">
        {t('managedByStripe')}
      </Typography>
      <Box sx={{ mt: 2 }}>
        <Button
          variant="contained"
          onClick={handleSubmit}
          disabled={!stripe || isProcessing}
          fullWidth
        >
          {isProcessing ? <CircularProgress size={20} /> : t('addCard')}
        </Button>
      </Box>
    </Box>
  );
}

function SubscriptionContent() {
  const { t } = useTranslation('payments');
  const { formatCurrency, formatDate } = useLocale();

  const { data: subscription, isLoading: subLoading, isError: subError } =
    useGetSubscriptionQuery();
  const { data: paymentMethods = [], isLoading: pmLoading } =
    useGetPaymentMethodsQuery();
  const [subscribe, { isLoading: subscribing }] = useSubscribeMutation();

  const [selectedPlan, setSelectedPlan] = useState<string | null>(null);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [selectedPaymentMethod, setSelectedPaymentMethod] = useState<string | null>(null);
  const [subscribeError, setSubscribeError] = useState<string | null>(null);

  const isLoading = subLoading || pmLoading;

  const handlePlanSelect = (planId: string) => {
    setSelectedPlan(planId);
    setDialogOpen(true);
    setSubscribeError(null);
    const defaultMethod = paymentMethods.find((pm: PaymentMethod) => pm.isDefault);
    setSelectedPaymentMethod(defaultMethod?.id ?? null);
  };

  const handleConfirmChange = async () => {
    if (!selectedPlan || !selectedPaymentMethod) return;
    setSubscribeError(null);
    try {
      await subscribe({ planType: selectedPlan, paymentMethodId: selectedPaymentMethod }).unwrap();
      setDialogOpen(false);
      setSelectedPlan(null);
    } catch {
      setSubscribeError(t('error'));
    }
  };

  const handlePaymentMethodCreated = (paymentMethodId: string) => {
    setSelectedPaymentMethod(paymentMethodId);
  };

  const getPlanAction = (planId: string): string | null => {
    if (!subscription) return t('selectPlan');
    const currentIndex = PLANS.findIndex((p) => p.id === subscription.planType);
    const targetIndex = PLANS.findIndex((p) => p.id === planId);
    if (currentIndex === targetIndex) return null;
    return targetIndex > currentIndex ? t('upgrade') : t('downgrade');
  };

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        {t('subscription')}
      </Typography>

      {isLoading && (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
          <CircularProgress />
        </Box>
      )}

      {subError && <Alert severity="error" sx={{ mb: 2 }}>{t('error')}</Alert>}

      {!isLoading && !subError && (
        <Grid container spacing={3}>
          {/* Current Plan */}
          <Grid size={{ xs: 12 }}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  {t('currentPlan')}
                </Typography>
                {subscription ? (
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                    <Typography variant="h5">
                      {t(subscription.planType)}
                    </Typography>
                    <Chip
                      label={t(subscription.status)}
                      color={getStatusColor(subscription.status) as 'success' | 'warning' | 'error' | 'default'}
                      size="small"
                    />
                    {subscription.expiresAt && (
                      <Typography variant="body2" color="text.secondary">
                        {formatDate(subscription.expiresAt)}
                      </Typography>
                    )}
                  </Box>
                ) : (
                  <Alert severity="info">{t('noSubscription')}</Alert>
                )}
              </CardContent>
            </Card>
          </Grid>

          {/* Plan Options */}
          <Grid size={{ xs: 12 }}>
            <Typography variant="h6" gutterBottom>
              {t('planOptions')}
            </Typography>
            <Grid container spacing={2}>
              {PLANS.map((plan) => {
                const action = getPlanAction(plan.id);
                const isCurrent = subscription?.planType === plan.id;
                return (
                  <Grid size={{ xs: 12, sm: 4 }} key={plan.id}>
                    <Card
                      variant={isCurrent ? 'elevation' : 'outlined'}
                      sx={{
                        border: isCurrent ? 2 : 1,
                        borderColor: isCurrent ? 'primary.main' : 'divider',
                      }}
                    >
                      <CardContent sx={{ textAlign: 'center' }}>
                        <Typography variant="h6">{t(plan.id)}</Typography>
                        <Typography variant="h4" sx={{ my: 2 }}>
                          {formatCurrency(plan.price, plan.currency)}
                        </Typography>
                        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                          / {t('month')}
                        </Typography>
                        {isCurrent ? (
                          <Chip label={t('currentPlan')} color="primary" />
                        ) : (
                          <Button
                            variant="contained"
                            color={action === t('upgrade') ? 'primary' : 'secondary'}
                            onClick={() => handlePlanSelect(plan.id)}
                            fullWidth
                          >
                            {action}
                          </Button>
                        )}
                      </CardContent>
                    </Card>
                  </Grid>
                );
              })}
            </Grid>
          </Grid>

          {/* Payment Methods */}
          <Grid size={{ xs: 12 }}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  {t('paymentMethods')}
                </Typography>
                {paymentMethods.length === 0 ? (
                  <Typography variant="body2" color="text.secondary">
                    {t('noPaymentMethods')}
                  </Typography>
                ) : (
                  paymentMethods.map((pm: PaymentMethod) => (
                    <Box
                      key={pm.id}
                      sx={{
                        display: 'flex',
                        alignItems: 'center',
                        gap: 2,
                        py: 1,
                        borderBottom: 1,
                        borderColor: 'divider',
                      }}
                    >
                      <Typography variant="body1">
                        •••• {pm.last4}
                      </Typography>
                      {pm.expiryMonth && pm.expiryYear && (
                        <Typography variant="body2" color="text.secondary">
                          {String(pm.expiryMonth).padStart(2, '0')}/{pm.expiryYear}
                        </Typography>
                      )}
                      {pm.isDefault && (
                        <Chip label="Default" size="small" color="primary" variant="outlined" />
                      )}
                    </Box>
                  ))
                )}
                <Box sx={{ mt: 2 }}>
                  <Typography variant="subtitle2" gutterBottom>
                    {t('addCard')}
                  </Typography>
                  <PaymentForm
                    onPaymentMethodCreated={handlePaymentMethodCreated}
                    isProcessing={subscribing}
                  />
                </Box>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      )}

      {/* Confirm Plan Change Dialog */}
      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{t('confirmChange')}</DialogTitle>
        <DialogContent>
          {selectedPlan && (
            <Box sx={{ mb: 2 }}>
              <Typography variant="body1">
                {t('changePlan')}: <strong>{t(selectedPlan)}</strong>
              </Typography>
              <Typography variant="body2" color="text.secondary">
                {formatCurrency(
                  PLANS.find((p) => p.id === selectedPlan)?.price ?? 0,
                  'USD',
                )} / {t('month')}
              </Typography>
            </Box>
          )}

          {paymentMethods.length > 0 && (
            <Box sx={{ mt: 2 }}>
              <Typography variant="subtitle2" gutterBottom>
                {t('paymentMethods')}
              </Typography>
              <RadioGroup
                value={selectedPaymentMethod ?? ''}
                onChange={(e) => setSelectedPaymentMethod(e.target.value)}
              >
                {paymentMethods.map((pm: PaymentMethod) => (
                  <FormControlLabel
                    key={pm.id}
                    value={pm.id}
                    control={<Radio />}
                    label={`•••• ${pm.last4 ?? ''} ${pm.isDefault ? '(Default)' : ''}`}
                  />
                ))}
              </RadioGroup>
            </Box>
          )}

          {subscribeError && <Alert severity="error" sx={{ mt: 2 }}>{subscribeError}</Alert>}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogOpen(false)}>{t('cancel')}</Button>
          <Button
            variant="contained"
            onClick={handleConfirmChange}
            disabled={!selectedPaymentMethod || subscribing}
          >
            {subscribing ? <CircularProgress size={20} /> : t('confirmChange')}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}

export default function SubscriptionPage() {
  return (
    <Elements stripe={stripePromise}>
      <SubscriptionContent />
    </Elements>
  );
}
