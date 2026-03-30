package com.spartangoldengym.iacoach.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OvertrainingRulesTest {

    @Test
    void computeRiskScore_allHighRiskIndicators_returnsHighScore() {
        double score = OvertrainingRules.computeRiskScore(90.0, 15.0, 25.0, -25.0);
        assertTrue(score >= OvertrainingRules.HIGH_RISK_THRESHOLD,
                "Score should be high risk: " + score);
    }

    @Test
    void computeRiskScore_allNormalIndicators_returnsLowScore() {
        double score = OvertrainingRules.computeRiskScore(60.0, 50.0, 80.0, 5.0);
        assertTrue(score < OvertrainingRules.MODERATE_RISK_THRESHOLD,
                "Score should be low risk: " + score);
    }

    @Test
    void computeRiskScore_nullInputs_returnsZero() {
        double score = OvertrainingRules.computeRiskScore(null, null, null, null);
        assertEquals(0.0, score);
    }

    @Test
    void computeRiskScore_partialData_computesFromAvailable() {
        double score = OvertrainingRules.computeRiskScore(90.0, null, null, null);
        assertTrue(score > 0.0, "Score should be > 0 with elevated HR");
    }

    @Test
    void detectIndicators_elevatedHR_detected() {
        List<String> indicators = OvertrainingRules.detectIndicators(90.0, 50.0, 80.0, 5.0);
        assertTrue(indicators.contains("elevated_resting_heart_rate"));
        assertEquals(1, indicators.size());
    }

    @Test
    void detectIndicators_allBad_allDetected() {
        List<String> indicators = OvertrainingRules.detectIndicators(90.0, 15.0, 25.0, -25.0);
        assertEquals(4, indicators.size());
        assertTrue(indicators.contains("elevated_resting_heart_rate"));
        assertTrue(indicators.contains("low_heart_rate_variability"));
        assertTrue(indicators.contains("poor_sleep_quality"));
        assertTrue(indicators.contains("performance_decline"));
    }

    @Test
    void detectIndicators_allNormal_noneDetected() {
        List<String> indicators = OvertrainingRules.detectIndicators(60.0, 50.0, 80.0, 5.0);
        assertTrue(indicators.isEmpty());
    }

    @Test
    void determineRiskLevel_highScore_returnsHigh() {
        assertEquals("high", OvertrainingRules.determineRiskLevel(0.5));
    }

    @Test
    void determineRiskLevel_moderateScore_returnsModerate() {
        assertEquals("moderate", OvertrainingRules.determineRiskLevel(0.3));
    }

    @Test
    void determineRiskLevel_lowScore_returnsLow() {
        assertEquals("low", OvertrainingRules.determineRiskLevel(0.1));
    }

    @Test
    void shouldGenerateRestAlert_moderateRisk_true() {
        assertTrue(OvertrainingRules.shouldGenerateRestAlert(0.3));
    }

    @Test
    void shouldGenerateRestAlert_lowRisk_false() {
        assertFalse(OvertrainingRules.shouldGenerateRestAlert(0.1));
    }

    @Test
    void suggestRestDays_highRisk_returns3() {
        assertEquals(3, OvertrainingRules.suggestRestDays(0.5));
    }

    @Test
    void suggestRestDays_moderateRisk_returns1() {
        assertEquals(1, OvertrainingRules.suggestRestDays(0.3));
    }

    @Test
    void suggestRestDays_lowRisk_returns0() {
        assertEquals(0, OvertrainingRules.suggestRestDays(0.1));
    }

    @Test
    void generateRecommendation_highRisk_mentionsRestDays() {
        String rec = OvertrainingRules.generateRecommendation("high");
        assertTrue(rec.contains("rest days"));
    }

    @Test
    void generateRecommendation_lowRisk_mentionsContinue() {
        String rec = OvertrainingRules.generateRecommendation("low");
        assertTrue(rec.contains("Continue"));
    }
}
