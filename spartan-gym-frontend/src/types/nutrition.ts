export interface NutritionPlan {
  id: string;
  userId: string;
  goal: 'weight_loss' | 'muscle_gain' | 'maintenance';
  dailyCalories: number;
  proteinGrams: number;
  carbsGrams: number;
  fatGrams: number;
}

export interface Food {
  id: string;
  name: string;
  barcode?: string;
  caloriesPer100g: number;
  proteinPer100g: number;
  carbsPer100g: number;
  fatPer100g: number;
  micronutrients?: Record<string, number>;
  region?: string;
}

export interface MealLog {
  id: string;
  userId: string;
  food: Food;
  quantityGrams: number;
  mealType: 'breakfast' | 'lunch' | 'dinner' | 'snack';
  loggedAt: string;
}

export interface DailyBalance {
  date: string;
  totalCalories: number;
  totalProtein: number;
  totalCarbs: number;
  totalFat: number;
  targetCalories: number;
  targetProtein: number;
  targetCarbs: number;
  targetFat: number;
}

export interface Recipe {
  id: string;
  name: string;
  description: string;
  calories: number;
  proteinGrams: number;
  carbsGrams: number;
  fatGrams: number;
  ingredients: string[];
  instructions: string[];
  goal?: string;
}

export interface Supplement {
  id: string;
  name: string;
  description: string;
  dosage: string;
  benefits: string[];
  category: string;
}
