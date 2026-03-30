package com.spartangoldengym.nutricion.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class MealLogResponse {

    private UUID id;
    private UUID userId;
    private UUID foodId;
    private String foodName;
    private BigDecimal quantityGrams;
    private String mealType;
    private BigDecimal calories;
    private BigDecimal protein;
    private BigDecimal carbs;
    private BigDecimal fat;
    private Instant loggedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public UUID getFoodId() { return foodId; }
    public void setFoodId(UUID foodId) { this.foodId = foodId; }
    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }
    public BigDecimal getQuantityGrams() { return quantityGrams; }
    public void setQuantityGrams(BigDecimal quantityGrams) { this.quantityGrams = quantityGrams; }
    public String getMealType() { return mealType; }
    public void setMealType(String mealType) { this.mealType = mealType; }
    public BigDecimal getCalories() { return calories; }
    public void setCalories(BigDecimal calories) { this.calories = calories; }
    public BigDecimal getProtein() { return protein; }
    public void setProtein(BigDecimal protein) { this.protein = protein; }
    public BigDecimal getCarbs() { return carbs; }
    public void setCarbs(BigDecimal carbs) { this.carbs = carbs; }
    public BigDecimal getFat() { return fat; }
    public void setFat(BigDecimal fat) { this.fat = fat; }
    public Instant getLoggedAt() { return loggedAt; }
    public void setLoggedAt(Instant loggedAt) { this.loggedAt = loggedAt; }
}
