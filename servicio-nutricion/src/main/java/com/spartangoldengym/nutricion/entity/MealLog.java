package com.spartangoldengym.nutricion.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "meal_logs")
public class MealLog {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "food_id")
    private UUID foodId;

    @Column(name = "quantity_grams", nullable = false)
    private BigDecimal quantityGrams;

    @Column(name = "meal_type", nullable = false, length = 20)
    private String mealType;

    @Column(name = "logged_at")
    private Instant loggedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id", insertable = false, updatable = false)
    private Food food;

    @PrePersist
    protected void onCreate() {
        if (loggedAt == null) {
            loggedAt = Instant.now();
        }
    }

    public MealLog() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public UUID getFoodId() { return foodId; }
    public void setFoodId(UUID foodId) { this.foodId = foodId; }
    public BigDecimal getQuantityGrams() { return quantityGrams; }
    public void setQuantityGrams(BigDecimal quantityGrams) { this.quantityGrams = quantityGrams; }
    public String getMealType() { return mealType; }
    public void setMealType(String mealType) { this.mealType = mealType; }
    public Instant getLoggedAt() { return loggedAt; }
    public void setLoggedAt(Instant loggedAt) { this.loggedAt = loggedAt; }
    public Food getFood() { return food; }
    public void setFood(Food food) { this.food = food; }
}
