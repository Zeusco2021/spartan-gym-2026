package com.spartangoldengym.nutricion.entity;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "nutrition_plans")
public class NutritionPlan {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 50)
    private String goal;

    @Column(name = "daily_calories")
    private Integer dailyCalories;

    @Column(name = "protein_grams")
    private Integer proteinGrams;

    @Column(name = "carbs_grams")
    private Integer carbsGrams;

    @Column(name = "fat_grams")
    private Integer fatGrams;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public NutritionPlan() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
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
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
