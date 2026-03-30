package com.spartangoldengym.nutricion.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.UUID;

public class CreateNutritionPlanRequest {

    @NotNull
    private UUID userId;

    @NotBlank
    private String goal;

    private Integer dailyCalories;
    private Integer proteinGrams;
    private Integer carbsGrams;
    private Integer fatGrams;

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }
    public Integer getDailyCalories() { return dailyCalories; }
    public void setDailyCalories(Integer dailyCalories) { this.dailyCalories = dailyCalories; }
    public Integer getProteinGrams() { return proteinGrams; }
    public void setProteinGrams(Integer proteinGrams) { this.proteinGrams = proteinGrams; }
    public Integer getCarbsGrams() { return carbsGrams; }
    public void setCarbsGrams(Integer carbsGrams) { this.carbsGrams = carbsGrams; }
    public Integer getFatGrams() { return fatGrams; }
    public void setFatGrams(Integer fatGrams) { this.fatGrams = fatGrams; }
}
