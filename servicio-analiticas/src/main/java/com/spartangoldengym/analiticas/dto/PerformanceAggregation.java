package com.spartangoldengym.analiticas.dto;

/**
 * Aggregated performance metrics: heartrate stats and exercise completion rates.
 */
public class PerformanceAggregation {

    private long sampleCount;
    private double heartrateSum;
    private double heartrateMax;
    private double heartrateMin;
    private long exercisesCompleted;
    private long exercisesPlanned;

    public PerformanceAggregation() {
        this.heartrateMin = Double.MAX_VALUE;
        this.heartrateMax = Double.MIN_VALUE;
    }

    public long getSampleCount() { return sampleCount; }
    public void setSampleCount(long sampleCount) { this.sampleCount = sampleCount; }
    public double getHeartrateSum() { return heartrateSum; }
    public void setHeartrateSum(double heartrateSum) { this.heartrateSum = heartrateSum; }
    public double getHeartrateMax() { return heartrateMax; }
    public void setHeartrateMax(double heartrateMax) { this.heartrateMax = heartrateMax; }
    public double getHeartrateMin() { return heartrateMin; }
    public void setHeartrateMin(double heartrateMin) { this.heartrateMin = heartrateMin; }
    public long getExercisesCompleted() { return exercisesCompleted; }
    public void setExercisesCompleted(long exercisesCompleted) { this.exercisesCompleted = exercisesCompleted; }
    public long getExercisesPlanned() { return exercisesPlanned; }
    public void setExercisesPlanned(long exercisesPlanned) { this.exercisesPlanned = exercisesPlanned; }

    public double getAverageHeartrate() {
        return sampleCount > 0 ? heartrateSum / sampleCount : 0.0;
    }

    public double getCompletionRate() {
        return exercisesPlanned > 0 ? (double) exercisesCompleted / exercisesPlanned : 0.0;
    }

    public PerformanceAggregation addHeartrate(double bpm) {
        this.sampleCount++;
        this.heartrateSum += bpm;
        if (bpm > this.heartrateMax) this.heartrateMax = bpm;
        if (bpm < this.heartrateMin) this.heartrateMin = bpm;
        return this;
    }

    public PerformanceAggregation addExerciseCompletion(long completed, long planned) {
        this.exercisesCompleted += completed;
        this.exercisesPlanned += planned;
        return this;
    }
}
