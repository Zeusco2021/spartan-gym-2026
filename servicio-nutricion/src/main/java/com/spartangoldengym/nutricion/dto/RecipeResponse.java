package com.spartangoldengym.nutricion.dto;

import java.util.List;

public class RecipeResponse {

    private String id;
    private String name;
    private String description;
    private int calories;
    private int proteinGrams;
    private int carbsGrams;
    private int fatGrams;
    private List<String> ingredients;
    private String instructions;
    private String goal;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getCalories() { return calories; }
    public void setCalories(int calories) { this.calories = calories; }
    public int getProteinGrams() { return proteinGrams; }
    public void setProteinGrams(int proteinGrams) { this.proteinGrams = proteinGrams; }
    public int getCarbsGrams() { return carbsGrams; }
    public void setCarbsGrams(int carbsGrams) { this.carbsGrams = carbsGrams; }
    public int getFatGrams() { return fatGrams; }
    public void setFatGrams(int fatGrams) { this.fatGrams = fatGrams; }
    public List<String> getIngredients() { return ingredients; }
    public void setIngredients(List<String> ingredients) { this.ingredients = ingredients; }
    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }
}
