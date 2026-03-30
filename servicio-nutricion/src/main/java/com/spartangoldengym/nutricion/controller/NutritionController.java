package com.spartangoldengym.nutricion.controller;

import com.spartangoldengym.nutricion.dto.*;
import com.spartangoldengym.nutricion.service.MealService;
import com.spartangoldengym.nutricion.service.NutritionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/nutrition")
public class NutritionController {

    private final NutritionService nutritionService;
    private final MealService mealService;

    public NutritionController(NutritionService nutritionService, MealService mealService) {
        this.nutritionService = nutritionService;
        this.mealService = mealService;
    }

    // --- Nutrition Plans ---

    @PostMapping("/plans")
    public ResponseEntity<NutritionPlanResponse> createPlan(
            @Valid @RequestBody CreateNutritionPlanRequest request) {
        NutritionPlanResponse response = nutritionService.createPlan(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/plans")
    public ResponseEntity<List<NutritionPlanResponse>> listPlans(
            @RequestParam(required = false) UUID userId) {
        List<NutritionPlanResponse> plans = nutritionService.listPlans(userId);
        return ResponseEntity.ok(plans);
    }

    // --- Foods ---

    @GetMapping("/foods")
    public ResponseEntity<List<FoodResponse>> listFoods(
            @RequestParam(required = false) String region) {
        List<FoodResponse> foods = nutritionService.listFoods(region);
        return ResponseEntity.ok(foods);
    }

    @GetMapping("/foods/barcode/{code}")
    public ResponseEntity<FoodResponse> findByBarcode(@PathVariable String code) {
        FoodResponse food = nutritionService.findByBarcode(code);
        return ResponseEntity.ok(food);
    }

    // --- Recipes ---

    @GetMapping("/recipes")
    public ResponseEntity<List<RecipeResponse>> getRecipes(
            @RequestParam(required = false) String goal,
            @RequestParam(required = false) String preference) {
        List<RecipeResponse> recipes = nutritionService.getRecipes(goal, preference);
        return ResponseEntity.ok(recipes);
    }

    // --- Supplements ---

    @GetMapping("/supplements")
    public ResponseEntity<List<SupplementResponse>> getSupplements() {
        List<SupplementResponse> supplements = nutritionService.getSupplements();
        return ResponseEntity.ok(supplements);
    }

    // --- Meals ---

    @PostMapping("/meals")
    public ResponseEntity<MealLogResponse> logMeal(
            @Valid @RequestBody LogMealRequest request) {
        MealLogResponse response = mealService.logMeal(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/daily-balance")
    public ResponseEntity<DailyBalanceResponse> getDailyBalance(
            @RequestParam UUID userId,
            @RequestParam(required = false) String date) {
        LocalDate targetDate = (date != null) ? LocalDate.parse(date) : LocalDate.now();
        DailyBalanceResponse balance = mealService.getDailyBalance(userId, targetDate);
        return ResponseEntity.ok(balance);
    }
}
