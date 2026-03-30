package com.spartangoldengym.nutricion.service;

import com.spartangoldengym.common.exception.ResourceNotFoundException;
import com.spartangoldengym.nutricion.dto.DailyBalanceResponse;
import com.spartangoldengym.nutricion.dto.LogMealRequest;
import com.spartangoldengym.nutricion.dto.MealLogResponse;
import com.spartangoldengym.nutricion.entity.Food;
import com.spartangoldengym.nutricion.entity.MealLog;
import com.spartangoldengym.nutricion.entity.NutritionPlan;
import com.spartangoldengym.nutricion.repository.FoodRepository;
import com.spartangoldengym.nutricion.repository.MealLogRepository;
import com.spartangoldengym.nutricion.repository.NutritionPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MealServiceTest {

    @Mock
    private MealLogRepository mealLogRepository;
    @Mock
    private FoodRepository foodRepository;
    @Mock
    private NutritionPlanRepository planRepository;
    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private MealService mealService;
    private UUID userId;
    private UUID foodId;

    @BeforeEach
    void setUp() {
        mealService = new MealService(mealLogRepository, foodRepository, planRepository, kafkaTemplate);
        userId = UUID.randomUUID();
        foodId = UUID.randomUUID();
    }

    // --- logMeal ---

    @Test
    void logMeal_validRequest_savesAndPublishesToKafka() {
        Food food = makeFood(foodId, "Chicken Breast", new BigDecimal("165"), new BigDecimal("31"),
                new BigDecimal("0"), new BigDecimal("3.6"));
        when(foodRepository.findById(foodId)).thenReturn(Optional.of(food));
        when(planRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
        when(mealLogRepository.save(any(MealLog.class))).thenAnswer(inv -> {
            MealLog ml = inv.getArgument(0);
            ml.setId(UUID.randomUUID());
            ml.setLoggedAt(Instant.now());
            return ml;
        });

        LogMealRequest request = new LogMealRequest();
        request.setUserId(userId);
        request.setFoodId(foodId);
        request.setQuantityGrams(new BigDecimal("200"));
        request.setMealType("lunch");

        MealLogResponse response = mealService.logMeal(request);

        assertNotNull(response.getId());
        assertEquals(userId, response.getUserId());
        assertEquals("Chicken Breast", response.getFoodName());
        assertEquals("lunch", response.getMealType());
        // 200g of chicken: 165 * 200/100 = 330 calories
        assertEquals(0, new BigDecimal("330.00").compareTo(response.getCalories()));
        // protein: 31 * 200/100 = 62
        assertEquals(0, new BigDecimal("62.00").compareTo(response.getProtein()));

        verify(mealLogRepository).save(any(MealLog.class));
        verify(kafkaTemplate).send(eq("nutrition.logs"), eq(userId.toString()), contains("mealLogId"));
    }

    @Test
    void logMeal_foodNotFound_throwsException() {
        when(foodRepository.findById(foodId)).thenReturn(Optional.empty());

        LogMealRequest request = new LogMealRequest();
        request.setUserId(userId);
        request.setFoodId(foodId);
        request.setQuantityGrams(new BigDecimal("100"));
        request.setMealType("breakfast");

        assertThrows(ResourceNotFoundException.class, () -> mealService.logMeal(request));
        verify(mealLogRepository, never()).save(any());
    }

    // --- getDailyBalance ---

    @Test
    void getDailyBalance_withMeals_returnsSumOfMacros() {
        LocalDate date = LocalDate.of(2024, 1, 15);
        Instant start = date.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        UUID food1Id = UUID.randomUUID();
        UUID food2Id = UUID.randomUUID();

        MealLog meal1 = makeMealLog(userId, food1Id, new BigDecimal("200"), "lunch");
        MealLog meal2 = makeMealLog(userId, food2Id, new BigDecimal("100"), "dinner");

        when(mealLogRepository.findByUserIdAndLoggedAtBetween(userId, start, end))
                .thenReturn(Arrays.asList(meal1, meal2));

        Food f1 = makeFood(food1Id, "Chicken", new BigDecimal("165"), new BigDecimal("31"),
                new BigDecimal("0"), new BigDecimal("3.6"));
        Food f2 = makeFood(food2Id, "Rice", new BigDecimal("130"), new BigDecimal("2.7"),
                new BigDecimal("28"), new BigDecimal("0.3"));

        when(foodRepository.findById(food1Id)).thenReturn(Optional.of(f1));
        when(foodRepository.findById(food2Id)).thenReturn(Optional.of(f2));

        DailyBalanceResponse balance = mealService.getDailyBalance(userId, date);

        assertEquals(userId, balance.getUserId());
        assertEquals(date, balance.getDate());
        assertEquals(2, balance.getMealCount());
        // Chicken 200g: 330 cal + Rice 100g: 130 cal = 460
        assertEquals(0, new BigDecimal("460.00").compareTo(balance.getTotalCalories()));
        // Protein: 62 + 2.7 = 64.7
        assertEquals(0, new BigDecimal("64.70").compareTo(balance.getTotalProtein()));
    }

    @Test
    void getDailyBalance_noMeals_returnsZeros() {
        LocalDate date = LocalDate.of(2024, 1, 15);
        when(mealLogRepository.findByUserIdAndLoggedAtBetween(eq(userId), any(), any()))
                .thenReturn(Collections.emptyList());

        DailyBalanceResponse balance = mealService.getDailyBalance(userId, date);

        assertEquals(0, balance.getMealCount());
        assertEquals(0, BigDecimal.ZERO.compareTo(balance.getTotalCalories()));
    }

    // --- hasSignificantImbalance ---

    @Test
    void hasSignificantImbalance_withinThreshold_returnsFalse() {
        NutritionPlan plan = makePlan(2000, 150, 250, 70);
        DailyBalanceResponse balance = makeBalance(new BigDecimal("1900"), new BigDecimal("140"),
                new BigDecimal("240"), new BigDecimal("65"));

        assertFalse(mealService.hasSignificantImbalance(balance, plan));
    }

    @Test
    void hasSignificantImbalance_calorieDeficit_returnsTrue() {
        NutritionPlan plan = makePlan(2000, 150, 250, 70);
        // 1200 cal is 40% below target (> 25% threshold)
        DailyBalanceResponse balance = makeBalance(new BigDecimal("1200"), new BigDecimal("140"),
                new BigDecimal("240"), new BigDecimal("65"));

        assertTrue(mealService.hasSignificantImbalance(balance, plan));
    }

    @Test
    void hasSignificantImbalance_proteinExcess_returnsTrue() {
        NutritionPlan plan = makePlan(2000, 100, 250, 70);
        // Protein 200 is 100% above target (> 25% threshold)
        DailyBalanceResponse balance = makeBalance(new BigDecimal("2000"), new BigDecimal("200"),
                new BigDecimal("250"), new BigDecimal("70"));

        assertTrue(mealService.hasSignificantImbalance(balance, plan));
    }

    // --- Helpers ---

    private Food makeFood(UUID id, String name, BigDecimal cal, BigDecimal protein,
                          BigDecimal carbs, BigDecimal fat) {
        Food food = new Food();
        food.setId(id);
        food.setName(name);
        food.setCaloriesPer100g(cal);
        food.setProteinPer100g(protein);
        food.setCarbsPer100g(carbs);
        food.setFatPer100g(fat);
        return food;
    }

    private MealLog makeMealLog(UUID userId, UUID foodId, BigDecimal qty, String type) {
        MealLog ml = new MealLog();
        ml.setId(UUID.randomUUID());
        ml.setUserId(userId);
        ml.setFoodId(foodId);
        ml.setQuantityGrams(qty);
        ml.setMealType(type);
        ml.setLoggedAt(Instant.now());
        return ml;
    }

    private NutritionPlan makePlan(int cal, int protein, int carbs, int fat) {
        NutritionPlan plan = new NutritionPlan();
        plan.setId(UUID.randomUUID());
        plan.setUserId(userId);
        plan.setGoal("maintenance");
        plan.setDailyCalories(cal);
        plan.setProteinGrams(protein);
        plan.setCarbsGrams(carbs);
        plan.setFatGrams(fat);
        return plan;
    }

    private DailyBalanceResponse makeBalance(BigDecimal cal, BigDecimal protein,
                                              BigDecimal carbs, BigDecimal fat) {
        DailyBalanceResponse b = new DailyBalanceResponse();
        b.setUserId(userId);
        b.setDate(LocalDate.now());
        b.setTotalCalories(cal);
        b.setTotalProtein(protein);
        b.setTotalCarbs(carbs);
        b.setTotalFat(fat);
        b.setMealCount(3);
        return b;
    }
}
