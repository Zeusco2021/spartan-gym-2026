package com.spartangoldengym.gimnasio.dto;

import java.util.UUID;

public class OccupancyResponse {

    private UUID gymId;
    private int currentOccupancy;
    private int maxCapacity;
    private double occupancyPercentage;

    public OccupancyResponse() {}

    public UUID getGymId() { return gymId; }
    public void setGymId(UUID gymId) { this.gymId = gymId; }
    public int getCurrentOccupancy() { return currentOccupancy; }
    public void setCurrentOccupancy(int currentOccupancy) { this.currentOccupancy = currentOccupancy; }
    public int getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }
    public double getOccupancyPercentage() { return occupancyPercentage; }
    public void setOccupancyPercentage(double occupancyPercentage) { this.occupancyPercentage = occupancyPercentage; }
}
