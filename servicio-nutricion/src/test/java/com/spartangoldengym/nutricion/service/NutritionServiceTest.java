package com.spartangoldengym.nutricion.service;

import com.spartangoldengym.common.exception.ResourceNotFoundException;
import com.spartangoldengym.nutricion.dto.*;
import com.spartangoldengym.nutricion.entity.Food;
import com.spartangoldengym.nutricion.entity.NutritionPlan;
import com.spartangoldengym.nutricion.repository.FoodRepository;
import com.spartangoldengym.nutricion.repository.NutritionPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NutritionServiceTest {

    @Mock
    private NutritionPlanRepository planRepository;

    @Mock
    private FoodRepository foodRepository;

    @InjectMocks
    private NutritionService nutritionService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    // --- Plan creation ---

    @Test
    void createPlan_withExplicitMacros_persistsProvidedValues() {
        CreateNutritionPlanRequest request = new CreateNutritionPlanRequest();
        request.setUserId(userId);
        request.setGoal("muscle_gain");
        request.setDailyCalories(3000);
        request.setProteinGrams(200);
        request.setCarbsGrams(350);
        request.setFatGrams(90);

        when(planRepository.save(any(NutritionPlan.class))).thenAnswer(inv -> {
            NutritionPlan p = inv.getArgument(0);
            p.setId(UUID.randomUUID());
            p.setCreatedAt(Instant.now());
            return p;
        });

        NutritionPlanResponse response = nutritionService.createPlan(request);

        assertNotNull(response.getId());
        assertEquals(userId, response.getUserId());
        assertEquals("muscle_gain", response.getGoal());
        assertEquals(3000, response.getDailyCalories());
        assertEquals(200, response.getProteinGrams());
    }

    @Test
    void createPlan_withoutMacros_appliesDefaults() {
        CreateNutritionPlanRequest request = new CreateNutritionPlanRequest();
        request.setUserId(userId);
        request.setGoal("weight_loss");

        ArgumentCaptor<NutritionPlan> captor = ArgumentCaptor.forClass(NutritionPlan.class);
        when(planRepository.save(captor.capture())).thenAnswer(inv -> {
            NutritionPlan p = inv.getArgument(0);
            p.setId(UUID.randomUUID());
            p.setCreatedAt(Instant.now());
            return p;
        });

        NutritionPlanResponse response = nutritionService.createPlan(request);

        assertEquals(1800, response.getDailyCalories());
        assertEquals(150, response.getProteinGrams());
    }

    @Test
    void createPlan_maintenanceGoal_appliesMaintenanceDefaults() {
        CreateNutritionPlanRequest request = new CreateNutritionPlanRequest();
        request.setUserId(userId);
        request.setGoal("maintenance");

        when(planRepository.save(any(NutritionPlan.class))).thenAnswer(inv -> {
            NutritionPlan p = inv.getArgument(0);
            p.setId(UUID.randomUUID());
            p.setCreatedAt(Instant.now());
            return p;
        });

        NutritionPlanResponse response = nutritionService.createPlan(request);

        assertEquals(2200, response.getDailyCalories());
        assertEquals(130, response.getProteinGrams());
    }

    // --- List plans ---

    @Test
    void listPlans_withUserId_filtersCorrectly() {
        NutritionPlan plan = makePlan(userId, "muscle_gain");
        when(planRepository.findByUserId(userId)).thenReturn(Collections.singletonList(plan));

        List<NutritionPlanResponse> result = nutritionService.listPlans(userId);

        assertEquals(1, result.size());
        assertEquals(userId, result.get(0).getUserId());
        verify(planRepository).findByUserId(userId);
    }

    @Test
    void listPlans_withoutUserId_returnsAll() {
        when(planRepository.findAll()).thenReturn(Collections.emptyList());

        List<NutritionPlanResponse> result = nutritionService.listPlans(null);

        assertTrue(result.isEmpty());
        verify(planRepository).findAll();
    }

    // --- Foods ---

    @Test
    void listFoods_withRegion_filtersCorrectly() {
        Food food = makeFood("Tortilla", "MX");
        when(foodRepository.findByRegion("MX")).thenReturn(Collections.singletonList(food));

        List<FoodResponse> result = nutritionService.listFoods("MX");

        assertEquals(1, result.size());
        assertEquals("Tortilla", result.get(0).getName());
        assertEquals("MX", result.get(0).getRegion());
    }

    @Test
    void listFoods_withoutRegion_returnsAll() {
        when(foodRepository.findAll()).thenReturn(Collections.emptyList());

        List<FoodResponse> result = nutritionService.listFoods(null);

        assertTrue(result.isEmpty());
        verify(foodRepository).findAll();
    }

    @Test
    void findByBarcode_existing_returnsFood() {
        Food food = makeFood("Protein Bar", "US");
        food.setBarcode("1234567890");
        when(foodRepository.findByBarcode("1234567890")).thenReturn(Optional.of(food));

        FoodResponse result = nutritionService.findByBarcode("1234567890");

        assertEquals("Protein Bar", result.getName());
        assertEquals("1234567890", result.getBarcode());
    }

    @Test
    void findByBarcode_notFound_throwsException() {
        when(foodRepository.findByBarcode("9999")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> nutritionService.findByBarcode("9999"));
    }

    // --- Recipes ---

    @Test
    void getRecipes_withGoalFilter_returnsMatchingRecipes() {
        List<RecipeResponse> result = nutritionService.getRecipes("weight_loss", null);

        assertFalse(result.isEmpty());
        result.forEach(r -> assertEquals("weight_loss", r.getGoal()));
    }

    @Test
    void getRecipes_withoutFilter_returnsAll() {
        List<RecipeResponse> result = nutritionService.getRecipes(null, null);

        assertEquals(3, result.size());
    }

    // --- Supplements ---

    @Test
    void getSupplements_returnsAllCategories() {
        List<SupplementResponse> result = nutritionService.getSupplements();

        assertEquals(3, result.size());
        Set<String> categories = new HashSet<>();
        result.forEach(s -> categories.add(s.getCategory()));
        assertTrue(categories.contains("protein"));
        assertTrue(categories.contains("creatine"));
        assertTrue(categories.contains("pre_workout"));
    }

    // --- Helpers ---

    private NutritionPlan makePlan(UUID userId, String goal) {
        NutritionPlan plan = new NutritionPlan();
        plan.setId(UUID.randomUUID());
        plan.setUserId(userId);
        plan.setGoal(goal);
        plan.setDailyCalories(2200);
        plan.setProteinGrams(130);
        plan.setCarbsGrams(250);
        plan.setFatGrams(70);
        plan.setCreatedAt(Instant.now());
        return plan;
    }

    private Food makeFood(String name, String region) {
        Food food = new Food();
        food.setId(UUID.randomUUID());
        food.setName(name);
        food.setRegion(region);
        food.setCaloriesPer100g(new BigDecimal("250.00"));
        food.setProteinPer100g(new BigDecimal("10.00"));
        food.setCarbsPer100g(new BigDecimal("30.00"));
        food.setFatPer100g(new BigDecimal("8.00"));
        return food;
    }
}
