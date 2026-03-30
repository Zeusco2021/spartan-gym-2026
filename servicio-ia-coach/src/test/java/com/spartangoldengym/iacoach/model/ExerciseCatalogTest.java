package com.spartangoldengym.iacoach.model;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ExerciseCatalogTest {

    @Test
    void getAll_returnsNonEmptyCatalog() {
        assertFalse(ExerciseCatalog.getAll().isEmpty());
    }

    @Test
    void findByName_existingExercise_returnsExercise() {
        assertTrue(ExerciseCatalog.findByName("Bench Press").isPresent());
        assertTrue(ExerciseCatalog.findByName("Push-ups").isPresent());
    }

    @Test
    void findByName_nonExisting_returnsEmpty() {
        assertFalse(ExerciseCatalog.findByName("NonExistent").isPresent());
    }

    @Test
    void findByMuscleGroup_chest_returnsChestExercises() {
        List<ExerciseInfo> chest = ExerciseCatalog.findByMuscleGroup("chest");
        assertFalse(chest.isEmpty());
        for (ExerciseInfo e : chest) {
            assertTrue(e.getMuscleGroups().contains("chest"));
        }
    }

    @Test
    void findBodyweightOnly_allHaveNoEquipment() {
        List<ExerciseInfo> bodyweight = ExerciseCatalog.findBodyweightOnly();
        assertFalse(bodyweight.isEmpty());
        for (ExerciseInfo e : bodyweight) {
            assertTrue(e.getEquipmentRequired().isEmpty(),
                    e.getName() + " should not require equipment");
        }
    }

    @Test
    void findAlternatives_benchPress_returnsAlternativesWithSameMuscleGroups() {
        ExerciseInfo benchPress = ExerciseCatalog.findByName("Bench Press").orElseThrow(
                () -> new RuntimeException("Bench Press not found"));

        List<ExerciseInfo> alternatives = ExerciseCatalog.findAlternatives(benchPress.getId(), null);

        assertFalse(alternatives.isEmpty());
        for (ExerciseInfo alt : alternatives) {
            assertNotEquals(benchPress.getId(), alt.getId());
            boolean hasOverlap = false;
            for (String mg : alt.getMuscleGroups()) {
                if (benchPress.getMuscleGroups().contains(mg)) {
                    hasOverlap = true;
                    break;
                }
            }
            assertTrue(hasOverlap, alt.getName() + " should share muscle groups with Bench Press");
        }
    }

    @Test
    void findAlternatives_withEmptyEquipment_returnsBodyweightOnly() {
        ExerciseInfo benchPress = ExerciseCatalog.findByName("Bench Press").orElseThrow(
                () -> new RuntimeException("Bench Press not found"));

        List<ExerciseInfo> alternatives = ExerciseCatalog.findAlternatives(
                benchPress.getId(), Collections.emptyList());

        for (ExerciseInfo alt : alternatives) {
            assertTrue(alt.getEquipmentRequired().isEmpty(),
                    alt.getName() + " should not require equipment");
        }
    }
}
