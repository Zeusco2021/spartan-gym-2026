package com.spartangoldengym.usuarios.dto;

import java.util.List;

public class OnboardingRequest {

    /** Fitness level: beginner, intermediate, advanced */
    private String fitnessLevel;

    /** Goals: weight_loss, muscle_gain, endurance, flexibility */
    private List<String> goals;

    /** Medical limitations (free text or structured list) */
    private List<String> medicalLimitations;

    /** Desired training frequency per week (1-7) */
    private Integer desiredFrequency;

    /** Whether this is a partial save (true) or final submission (false/null) */
    private Boolean partial;

    /** Current onboarding step for partial progress tracking */
    private Integer currentStep;

    public OnboardingRequest() {}

    public String getFitnessLevel() { return fitnessLevel; }
    public void setFitnessLevel(String fitnessLevel) { this.fitnessLevel = fitnessLevel; }
    public List<String> getGoals() { return goals; }
    public void setGoals(List<String> goals) { this.goals = goals; }
    public List<String> getMedicalLimitations() { return medicalLimitations; }
    public void setMedicalLimitations(List<String> medicalLimitations) { this.medicalLimitations = medicalLimitations; }
    public Integer getDesiredFrequency() { return desiredFrequency; }
    public void setDesiredFrequency(Integer desiredFrequency) { this.desiredFrequency = desiredFrequency; }
    public Boolean getPartial() { return partial; }
    public void setPartial(Boolean partial) { this.partial = partial; }
    public Integer getCurrentStep() { return currentStep; }
    public void setCurrentStep(Integer currentStep) { this.currentStep = currentStep; }
}
