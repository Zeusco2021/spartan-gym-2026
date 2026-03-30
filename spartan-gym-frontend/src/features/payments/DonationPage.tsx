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
  TextField,
} from '@mui/material';
import { useTranslation } from 'react-i18next';
import { useParams } from 'react-router-dom';
import { PayPalScriptProvider, PayPalButtons } from '@paypal/react-paypal-js';
import { useLocale } from '@/hooks/useLocale';
import { useDonateMutation } from '@/api/paymentsApi';

const SUGGESTED_AMOUNTS = [5, 10, 25, 50];
const CURRENCY = 'USD';

function DonationContent() {
  const { t } = useTranslation('payments');
  const { formatCurrency } = useLocale();
  const { creatorId } = useParams<{ creatorId: string }>();

  const [donate, { isLoading: isDonating }] = useDonateMutation();

  const [selectedAmount, setSelectedAmount] = useState<number | null>(null);
  const [customAmount, setCustomAmount] = useState('');
  const [message, setMessage] = useState('');
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const effectiveAmount = selectedAmount ?? (customAmount ? parseFloat(customAmount) : 0);
  const isValidAmount = effectiveAmount > 0 && isFinite(effectiveAmount);

  const handleSuggestedClick = (amount: number) => {
    setSelectedAmount(amount);
    setCustomAmount('');
    setError(null);
  };

  const handleCustomAmountChange = (value: string) => {
    setCustomAmount(value);
    setSelectedAmount(null);
    setError(null);
  };

  const handleApprove = async () => {
    if (!creatorId || !isValidAmount) return;
    setError(null);
    try {
      await donate({
        creatorId,
        amount: effectiveAmount,
        currency: CURRENCY,
        message: message || undefined,
      }).unwrap();
      setSuccess(true);
    } catch {
      setError(t('donationError'));
    }
  };

  if (success) {
    return (
      <Box sx={{ textAlign: 'center', py: 6 }}>
        <Typography variant="h4" gutterBottom>
          🎉 {t('donationSuccess')}
        </Typography>
        <Typography variant="body1" color="text.secondary">
          {t('thankYou')}
        </Typography>
      </Box>
    );
  }

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        {t('donationTitle')}
      </Typography>

      {isDonating && (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
          <CircularProgress />
          <Typography sx={{ ml: 2 }}>{t('processingDonation')}</Typography>
        </Box>
      )}

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {!isDonating && (
        <Grid container spacing={3}>
          {/* Suggested Amounts */}
          <Grid size={{ xs: 12 }}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  {t('suggestedAmounts')}
                </Typography>
                <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
                  {SUGGESTED_AMOUNTS.map((amount) => (
                    <Button
                      key={amount}
                      variant={selectedAmount === amount ? 'contained' : 'outlined'}
                      size="large"
                      onClick={() => handleSuggestedClick(amount)}
                      sx={{ minWidth: 100 }}
                    >
                      {formatCurrency(amount, CURRENCY)}
                    </Button>
                  ))}
                </Box>
              </CardContent>
            </Card>
          </Grid>

          {/* Custom Amount */}
          <Grid size={{ xs: 12, md: 6 }}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  {t('customAmount')}
                </Typography>
                <TextField
                  type="number"
                  label={t('enterAmount')}
                  value={customAmount}
                  onChange={(e) => handleCustomAmountChange(e.target.value)}
                  fullWidth
                  slotProps={{ htmlInput: { min: 1, step: 0.01 } }}
                />
              </CardContent>
            </Card>
          </Grid>

          {/* Message */}
          <Grid size={{ xs: 12, md: 6 }}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  {t('donationMessage')}
                </Typography>
                <TextField
                  label={t('donationMessagePlaceholder')}
                  value={message}
                  onChange={(e) => setMessage(e.target.value)}
                  multiline
                  rows={3}
                  fullWidth
                />
              </CardContent>
            </Card>
          </Grid>

          {/* PayPal Button */}
          <Grid size={{ xs: 12 }}>
            <Card>
              <CardContent>
                {!isValidAmount ? (
                  <Alert severity="info">{t('selectAmount')}</Alert>
                ) : (
                  <Box>
                    <Typography variant="body1" sx={{ mb: 2 }}>
                      {t('donate')}: {formatCurrency(effectiveAmount, CURRENCY)}
                    </Typography>
                    <PayPalButtons
                      style={{ layout: 'vertical' }}
                      disabled={isDonating}
                      createOrder={(_data, actions) =>
                        actions.order.create({
                          intent: 'CAPTURE',
                          purchase_units: [
                            {
                              amount: {
                                value: effectiveAmount.toFixed(2),
                                currency_code: CURRENCY,
                              },
                            },
                          ],
                        })
                      }
                      onApprove={async (_data, actions) => {
                        await actions.order?.capture();
                        await handleApprove();
                      }}
                      onError={() => setError(t('donationError'))}
                    />
                  </Box>
                )}
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      )}
    </Box>
  );
}

export default function DonationPage() {
  return (
    <PayPalScriptProvider
      options={{ clientId: import.meta.env.VITE_PAYPAL_CLIENT_ID ?? '' }}
    >
      <DonationContent />
    </PayPalScriptProvider>
  );
}
