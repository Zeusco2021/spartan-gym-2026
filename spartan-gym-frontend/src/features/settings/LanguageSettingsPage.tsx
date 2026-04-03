import {
  Box,
  Card,
  CardContent,
  Typography,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  RadioGroup,
  FormControlLabel,
  Radio,
  Grid2 as Grid,
} from '@mui/material';
import { useTranslation } from 'react-i18next';
import { useLocale } from '@/hooks/useLocale';
import { useAppDispatch, useAppSelector } from '@/app/hooks';
import { setMeasurementUnit } from '@/features/settings/uiSlice';

const LANGUAGES = [
  { code: 'en', label: 'English' },
  { code: 'es', label: 'Español' },
  { code: 'fr', label: 'Français' },
  { code: 'de', label: 'Deutsch' },
  { code: 'ja', label: '日本語' },
] as const;

export default function LanguageSettingsPage() {
  const { t } = useTranslation('settings');
  const {
    locale,
    changeLocale,
    formatDate,
    formatTime,
    formatCurrency,
    formatNumber,
    formatWeight,
    formatDistance,
  } = useLocale();
  const dispatch = useAppDispatch();
  const measurementUnit = useAppSelector((state) => state.ui.measurementUnit);

  const sampleDate = new Date().toISOString();

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        {t('language')}
      </Typography>

      <Grid container spacing={3}>
        {/* Language Selector */}
        <Grid size={{ xs: 12, md: 6 }}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                {t('selectLanguage')}
              </Typography>
              <FormControl fullWidth>
                <InputLabel id="language-select-label">{t('language')}</InputLabel>
                <Select
                  labelId="language-select-label"
                  value={locale}
                  label={t('language')}
                  onChange={(e) => changeLocale(e.target.value)}
                  aria-label={t('selectLanguage')}
                >
                  {LANGUAGES.map(({ code, label }) => (
                    <MenuItem key={code} value={code}>
                      {label}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </CardContent>
          </Card>
        </Grid>

        {/* Measurement Unit Selector */}
        <Grid size={{ xs: 12, md: 6 }}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                {t('measurementUnits')}
              </Typography>
              <FormControl>
                <RadioGroup
                  value={measurementUnit}
                  onChange={(e) =>
                    dispatch(setMeasurementUnit(e.target.value as 'metric' | 'imperial'))
                  }
                  aria-label={t('measurementUnits')}
                >
                  <FormControlLabel
                    value="metric"
                    control={<Radio aria-label={t('metric')} />}
                    label={t('metric')}
                  />
                  <FormControlLabel
                    value="imperial"
                    control={<Radio aria-label={t('imperial')} />}
                    label={t('imperial')}
                  />
                </RadioGroup>
              </FormControl>
            </CardContent>
          </Card>
        </Grid>

        {/* Format Preview */}
        <Grid size={{ xs: 12 }}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                {t('formatPreview')}
              </Typography>
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                <Box>
                  <Typography variant="body2" color="text.secondary">
                    {t('dateFormat')}
                  </Typography>
                  <Typography variant="body1">{formatDate(sampleDate)}</Typography>
                </Box>
                <Box>
                  <Typography variant="body2" color="text.secondary">
                    {t('timeFormat')}
                  </Typography>
                  <Typography variant="body1">{formatTime(sampleDate)}</Typography>
                </Box>
                <Box>
                  <Typography variant="body2" color="text.secondary">
                    {t('currencyFormat')}
                  </Typography>
                  <Typography variant="body1">{formatCurrency(99.99, 'USD')}</Typography>
                </Box>
                <Box>
                  <Typography variant="body2" color="text.secondary">
                    {t('numberFormat')}
                  </Typography>
                  <Typography variant="body1">{formatNumber(1234567.89)}</Typography>
                </Box>
                <Box>
                  <Typography variant="body2" color="text.secondary">
                    {t('weightFormat')}
                  </Typography>
                  <Typography variant="body1">{formatWeight(75)}</Typography>
                </Box>
                <Box>
                  <Typography variant="body2" color="text.secondary">
                    {t('distanceFormat')}
                  </Typography>
                  <Typography variant="body1">{formatDistance(10)}</Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
}
