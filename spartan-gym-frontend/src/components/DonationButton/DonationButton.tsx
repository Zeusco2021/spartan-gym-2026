import { useState } from 'react';
import {
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Box,
  Typography,
  CircularProgress,
  Alert,
  ToggleButton,
  ToggleButtonGroup,
} from '@mui/material';
import FavoriteIcon from '@mui/icons-material/Favorite';
import { useTranslation } from 'react-i18next';
import { useLocale } from '@/hooks/useLocale';
import { useDonateMutation } from '@/api/paymentsApi';

const SUGGESTED_AMOUNTS = [5, 10, 25, 50];
const CURRENCY = 'USD';

interface DonationButtonProps {
  creatorId: string;
  creatorName: string;
}

export default function DonationButton({ creatorId, creatorName }: DonationButtonProps) {
  const { t } = useTranslation('payments');
  const { formatCurrency } = useLocale();
  const [donate, { isLoading }] = useDonateMutation();

  const [open, setOpen] = useState(false);
  const [amount, setAmount] = useState<number | null>(null);
  const [customAmount, setCustomAmount] = useState('');
  const [message, setMessage] = useState('');
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const effectiveAmount = amount ?? (customAmount ? parseFloat(customAmount) : 0);
  const isValid = effectiveAmount > 0 && isFinite(effectiveAmount);

  const handleOpen = () => {
    setOpen(true);
    setSuccess(false);
    setError(null);
    setAmount(null);
    setCustomAmount('');
    setMessage('');
  };

  const handleDonate = async () => {
    if (!isValid) return;
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

  return (
    <>
      <Button
        variant="contained"
        color="secondary"
        startIcon={<FavoriteIcon />}
        onClick={handleOpen}
        aria-label={`${t('donate')} ${creatorName}`}
      >
        {t('donate')}
      </Button>

      <Dialog open={open} onClose={() => setOpen(false)} maxWidth="xs" fullWidth>
        <DialogTitle>{t('donateToCreator', { name: creatorName })}</DialogTitle>
        <DialogContent>
          {success ? (
            <Alert severity="success" sx={{ mt: 1 }}>{t('donationSuccess')}</Alert>
          ) : (
            <Box sx={{ mt: 1 }}>
              <Typography variant="body2" gutterBottom>
                {t('selectAmount')}
              </Typography>
              <ToggleButtonGroup
                value={amount}
                exclusive
                onChange={(_, val) => {
                  setAmount(val);
                  if (val !== null) setCustomAmount('');
                }}
                aria-label={t('suggestedAmounts')}
                sx={{ mb: 2, flexWrap: 'wrap' }}
              >
                {SUGGESTED_AMOUNTS.map((a) => (
                  <ToggleButton key={a} value={a} aria-label={formatCurrency(a, CURRENCY)}>
                    {formatCurrency(a, CURRENCY)}
                  </ToggleButton>
                ))}
              </ToggleButtonGroup>

              <TextField
                type="number"
                label={t('customAmount')}
                value={customAmount}
                onChange={(e) => {
                  setCustomAmount(e.target.value);
                  setAmount(null);
                }}
                fullWidth
                size="small"
                slotProps={{ htmlInput: { min: 1, step: 0.01 } }}
                sx={{ mb: 2 }}
              />

              <TextField
                label={t('donationMessage')}
                value={message}
                onChange={(e) => setMessage(e.target.value)}
                multiline
                rows={2}
                fullWidth
                size="small"
              />

              {error && <Alert severity="error" sx={{ mt: 2 }}>{error}</Alert>}
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)} aria-label={t('cancel')}>
            {success ? t('close') : t('cancel')}
          </Button>
          {!success && (
            <Button
              variant="contained"
              onClick={handleDonate}
              disabled={!isValid || isLoading}
              aria-label={t('confirmDonation')}
            >
              {isLoading ? <CircularProgress size={20} /> : t('confirmDonation')}
            </Button>
          )}
        </DialogActions>
      </Dialog>
    </>
  );
}
