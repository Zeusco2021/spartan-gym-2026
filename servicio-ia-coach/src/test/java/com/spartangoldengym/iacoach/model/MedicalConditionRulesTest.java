package com.spartangoldengym.iacoach.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MedicalConditionRulesTest {

    @Test
    void backProblems_excludesHeavySpinalExercises() {
        Set<String> contraindicated = MedicalConditionRules.getContraindicatedExercises("back_problems");
        assertTrue(contraindicated.contains("Barbell Squat"));
        assertTrue(contraindicated.contains("Romanian Deadlift"));
    }

    @Test
    void thrombosis_excludesHighPressureExercises() {
        Set<String> contraindicated = MedicalConditionRules.getContraindicatedExercises("thrombosis");
        assertTrue(contraindicated.contains("Barbell Squat"));
        assertTrue(contraindicated.contains("Wall Sit"));
    }

    @Test
    void unknownCondition_returnsEmptySet() {
        Set<String> contraindicated = MedicalConditionRules.getContraindicatedExercises("unknown");
        assertTrue(contraindicated.isEmpty());
    }

    @Test
    void multipleConditions_combinesContraindications() {
        Set<String> contraindicated = MedicalConditionRules.getAllContraindicatedExercises(
                Arrays.asList("back_problems", "knee_problems"));
        assertTrue(contraindicated.contains("Barbell Squat")); // both
        assertTrue(contraindicated.contains("Romanian Deadlift")); // back
        assertTrue(contraindicated.contains("Lunges")); // knee
    }

    @Test
    void isContraindicated_correctlyIdentifies() {
        assertTrue(MedicalConditionRules.isContraindicated("Barbell Squat",
                Arrays.asList("back_problems")));
        assertFalse(MedicalConditionRules.isContraindicated("Push-ups",
                Arrays.asList("back_problems")));
    }

    @Test
    void nullConditions_returnsEmptySet() {
        Set<String> contraindicated = MedicalConditionRules.getAllContraindicatedExercises(null);
        assertTrue(contraindicated.isEmpty());
    }

    @Test
    void emptyConditions_returnsEmptySet() {
        Set<String> contraindicated = MedicalConditionRules.getAllContraindicatedExercises(
                Collections.emptyList());
        assertTrue(contraindicated.isEmpty());
    }
}
