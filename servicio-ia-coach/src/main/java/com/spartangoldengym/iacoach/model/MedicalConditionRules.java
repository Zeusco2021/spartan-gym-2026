package com.spartangoldengym.iacoach.model;

import java.util.*;

/**
 * Rules mapping medical conditions to contraindicated exercise types/muscle groups.
 * Used to filter exercises when generating personalized plans.
 */
public final class MedicalConditionRules {

    private MedicalConditionRules() {}

    private static final Map<String, Set<String>> CONTRAINDICATED_EXERCISES = new HashMap<>();

    static {
        // Back problems: avoid heavy spinal loading
        CONTRAINDICATED_EXERCISES.put("back_problems", new HashSet<>(Arrays.asList(
                "Barbell Squat", "Romanian Deadlift", "Bent-over Row", "Overhead Press", "Ab Rollout"
        )));

        // Thrombosis: avoid heavy static holds and high-pressure exercises
        CONTRAINDICATED_EXERCISES.put("thrombosis", new HashSet<>(Arrays.asList(
                "Barbell Squat", "Leg Press", "Wall Sit", "Handstand Hold"
        )));

        // Diabetes: no specific exercise contraindications, but avoid extreme intensity
        CONTRAINDICATED_EXERCISES.put("diabetes", new HashSet<>(Arrays.asList(
                "Handstand Hold"
        )));

        // Knee problems: avoid high-impact knee exercises
        CONTRAINDICATED_EXERCISES.put("knee_problems", new HashSet<>(Arrays.asList(
                "Barbell Squat", "Lunges", "Leg Press", "Mountain Climbers"
        )));

        // Shoulder injury: avoid overhead and pressing movements
        CONTRAINDICATED_EXERCISES.put("shoulder_injury", new HashSet<>(Arrays.asList(
                "Overhead Press", "Bench Press", "Handstand Hold", "Pike Push-ups"
        )));

        // Heart condition: avoid very intense exercises
        CONTRAINDICATED_EXERCISES.put("heart_condition", new HashSet<>(Arrays.asList(
                "Handstand Hold", "Mountain Climbers"
        )));
    }

    public static Set<String> getContraindicatedExercises(String condition) {
        return CONTRAINDICATED_EXERCISES.getOrDefault(
                condition.toLowerCase().replace(" ", "_"),
                Collections.emptySet());
    }

    public static Set<String> getAllContraindicatedExercises(List<String> conditions) {
        if (conditions == null || conditions.isEmpty()) return Collections.emptySet();
        Set<String> result = new HashSet<>();
        for (String condition : conditions) {
            result.addAll(getContraindicatedExercises(condition));
        }
        return result;
    }

    public static boolean isContraindicated(String exerciseName, List<String> conditions) {
        return getAllContraindicatedExercises(conditions).contains(exerciseName);
    }
}
