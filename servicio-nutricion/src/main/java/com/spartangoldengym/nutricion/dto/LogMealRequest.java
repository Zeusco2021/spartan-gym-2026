package com.spartangoldengym.nutricion.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

public class LogMealRequest {

    @NotNull
    private UUID userId;

    @NotNull
    private UUID foodId;

    @NotNull
    @Positive
    private BigDecimal quantityGrams;

    @NotBlank
    private String mealType;

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public UUID getFoodId() { return foodId; }
    public void setFoodId(UUID foodId) { this.foodId = foodId; }
    public BigDecimal getQuantityGrams() { return quantityGrams; }
    public void setQuantityGrams(BigDecimal quantityGrams) { this.quantityGrams = quantityGrams; }
    public String getMealType() { return mealType; }
    public void setMealType(String mealType) { this.mealType = mealType; }
}
