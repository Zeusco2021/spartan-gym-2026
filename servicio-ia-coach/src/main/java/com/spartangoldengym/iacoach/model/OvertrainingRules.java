package com.spartangoldengym.iacoach.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Rules engine for overtraining detection based on biometric data.
 * Analyzes resting heart rate, heart rate variability, sleep quality,
 * and performance changes to determine overtraining risk.
 *
 * Req 3.3, 18.3
 */
public final class OvertrainingRules {

    private OvertrainingRules() {
    }

    // Thresholds for overtraining indicators
    public static final double RESTING_HR_HIGH_THRESHOLD = 75.0;
    public static final double HRV_LOW_THRESHOLD = 30.0;
    public static final double SLEEP_QUALITY_LOW_THRESHOLD = 50.0;
    public static final double PERFORMANCE_DECLINE_THRESHOLD = -10.0;

    // Risk thresholds
    public static final double HIGH_RISK_THRESHOLD = 0.4;
    public static final double MODERATE_RISK_THRESHOLD = 0.2;

    /**
     * Analyze biometric data and compute overtraining risk score (0.0 to 1.0).
     * Each indicator contributes to the overall risk.
     */
    public static double computeRiskScore(Double restingHeartRate,
                                          Double heartRateVariability,
                                          Double sleepQualityScore,
                                          Double performanceChange) {
        double score = 0.0;
        int indicators = 0;

        if (restingHeartRate != null) {
            indicators++;
            if (restingHeartRate > RESTING_HR_HIGH_THRESHOLD) {
                score += normalize(restingHeartRate, RESTING_HR_HIGH_THRESHOLD, 100.0);
            }
        }

        if (heartRateVariability != null) {
            indicators++;
            if (heartRateVariability < HRV_LOW_THRESHOLD) {
                score += normalize(HRV_LOW_THRESHOLD - heartRateVariability, 0.0, HRV_LOW_THRESHOLD);
            }
        }

        if (sleepQualityScore != null) {
            indicators++;
            if (sleepQualityScore < SLEEP_QUALITY_LOW_THRESHOLD) {
                score += normalize(SLEEP_QUALITY_LOW_THRESHOLD - sleepQualityScore, 0.0, SLEEP_QUALITY_LOW_THRESHOLD);
            }
        }

        if (performanceChange != null) {
            indicators++;
            if (performanceChange < PERFORMANCE_DECLINE_THRESHOLD) {
                score += normalize(Math.abs(performanceChange), Math.abs(PERFORMANCE_DECLINE_THRESHOLD), 50.0);
            }
        }

        if (indicators == 0) {
            return 0.0;
        }

        return Math.min(1.0, score / indicators);
    }

    /**
     * Detect which overtraining indicators are present.
     */
    public static List<String> detectIndicators(Double restingHeartRate,
                                                Double heartRateVariability,
                                                Double sleepQualityScore,
                                                Double performanceChange) {
        List<String> indicators = new ArrayList<>();

        if (restingHeartRate != null && restingHeartRate > RESTING_HR_HIGH_THRESHOLD) {
            indicators.add("elevated_resting_heart_rate");
        }
        if (heartRateVariability != null && heartRateVariability < HRV_LOW_THRESHOLD) {
            indicators.add("low_heart_rate_variability");
        }
        if (sleepQualityScore != null && sleepQualityScore < SLEEP_QUALITY_LOW_THRESHOLD) {
            indicators.add("poor_sleep_quality");
        }
        if (performanceChange != null && performanceChange < PERFORMANCE_DECLINE_THRESHOLD) {
            indicators.add("performance_decline");
        }

        return indicators;
    }

    /**
     * Determine risk level string from risk score.
     */
    public static String determineRiskLevel(double riskScore) {
        if (riskScore >= HIGH_RISK_THRESHOLD) {
            return "high";
        } else if (riskScore >= MODERATE_RISK_THRESHOLD) {
            return "moderate";
        }
        return "low";
    }

    /**
     * Determine if a rest alert should be generated.
     * Alert is generated when risk is moderate or high.
     */
    public static boolean shouldGenerateRestAlert(double riskScore) {
        return riskScore >= MODERATE_RISK_THRESHOLD;
    }

    /**
     * Suggest number of rest days based on risk score.
     */
    public static int suggestRestDays(double riskScore) {
        if (riskScore >= HIGH_RISK_THRESHOLD) {
            return 3;
        } else if (riskScore >= MODERATE_RISK_THRESHOLD) {
            return 1;
        }
        return 0;
    }

    /**
     * Generate recommendation text based on risk level.
     */
    public static String generateRecommendation(String riskLevel) {
        switch (riskLevel) {
            case "high":
                return "High overtraining risk detected. Take 3 rest days and focus on sleep and recovery.";
            case "moderate":
                return "Moderate overtraining risk. Consider a light recovery day with stretching or walking.";
            default:
                return "No overtraining indicators detected. Continue with your training plan.";
        }
    }

    private static double normalize(double value, double min, double max) {
        if (max <= min) return 0.0;
        return Math.max(0.0, Math.min(1.0, (value - min) / (max - min)));
    }
}
