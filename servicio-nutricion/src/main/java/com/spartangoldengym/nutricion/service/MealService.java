package com.spartangoldengym.nutricion.service;

import com.spartangoldengym.common.config.KafkaTopics;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MealService {

    private static final Logger log = LoggerFactory.getLogger(MealService.class);
    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final double DEFICIT_EXCESS_THRESHOLD = 0.25;
    private static final int CONSECUTIVE_DAYS_THRESHOLD = 3;

    private final MealLogRepository mealLogRepository;
    private final FoodRepository foodRepository;
    private final NutritionPlanRepository planRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public MealService(MealLogRepository mealLogRepository,
                       FoodRepository foodRepository,
                       NutritionPlanRepository planRepository,
                       KafkaTemplate<String, String> kafkaTemplate) {
        this.mealLogRepository = mealLogRepository;
        this.foodRepository = foodRepository;
        this.planRepository = planRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public MealLogResponse logMeal(LogMealRequest request) {
        Food food = foodRepository.findById(request.getFoodId())
                .orElseThrow(() -> new ResourceNotFoundException("Food", "id=" + request.getFoodId()));

        MealLog mealLog = new MealLog();
        mealLog.setUserId(request.getUserId());
        mealLog.setFoodId(request.getFoodId());
        mealLog.setQuantityGrams(request.getQuantityGrams());
        mealLog.setMealType(request.getMealType());
        mealLog = mealLogRepository.save(mealLog);

        BigDecimal factor = request.getQuantityGrams().divide(HUNDRED, 4, RoundingMode.HALF_UP);
        BigDecimal calories = safeMultiply(food.getCaloriesPer100g(), factor);
        BigDecimal protein = safeMultiply(food.getProteinPer100g(), factor);
        BigDecimal carbs = safeMultiply(food.getCarbsPer100g(), factor);
        BigDecimal fat = safeMultiply(food.getFatPer100g(), factor);

        // Publish to Kafka
        String payload = String.format(
                "{\"mealLogId\":\"%s\",\"userId\":\"%s\",\"foodId\":\"%s\",\"quantityGrams\":%s," +
                "\"mealType\":\"%s\",\"calories\":%s,\"protein\":%s,\"carbs\":%s,\"fat\":%s,\"loggedAt\":\"%s\"}",
                mealLog.getId(), mealLog.getUserId(), mealLog.getFoodId(),
                mealLog.getQuantityGrams().toPlainString(), mealLog.getMealType(),
                calories.toPlainString(), protein.toPlainString(),
                carbs.toPlainString(), fat.toPlainString(), mealLog.getLoggedAt());
        kafkaTemplate.send(KafkaTopics.NUTRITION_LOGS, request.getUserId().toString(), payload);
        log.info("Logged meal id={} for user={}, published to Kafka", mealLog.getId(), mealLog.getUserId());

        // Check 3-day macro deficit/excess
        checkConsecutiveMacroImbalance(request.getUserId());

        return toMealLogResponse(mealLog, food, calories, protein, carbs, fat);
    }

    @Transactional(readOnly = true)
    public DailyBalanceResponse getDailyBalance(UUID userId, LocalDate date) {
        Instant start = date.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        List<MealLog> meals = mealLogRepository.findByUserIdAndLoggedAtBetween(userId, start, end);

        BigDecimal totalCalories = BigDecimal.ZERO;
        BigDecimal totalProtein = BigDecimal.ZERO;
        BigDecimal totalCarbs = BigDecimal.ZERO;
        BigDecimal totalFat = BigDecimal.ZERO;

        for (MealLog meal : meals) {
            Food food = foodRepository.findById(meal.getFoodId()).orElse(null);
            if (food == null) continue;

            BigDecimal factor = meal.getQuantityGrams().divide(HUNDRED, 4, RoundingMode.HALF_UP);
            totalCalories = totalCalories.add(safeMultiply(food.getCaloriesPer100g(), factor));
            totalProtein = totalProtein.add(safeMultiply(food.getProteinPer100g(), factor));
            totalCarbs = totalCarbs.add(safeMultiply(food.getCarbsPer100g(), factor));
            totalFat = totalFat.add(safeMultiply(food.getFatPer100g(), factor));
        }

        DailyBalanceResponse response = new DailyBalanceResponse();
        response.setUserId(userId);
        response.setDate(date);
        response.setTotalCalories(totalCalories.setScale(2, RoundingMode.HALF_UP));
        response.setTotalProtein(totalProtein.setScale(2, RoundingMode.HALF_UP));
        response.setTotalCarbs(totalCarbs.setScale(2, RoundingMode.HALF_UP));
        response.setTotalFat(totalFat.setScale(2, RoundingMode.HALF_UP));
        response.setMealCount(meals.size());
        return response;
    }

    void checkConsecutiveMacroImbalance(UUID userId) {
        List<NutritionPlan> plans = planRepository.findByUserId(userId);
        if (plans.isEmpty()) return;

        NutritionPlan plan = plans.get(plans.size() - 1);
        if (plan.getDailyCalories() == null) return;

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        int consecutiveImbalanceDays = 0;

        for (int i = 0; i < CONSECUTIVE_DAYS_THRESHOLD; i++) {
            LocalDate day = today.minusDays(i);
            DailyBalanceResponse balance = getDailyBalance(userId, day);

            if (balance.getMealCount() == 0) return;

            if (hasSignificantImbalance(balance, plan)) {
                consecutiveImbalanceDays++;
            } else {
                return;
            }
        }

        if (consecutiveImbalanceDays >= CONSECUTIVE_DAYS_THRESHOLD) {
            log.warn("User {} has macro imbalance for {} consecutive days — notifying", userId, CONSECUTIVE_DAYS_THRESHOLD);
            String notification = String.format(
                    "{\"type\":\"macro_imbalance\",\"userId\":\"%s\",\"consecutiveDays\":%d,\"message\":\"Significant macro imbalance detected for %d consecutive days. Please review your nutrition.\"}",
                    userId, CONSECUTIVE_DAYS_THRESHOLD, CONSECUTIVE_DAYS_THRESHOLD);
            kafkaTemplate.send(KafkaTopics.NUTRITION_LOGS, userId.toString(), notification);
        }
    }

    boolean hasSignificantImbalance(DailyBalanceResponse balance, NutritionPlan plan) {
        double calTarget = plan.getDailyCalories();
        double calActual = balance.getTotalCalories().doubleValue();
        if (Math.abs(calActual - calTarget) / calTarget > DEFICIT_EXCESS_THRESHOLD) return true;

        if (plan.getProteinGrams() != null && plan.getProteinGrams() > 0) {
            double proteinActual = balance.getTotalProtein().doubleValue();
            if (Math.abs(proteinActual - plan.getProteinGrams()) / plan.getProteinGrams() > DEFICIT_EXCESS_THRESHOLD) return true;
        }

        if (plan.getCarbsGrams() != null && plan.getCarbsGrams() > 0) {
            double carbsActual = balance.getTotalCarbs().doubleValue();
            if (Math.abs(carbsActual - plan.getCarbsGrams()) / plan.getCarbsGrams() > DEFICIT_EXCESS_THRESHOLD) return true;
        }

        if (plan.getFatGrams() != null && plan.getFatGrams() > 0) {
            double fatActual = balance.getTotalFat().doubleValue();
            if (Math.abs(fatActual - plan.getFatGrams()) / plan.getFatGrams() > DEFICIT_EXCESS_THRESHOLD) return true;
        }

        return false;
    }

    private BigDecimal safeMultiply(BigDecimal value, BigDecimal factor) {
        return value != null ? value.multiply(factor) : BigDecimal.ZERO;
    }

    private MealLogResponse toMealLogResponse(MealLog mealLog, Food food,
                                               BigDecimal calories, BigDecimal protein,
                                               BigDecimal carbs, BigDecimal fat) {
        MealLogResponse r = new MealLogResponse();
        r.setId(mealLog.getId());
        r.setUserId(mealLog.getUserId());
        r.setFoodId(mealLog.getFoodId());
        r.setFoodName(food.getName());
        r.setQuantityGrams(mealLog.getQuantityGrams());
        r.setMealType(mealLog.getMealType());
        r.setCalories(calories.setScale(2, RoundingMode.HALF_UP));
        r.setProtein(protein.setScale(2, RoundingMode.HALF_UP));
        r.setCarbs(carbs.setScale(2, RoundingMode.HALF_UP));
        r.setFat(fat.setScale(2, RoundingMode.HALF_UP));
        r.setLoggedAt(mealLog.getLoggedAt());
        return r;
    }
}
