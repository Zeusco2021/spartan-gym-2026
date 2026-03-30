import { useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  CircularProgress,
  Alert,
  TextField,
  Button,
  MenuItem,
  List,
  ListItem,
  ListItemButton,
  ListItemText,
} from '@mui/material';
import { useTranslation } from 'react-i18next';
import { useSearchFoodsQuery, useLogMealMutation } from '@/api/nutritionApi';
import type { Food } from '@/types';

const MEAL_TYPES = ['breakfast', 'lunch', 'dinner', 'snack'] as const;

export default function MealLogPage() {
  const { t } = useTranslation('nutrition');

  const [searchQuery, setSearchQuery] = useState('');
  const [selectedFood, setSelectedFood] = useState<Food | null>(null);
  const [quantity, setQuantity] = useState('100');
  const [mealType, setMealType] = useState<string>('lunch');
  const [successMsg, setSuccessMsg] = useState('');

  const { data: foods, isLoading: searchLoading } = useSearchFoodsQuery(
    { query: searchQuery },
    { skip: searchQuery.length < 2 },
  );

  const [logMeal, { isLoading: logging, isError: logError }] = useLogMealMutation();

  const handleSelectFood = (food: Food) => {
    setSelectedFood(food);
    setSearchQuery(food.name);
  };

  const handleSubmit = async () => {
    if (!selectedFood) return;
    try {
      await logMeal({
        foodId: selectedFood.id,
        quantityGrams: Number(quantity),
        mealType,
      }).unwrap();
      setSuccessMsg(t('mealLogged'));
      setSelectedFood(null);
      setSearchQuery('');
      setQuantity('100');
    } catch {
      // error handled by logError state
    }
  };

  const qtyNum = Number(quantity);
  const multiplier = qtyNum / 100;

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        {t('logMeal')}
      </Typography>

      {successMsg && (
        <Alert severity="success" sx={{ mb: 2 }} onClose={() => setSuccessMsg('')}>
          {successMsg}
        </Alert>
      )}
      {logError && <Alert severity="error" sx={{ mb: 2 }}>{t('error')}</Alert>}

      <Card sx={{ mb: 3 }}>
        <CardContent>
          {/* Food Search */}
          <TextField
            fullWidth
            label={t('searchFoods')}
            value={searchQuery}
            onChange={(e) => {
              setSearchQuery(e.target.value);
              if (selectedFood && e.target.value !== selectedFood.name) {
                setSelectedFood(null);
              }
            }}
            sx={{ mb: 2 }}
          />

          {searchLoading && <CircularProgress size={24} />}

          {!selectedFood && foods && foods.content.length > 0 && searchQuery.length >= 2 && (
            <List sx={{ maxHeight: 200, overflow: 'auto', mb: 2 }}>
              {foods.content.map((food) => (
                <ListItem key={food.id} disablePadding>
                  <ListItemButton onClick={() => handleSelectFood(food)}>
                    <ListItemText
                      primary={food.name}
                      secondary={`${food.caloriesPer100g} ${t('kcal')} | P: ${food.proteinPer100g}${t('grams')} | C: ${food.carbsPer100g}${t('grams')} | F: ${food.fatPer100g}${t('grams')} (${t('foodInfo')})`}
                    />
                  </ListItemButton>
                </ListItem>
              ))}
            </List>
          )}

          {!selectedFood && foods && foods.content.length === 0 && searchQuery.length >= 2 && (
            <Alert severity="info" sx={{ mb: 2 }}>{t('noFoodsFound')}</Alert>
          )}

          {/* Selected food nutritional info */}
          {selectedFood && (
            <Alert severity="info" sx={{ mb: 2 }}>
              {selectedFood.name} — {Math.round(selectedFood.caloriesPer100g * multiplier)} {t('kcal')} |{' '}
              {t('protein')}: {Math.round(selectedFood.proteinPer100g * multiplier)}{t('grams')} |{' '}
              {t('carbs')}: {Math.round(selectedFood.carbsPer100g * multiplier)}{t('grams')} |{' '}
              {t('fat')}: {Math.round(selectedFood.fatPer100g * multiplier)}{t('grams')}
            </Alert>
          )}

          {/* Quantity & Meal Type */}
          <Box sx={{ display: 'flex', gap: 2, mb: 2, flexWrap: 'wrap' }}>
            <TextField
              label={t('quantity')}
              type="number"
              value={quantity}
              onChange={(e) => setQuantity(e.target.value)}
              slotProps={{ htmlInput: { min: 1 } }}
              sx={{ minWidth: 150 }}
            />
            <TextField
              select
              label={t('mealType')}
              value={mealType}
              onChange={(e) => setMealType(e.target.value)}
              sx={{ minWidth: 150 }}
            >
              {MEAL_TYPES.map((type) => (
                <MenuItem key={type} value={type}>
                  {t(type)}
                </MenuItem>
              ))}
            </TextField>
          </Box>

          <Button
            variant="contained"
            onClick={handleSubmit}
            disabled={!selectedFood || logging || qtyNum <= 0}
          >
            {logging ? <CircularProgress size={20} /> : t('addMeal')}
          </Button>
        </CardContent>
      </Card>
    </Box>
  );
}
