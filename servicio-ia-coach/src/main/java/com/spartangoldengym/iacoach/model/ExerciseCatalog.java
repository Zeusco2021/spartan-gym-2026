package com.spartangoldengym.iacoach.model;

import java.util.*;

/**
 * In-memory exercise catalog used by the IA Coach for plan generation,
 * alternative suggestions, and warmup recommendations.
 * In production, this data would come from Neptune graph and SageMaker.
 */
public final class ExerciseCatalog {

    private ExerciseCatalog() {}

    private static final List<ExerciseInfo> EXERCISES = new ArrayList<>();

    static {
        // Chest exercises
        add("Bench Press", Arrays.asList("chest", "triceps", "shoulders"), Arrays.asList("barbell", "bench"), "intermediate");
        add("Push-ups", Arrays.asList("chest", "triceps", "shoulders"), Collections.emptyList(), "beginner");
        add("Dumbbell Flyes", Arrays.asList("chest"), Arrays.asList("dumbbells", "bench"), "intermediate");
        add("Incline Push-ups", Arrays.asList("chest", "shoulders"), Collections.emptyList(), "beginner");
        add("Diamond Push-ups", Arrays.asList("chest", "triceps"), Collections.emptyList(), "intermediate");
        add("Cable Crossover", Arrays.asList("chest"), Arrays.asList("cable_machine"), "intermediate");

        // Back exercises
        add("Pull-ups", Arrays.asList("back", "biceps"), Arrays.asList("pull_up_bar"), "intermediate");
        add("Bent-over Row", Arrays.asList("back", "biceps"), Arrays.asList("barbell"), "intermediate");
        add("Superman Hold", Arrays.asList("back", "glutes"), Collections.emptyList(), "beginner");
        add("Inverted Row", Arrays.asList("back", "biceps"), Collections.emptyList(), "beginner");
        add("Lat Pulldown", Arrays.asList("back", "biceps"), Arrays.asList("cable_machine"), "intermediate");

        // Legs exercises
        add("Barbell Squat", Arrays.asList("quadriceps", "glutes", "hamstrings"), Arrays.asList("barbell", "squat_rack"), "intermediate");
        add("Bodyweight Squat", Arrays.asList("quadriceps", "glutes"), Collections.emptyList(), "beginner");
        add("Lunges", Arrays.asList("quadriceps", "glutes", "hamstrings"), Collections.emptyList(), "beginner");
        add("Leg Press", Arrays.asList("quadriceps", "glutes"), Arrays.asList("leg_press_machine"), "intermediate");
        add("Romanian Deadlift", Arrays.asList("hamstrings", "glutes", "back"), Arrays.asList("barbell"), "intermediate");
        add("Glute Bridge", Arrays.asList("glutes", "hamstrings"), Collections.emptyList(), "beginner");
        add("Wall Sit", Arrays.asList("quadriceps"), Collections.emptyList(), "beginner");
        add("Calf Raises", Arrays.asList("calves"), Collections.emptyList(), "beginner");

        // Shoulders exercises
        add("Overhead Press", Arrays.asList("shoulders", "triceps"), Arrays.asList("barbell"), "intermediate");
        add("Pike Push-ups", Arrays.asList("shoulders", "triceps"), Collections.emptyList(), "intermediate");
        add("Lateral Raises", Arrays.asList("shoulders"), Arrays.asList("dumbbells"), "beginner");
        add("Handstand Hold", Arrays.asList("shoulders", "core"), Collections.emptyList(), "advanced");

        // Arms exercises
        add("Barbell Curl", Arrays.asList("biceps"), Arrays.asList("barbell"), "beginner");
        add("Tricep Dips", Arrays.asList("triceps", "chest"), Collections.emptyList(), "intermediate");
        add("Chin-ups", Arrays.asList("biceps", "back"), Arrays.asList("pull_up_bar"), "intermediate");
        add("Tricep Kickback", Arrays.asList("triceps"), Arrays.asList("dumbbells"), "beginner");

        // Core exercises
        add("Plank", Arrays.asList("core", "shoulders"), Collections.emptyList(), "beginner");
        add("Crunches", Arrays.asList("core"), Collections.emptyList(), "beginner");
        add("Mountain Climbers", Arrays.asList("core", "shoulders", "quadriceps"), Collections.emptyList(), "intermediate");
        add("Leg Raises", Arrays.asList("core"), Collections.emptyList(), "beginner");
        add("Russian Twist", Arrays.asList("core"), Collections.emptyList(), "intermediate");
        add("Ab Rollout", Arrays.asList("core"), Arrays.asList("ab_wheel"), "advanced");
    }

    private static void add(String name, List<String> muscleGroups, List<String> equipment, String difficulty) {
        EXERCISES.add(new ExerciseInfo(
                UUID.nameUUIDFromBytes(name.getBytes()),
                name, muscleGroups, equipment, difficulty));
    }

    public static List<ExerciseInfo> getAll() {
        return Collections.unmodifiableList(EXERCISES);
    }

    public static Optional<ExerciseInfo> findById(UUID id) {
        return EXERCISES.stream().filter(e -> e.getId().equals(id)).findFirst();
    }

    public static Optional<ExerciseInfo> findByName(String name) {
        return EXERCISES.stream().filter(e -> e.getName().equalsIgnoreCase(name)).findFirst();
    }

    public static List<ExerciseInfo> findByMuscleGroup(String muscleGroup) {
        List<ExerciseInfo> result = new ArrayList<>();
        for (ExerciseInfo e : EXERCISES) {
            if (e.getMuscleGroups().contains(muscleGroup.toLowerCase())) {
                result.add(e);
            }
        }
        return result;
    }

    public static List<ExerciseInfo> findBodyweightOnly() {
        List<ExerciseInfo> result = new ArrayList<>();
        for (ExerciseInfo e : EXERCISES) {
            if (e.getEquipmentRequired().isEmpty()) {
                result.add(e);
            }
        }
        return result;
    }

    /**
     * Find alternatives for a given exercise that target the same muscle groups.
     * Optionally filter by available equipment.
     */
    public static List<ExerciseInfo> findAlternatives(UUID exerciseId, List<String> availableEquipment) {
        Optional<ExerciseInfo> original = findById(exerciseId);
        if (!original.isPresent()) return Collections.emptyList();

        ExerciseInfo orig = original.get();
        List<ExerciseInfo> result = new ArrayList<>();
        for (ExerciseInfo e : EXERCISES) {
            if (e.getId().equals(exerciseId)) continue;
            if (!hasOverlappingMuscleGroups(orig, e)) continue;
            if (availableEquipment != null) {
                // If available equipment is specified (even empty), filter exercises
                // that require equipment not in the available list
                if (!e.getEquipmentRequired().isEmpty() && !availableEquipment.containsAll(e.getEquipmentRequired())) {
                    continue;
                }
                // If available equipment is empty, only allow bodyweight exercises
                if (availableEquipment.isEmpty() && !e.getEquipmentRequired().isEmpty()) {
                    continue;
                }
            }
            result.add(e);
        }
        return result;
    }

    private static boolean hasOverlappingMuscleGroups(ExerciseInfo a, ExerciseInfo b) {
        for (String mg : a.getMuscleGroups()) {
            if (b.getMuscleGroups().contains(mg)) return true;
        }
        return false;
    }
}
