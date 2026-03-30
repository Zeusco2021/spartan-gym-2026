package com.spartangoldengym.iacoach.model;

import com.spartangoldengym.iacoach.dto.WarmupResponse.WarmupExercise;

import java.util.*;

/**
 * Rules for warmup recommendations based on planned exercise types.
 * Req 3.5: Recommend warmup exercises prior to each routine, including analysis
 * of whether warmup is recommended based on the type of planned exercise.
 */
public final class WarmupRules {

    private WarmupRules() {}

    // Exercise types that strongly benefit from warmup
    private static final Set<String> HIGH_WARMUP_TYPES = new HashSet<>(Arrays.asList(
            "strength", "powerlifting", "heavy_compound", "plyometrics", "sprinting"
    ));

    // Exercise types where warmup is optional/light
    private static final Set<String> LOW_WARMUP_TYPES = new HashSet<>(Arrays.asList(
            "stretching", "yoga", "walking", "light_cardio"
    ));

    public static boolean isWarmupRecommended(List<String> exerciseTypes) {
        if (exerciseTypes == null || exerciseTypes.isEmpty()) return true;
        for (String type : exerciseTypes) {
            if (HIGH_WARMUP_TYPES.contains(type.toLowerCase())) return true;
        }
        for (String type : exerciseTypes) {
            if (!LOW_WARMUP_TYPES.contains(type.toLowerCase())) return true;
        }
        return false;
    }

    public static String getRationale(List<String> exerciseTypes) {
        if (exerciseTypes == null || exerciseTypes.isEmpty()) {
            return "General warmup recommended to prepare muscles and joints.";
        }
        for (String type : exerciseTypes) {
            if (HIGH_WARMUP_TYPES.contains(type.toLowerCase())) {
                return "Warmup strongly recommended for " + type + " exercises to prevent injury and improve performance.";
            }
        }
        for (String type : exerciseTypes) {
            if (LOW_WARMUP_TYPES.contains(type.toLowerCase())) {
                return "Light warmup optional for " + type + " exercises as they are low intensity.";
            }
        }
        return "Warmup recommended to prepare muscles and joints for the planned exercises.";
    }

    public static List<WarmupExercise> generateWarmupExercises(List<String> exerciseTypes, List<String> targetMuscleGroups) {
        List<WarmupExercise> warmups = new ArrayList<>();

        // General cardio warmup
        WarmupExercise cardio = new WarmupExercise();
        cardio.setName("Light Jogging / Jump Rope");
        cardio.setDurationSeconds(300);
        cardio.setDescription("5 minutes of light cardio to raise heart rate and body temperature");
        cardio.setTargetMuscleGroups(Arrays.asList("full_body"));
        warmups.add(cardio);

        // Dynamic stretching
        WarmupExercise dynamic = new WarmupExercise();
        dynamic.setName("Dynamic Stretching");
        dynamic.setDurationSeconds(180);
        dynamic.setDescription("Arm circles, leg swings, hip rotations");
        dynamic.setTargetMuscleGroups(Arrays.asList("full_body"));
        warmups.add(dynamic);

        // Muscle-group specific warmups
        if (targetMuscleGroups != null) {
            for (String mg : targetMuscleGroups) {
                WarmupExercise specific = buildMuscleGroupWarmup(mg);
                if (specific != null) warmups.add(specific);
            }
        }

        return warmups;
    }

    private static WarmupExercise buildMuscleGroupWarmup(String muscleGroup) {
        WarmupExercise w = new WarmupExercise();
        w.setTargetMuscleGroups(Arrays.asList(muscleGroup));

        switch (muscleGroup.toLowerCase()) {
            case "chest":
            case "shoulders":
                w.setName("Arm Circles & Band Pull-aparts");
                w.setDurationSeconds(120);
                w.setDescription("Shoulder mobility and chest activation");
                return w;
            case "back":
                w.setName("Cat-Cow Stretch & Scapular Retractions");
                w.setDurationSeconds(120);
                w.setDescription("Spine mobility and back activation");
                return w;
            case "quadriceps":
            case "hamstrings":
            case "glutes":
                w.setName("Bodyweight Squats & Leg Swings");
                w.setDurationSeconds(120);
                w.setDescription("Lower body activation and hip mobility");
                return w;
            case "core":
                w.setName("Dead Bug & Bird Dog");
                w.setDurationSeconds(120);
                w.setDescription("Core activation and spinal stability");
                return w;
            case "biceps":
            case "triceps":
                w.setName("Light Band Curls & Extensions");
                w.setDurationSeconds(90);
                w.setDescription("Arm activation with light resistance");
                return w;
            default:
                return null;
        }
    }
}
