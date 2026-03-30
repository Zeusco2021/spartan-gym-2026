package com.spartangoldengym.usuarios.dto;

import java.util.UUID;

public class OnboardingResponse {

    private UUID userId;
    private Integer currentStep;
    private boolean completed;
    private String fitnessLevel;
    private String goals;
    private String medicalLimitations;
    private Integer desiredFrequency;

    public OnboardingResponse() {}

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public Integer getCurrentStep() { return currentStep; }
    public void setCurrentStep(Integer currentStep) { this.currentStep = currentStep; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public String getFitnessLevel() { return fitnessLevel; }
    public void setFitnessLevel(String fitnessLevel) { this.fitnessLevel = fitnessLevel; }
    public String getGoals() { return goals; }
    public void setGoals(String goals) { this.goals = goals; }
    public String getMedicalLimitations() { return medicalLimitations; }
    public void setMedicalLimitations(String medicalLimitations) { this.medicalLimitations = medicalLimitations; }
    public Integer getDesiredFrequency() { return desiredFrequency; }
    public void setDesiredFrequency(Integer desiredFrequency) { this.desiredFrequency = desiredFrequency; }
}
