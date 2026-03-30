package com.spartangoldengym.nutricion.service;

import com.spartangoldengym.common.exception.ResourceNotFoundException;
import com.spartangoldengym.nutricion.dto.*;
import com.spartangoldengym.nutricion.entity.Food;
import com.spartangoldengym.nutricion.entity.NutritionPlan;
import com.spartangoldengym.nutricion.repository.FoodRepository;
import com.spartangoldengym.nutricion.repository.NutritionPlanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class NutritionService {

    private static final Logger log = LoggerFactory.getLogger(NutritionService.class);

    private final NutritionPlanRepository planRepository;
    private final FoodRepository foodRepository;

    public NutritionService(NutritionPlanRepository planRepository, FoodRepository foodRepository) {
        this.planRepository = planRepository;
        this.foodRepository = foodRepository;
    }

    // --- Nutrition Plans ---

    @Transactional
    public NutritionPlanResponse createPlan(CreateNutritionPlanRequest request) {
        NutritionPlan plan = new NutritionPlan();
        plan.setUserId(request.getUserId());
        plan.setGoal(request.getGoal());

        if (request.getDailyCalories() != null) {
            plan.setDailyCalories(request.getDailyCalories());
            plan.setProteinGrams(request.getProteinGrams());
            plan.setCarbsGrams(request.getCarbsGrams());
            plan.setFatGrams(request.getFatGrams());
        } else {
            applyDefaultMacros(plan, request.getGoal());
        }

        plan = planRepository.save(plan);
        log.info("Created nutrition plan id={} for user={} goal={}", plan.getId(), plan.getUserId(), plan.getGoal());
        return toResponse(plan);
    }

    @Transactional(readOnly = true)
    public List<NutritionPlanResponse> listPlans(UUID userId) {
        List<NutritionPlan> plans = (userId != null)
                ? planRepository.findByUserId(userId)
                : planRepository.findAll();
        return plans.stream().map(this::toResponse).collect(Collectors.toList());
    }

    // --- Foods ---

    @Transactional(readOnly = true)
    public List<FoodResponse> listFoods(String region) {
        List<Food> foods = (region != null)
                ? foodRepository.findByRegion(region)
                : foodRepository.findAll();
        return foods.stream().map(this::toFoodResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FoodResponse findByBarcode(String barcode) {
        Food food = foodRepository.findByBarcode(barcode)
                .orElseThrow(() -> new ResourceNotFoundException("Food", "barcode=" + barcode));
        return toFoodResponse(food);
    }

    // --- Recipes (stub/in-memory) ---

    public List<RecipeResponse> getRecipes(String goal, String preference) {
        List<RecipeResponse> recipes = buildStubRecipes();
        return recipes.stream()
                .filter(r -> goal == null || goal.equalsIgnoreCase(r.getGoal()))
                .collect(Collectors.toList());
    }

    // --- Supplements (stub/in-memory) ---

    public List<SupplementResponse> getSupplements() {
        return buildStubSupplements();
    }

    // --- Helpers ---

    void applyDefaultMacros(NutritionPlan plan, String goal) {
        switch (goal.toLowerCase()) {
            case "muscle_gain":
                plan.setDailyCalories(2800);
                plan.setProteinGrams(180);
                plan.setCarbsGrams(300);
                plan.setFatGrams(80);
                break;
            case "weight_loss":
                plan.setDailyCalories(1800);
                plan.setProteinGrams(150);
                plan.setCarbsGrams(150);
                plan.setFatGrams(60);
                break;
            default: // maintenance
                plan.setDailyCalories(2200);
                plan.setProteinGrams(130);
                plan.setCarbsGrams(250);
                plan.setFatGrams(70);
                break;
        }
    }

    NutritionPlanResponse toResponse(NutritionPlan plan) {
        NutritionPlanResponse r = new NutritionPlanResponse();
        r.setId(plan.getId());
        r.setUserId(plan.getUserId());
        r.setGoal(plan.getGoal());
        r.setDailyCalories(plan.getDailyCalories());
        r.setProteinGrams(plan.getProteinGrams());
        r.setCarbsGrams(plan.getCarbsGrams());
        r.setFatGrams(plan.getFatGrams());
        r.setCreatedAt(plan.getCreatedAt());
        return r;
    }

    FoodResponse toFoodResponse(Food food) {
        FoodResponse r = new FoodResponse();
        r.setId(food.getId());
        r.setName(food.getName());
        r.setBarcode(food.getBarcode());
        r.setCaloriesPer100g(food.getCaloriesPer100g());
        r.setProteinPer100g(food.getProteinPer100g());
        r.setCarbsPer100g(food.getCarbsPer100g());
        r.setFatPer100g(food.getFatPer100g());
        r.setMicronutrients(food.getMicronutrients());
        r.setRegion(food.getRegion());
        return r;
    }

    private List<RecipeResponse> buildStubRecipes() {
        List<RecipeResponse> recipes = new ArrayList<>();

        RecipeResponse r1 = new RecipeResponse();
        r1.setId("recipe-1");
        r1.setName("Grilled Chicken Salad");
        r1.setDescription("High-protein salad with grilled chicken breast");
        r1.setCalories(450);
        r1.setProteinGrams(42);
        r1.setCarbsGrams(20);
        r1.setFatGrams(22);
        r1.setIngredients(Arrays.asList("chicken breast", "mixed greens", "olive oil", "tomatoes", "avocado"));
        r1.setInstructions("Grill chicken, toss with greens and dressing.");
        r1.setGoal("weight_loss");
        recipes.add(r1);

        RecipeResponse r2 = new RecipeResponse();
        r2.setId("recipe-2");
        r2.setName("Protein Oatmeal Bowl");
        r2.setDescription("High-carb oatmeal with whey protein for muscle gain");
        r2.setCalories(650);
        r2.setProteinGrams(40);
        r2.setCarbsGrams(80);
        r2.setFatGrams(15);
        r2.setIngredients(Arrays.asList("oats", "whey protein", "banana", "peanut butter", "milk"));
        r2.setInstructions("Cook oats, stir in protein powder, top with banana and peanut butter.");
        r2.setGoal("muscle_gain");
        recipes.add(r2);

        RecipeResponse r3 = new RecipeResponse();
        r3.setId("recipe-3");
        r3.setName("Balanced Quinoa Bowl");
        r3.setDescription("Balanced meal with quinoa, vegetables and lean protein");
        r3.setCalories(520);
        r3.setProteinGrams(30);
        r3.setCarbsGrams(55);
        r3.setFatGrams(18);
        r3.setIngredients(Arrays.asList("quinoa", "black beans", "corn", "bell pepper", "lime"));
        r3.setInstructions("Cook quinoa, mix with beans and vegetables, dress with lime.");
        r3.setGoal("maintenance");
        recipes.add(r3);

        return recipes;
    }

    private List<SupplementResponse> buildStubSupplements() {
        List<SupplementResponse> supplements = new ArrayList<>();

        SupplementResponse s1 = new SupplementResponse();
        s1.setId("supp-1");
        s1.setName("Whey Protein Isolate");
        s1.setCategory("protein");
        s1.setDosage("25-30g per serving, 1-2 servings daily");
        s1.setBenefits("Supports muscle recovery and growth, high bioavailability");
        s1.setWarnings("May cause digestive issues in lactose-intolerant individuals");
        supplements.add(s1);

        SupplementResponse s2 = new SupplementResponse();
        s2.setId("supp-2");
        s2.setName("Creatine Monohydrate");
        s2.setCategory("creatine");
        s2.setDosage("5g daily");
        s2.setBenefits("Increases strength, power output and muscle hydration");
        s2.setWarnings("Stay well hydrated; consult physician if you have kidney conditions");
        supplements.add(s2);

        SupplementResponse s3 = new SupplementResponse();
        s3.setId("supp-3");
        s3.setName("Pre-Workout Formula");
        s3.setCategory("pre_workout");
        s3.setDosage("1 scoop 20-30 minutes before training");
        s3.setBenefits("Increases energy, focus and endurance during workouts");
        s3.setWarnings("Contains caffeine; avoid late evening use; do not exceed recommended dose");
        supplements.add(s3);

        return supplements;
    }
}
