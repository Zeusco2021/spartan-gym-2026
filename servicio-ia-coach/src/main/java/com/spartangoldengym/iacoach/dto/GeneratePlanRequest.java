package com.spartangoldengym.iacoach.dto;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public class GeneratePlanRequest {

    @NotNull
    private UUID userId;

    private String fitnessLevel;
    private List<String> goals;
    private List<String> medicalConditions;
    private List<String> availableEquipment;
    private Integer age;
    private Integer daysPerWeek;
    private boolean noEquipment;

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getFitnessLevel() { return fitnessLevel; }
    public void setFitnessLevel(String fitnessLevel) { this.fitnessLevel = fitnessLevel; }
    public List<String> getGoals() { return goals; }
    public void setGoals(List<String> goals) { this.goals = goals; }
    public List<String> getMedicalConditions() { return medicalConditions; }
    public void setMedicalConditions(List<String> medicalConditions) { this.medicalConditions = medicalConditions; }
    public List<String> getAvailableEquipment() { return availableEquipment; }
    public void setAvailableEquipment(List<String> availableEquipment) { this.availableEquipment = availableEquipment; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public Integer getDaysPerWeek() { return daysPerWeek; }
    public void setDaysPerWeek(Integer daysPerWeek) { this.daysPerWeek = daysPerWeek; }
    public boolean isNoEquipment() { return noEquipment; }
    public void setNoEquipment(boolean noEquipment) { this.noEquipment = noEquipment; }
}
