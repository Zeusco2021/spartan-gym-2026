import { useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  CircularProgress,
  Alert,
  Grid2 as Grid,
  Chip,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  MenuItem,
  TextField,
  List,
  ListItem,
  ListItemText,
} from '@mui/material';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import { useTranslation } from 'react-i18next';
import { useGetRecipesQuery } from '@/api/nutritionApi';

const GOALS = ['', 'weight_loss', 'muscle_gain', 'maintenance'] as const;

export default function RecipesPage() {
  const { t } = useTranslation('nutrition');
  const [goal, setGoal] = useState('');

  const { data, isLoading, isError } = useGetRecipesQuery(
    goal ? { goal } : {},
    { refetchOnMountOrArgChange: true },
  );

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        {t('recipesTitle')}
      </Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
        {t('recipesDescription')}
      </Typography>

      <TextField
        select
        label={t('goalFilter')}
        value={goal}
        onChange={(e) => setGoal(e.target.value)}
        sx={{ mb: 3, minWidth: 200 }}
        size="small"
      >
        {GOALS.map((g) => (
          <MenuItem key={g} value={g}>
            {g ? t(g) : t('allGoals')}
          </MenuItem>
        ))}
      </TextField>

      {isLoading && (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
          <CircularProgress aria-label={t('loading')} />
        </Box>
      )}

      {isError && <Alert severity="error">{t('error')}</Alert>}

      {data && data.content.length === 0 && (
        <Alert severity="info">{t('noRecipes')}</Alert>
      )}

      {data && data.content.length > 0 && (
        <Grid container spacing={3} aria-label={t('recipesTitle')}>
          {data.content.map((recipe) => (
            <Grid key={recipe.id} size={{ xs: 12, md: 6 }}>
              <Card aria-label={recipe.name}>
                <CardContent>
                  <Typography variant="h6">{recipe.name}</Typography>
                  <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                    {recipe.description}
                  </Typography>
                  <Box sx={{ display: 'flex', gap: 1, mb: 2, flexWrap: 'wrap' }}>
                    <Chip label={`${recipe.calories} ${t('kcal')}`} size="small" />
                    <Chip label={`P: ${recipe.proteinGrams}${t('grams')}`} size="small" color="success" />
                    <Chip label={`C: ${recipe.carbsGrams}${t('grams')}`} size="small" color="primary" />
                    <Chip label={`F: ${recipe.fatGrams}${t('grams')}`} size="small" color="warning" />
                  </Box>

                  <Accordion disableGutters>
                    <AccordionSummary
                      expandIcon={<ExpandMoreIcon />}
                      aria-label={`${t('ingredients')} - ${recipe.name}`}
                    >
                      <Typography variant="body2">{t('ingredients')}</Typography>
                    </AccordionSummary>
                    <AccordionDetails>
                      <List dense aria-label={`${t('ingredients')} ${recipe.name}`}>
                        {recipe.ingredients.map((ing, i) => (
                          <ListItem key={i}>
                            <ListItemText primary={ing} />
                          </ListItem>
                        ))}
                      </List>
                    </AccordionDetails>
                  </Accordion>

                  <Accordion disableGutters>
                    <AccordionSummary
                      expandIcon={<ExpandMoreIcon />}
                      aria-label={`${t('instructions')} - ${recipe.name}`}
                    >
                      <Typography variant="body2">{t('instructions')}</Typography>
                    </AccordionSummary>
                    <AccordionDetails>
                      <List dense aria-label={`${t('instructions')} ${recipe.name}`}>
                        {recipe.instructions.map((step, i) => (
                          <ListItem key={i}>
                            <ListItemText primary={`${i + 1}. ${step}`} />
                          </ListItem>
                        ))}
                      </List>
                    </AccordionDetails>
                  </Accordion>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}
    </Box>
  );
}
