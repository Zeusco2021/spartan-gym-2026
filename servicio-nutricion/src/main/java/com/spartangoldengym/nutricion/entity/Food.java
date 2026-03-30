package com.spartangoldengym.nutricion.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "foods")
public class Food {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 50)
    private String barcode;

    @Column(name = "calories_per_100g")
    private BigDecimal caloriesPer100g;

    @Column(name = "protein_per_100g")
    private BigDecimal proteinPer100g;

    @Column(name = "carbs_per_100g")
    private BigDecimal carbsPer100g;

    @Column(name = "fat_per_100g")
    private BigDecimal fatPer100g;

    @Column(columnDefinition = "jsonb")
    private String micronutrients;

    @Column(length = 50)
    private String region;

    public Food() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public BigDecimal getCaloriesPer100g() { return caloriesPer100g; }
    public void setCaloriesPer100g(BigDecimal caloriesPer100g) { this.caloriesPer100g = caloriesPer100g; }
    public BigDecimal getProteinPer100g() { return proteinPer100g; }
    public void setProteinPer100g(BigDecimal proteinPer100g) { this.proteinPer100g = proteinPer100g; }
    public BigDecimal getCarbsPer100g() { return carbsPer100g; }
    public void setCarbsPer100g(BigDecimal carbsPer100g) { this.carbsPer100g = carbsPer100g; }
    public BigDecimal getFatPer100g() { return fatPer100g; }
    public void setFatPer100g(BigDecimal fatPer100g) { this.fatPer100g = fatPer100g; }
    public String getMicronutrients() { return micronutrients; }
    public void setMicronutrients(String micronutrients) { this.micronutrients = micronutrients; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
}
