import {
  Box,
  Card,
  CardContent,
  Typography,
  CircularProgress,
  Alert,
  Grid2 as Grid,
  Chip,
} from '@mui/material';
import { useTranslation } from 'react-i18next';
import { useGetSupplementsQuery } from '@/api/nutritionApi';

export default function SupplementsPage() {
  const { t } = useTranslation('nutrition');

  const { data, isLoading, isError } = useGetSupplementsQuery(undefined, {
    refetchOnMountOrArgChange: true,
  });

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        {t('supplementsTitle')}
      </Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
        {t('supplementsDescription')}
      </Typography>

      {isLoading && (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
          <CircularProgress />
        </Box>
      )}

      {isError && <Alert severity="error">{t('error')}</Alert>}

      {data && data.length === 0 && (
        <Alert severity="info">{t('noSupplements')}</Alert>
      )}

      {data && data.length > 0 && (
        <Grid container spacing={3}>
          {data.map((supp) => (
            <Grid key={supp.id} size={{ xs: 12, md: 6 }}>
              <Card>
                <CardContent>

                  <Typography variant="h6">{supp.name}</Typography>
                  <Chip label={supp.category} size="small" sx={{ mb: 1 }} />
                  <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                    {supp.description}
                  </Typography>
                  <Typography variant="body2">
                    {t('dosage')}: {supp.dosage}
                  </Typography>
                  <Typography variant="body2" sx={{ mt: 1 }}>
                    {t('benefits')}:
                  </Typography>
                  <Box sx={{ display: 'flex', gap: 0.5, flexWrap: 'wrap', mt: 0.5 }}>
                    {supp.benefits.map((benefit, i) => (
                      <Chip key={i} label={benefit} size="small" variant="outlined" />
                    ))}
                  </Box>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}
    </Box>
  );
}
