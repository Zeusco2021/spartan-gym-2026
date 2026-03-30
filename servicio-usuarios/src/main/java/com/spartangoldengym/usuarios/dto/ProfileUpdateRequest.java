package com.spartangoldengym.usuarios.dto;

import javax.validation.constraints.Size;

public class ProfileUpdateRequest {

    @Size(max = 255)
    private String name;

    @Size(max = 500)
    private String profilePhotoUrl;

    @Size(max = 10)
    private String locale;

    private String fitnessGoals;

    private String medicalConditions;

    public ProfileUpdateRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getProfilePhotoUrl() { return profilePhotoUrl; }
    public void setProfilePhotoUrl(String profilePhotoUrl) { this.profilePhotoUrl = profilePhotoUrl; }
    public String getLocale() { return locale; }
    public void setLocale(String locale) { this.locale = locale; }
    public String getFitnessGoals() { return fitnessGoals; }
    public void setFitnessGoals(String fitnessGoals) { this.fitnessGoals = fitnessGoals; }
    public String getMedicalConditions() { return medicalConditions; }
    public void setMedicalConditions(String medicalConditions) { this.medicalConditions = medicalConditions; }
}
