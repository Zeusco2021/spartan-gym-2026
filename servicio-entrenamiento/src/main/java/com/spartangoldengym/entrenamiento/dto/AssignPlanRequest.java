package com.spartangoldengym.entrenamiento.dto;

import javax.validation.constraints.NotNull;
import java.util.UUID;

public class AssignPlanRequest {

    @NotNull
    private UUID clientId;

    @NotNull
    private UUID trainerId;

    public UUID getClientId() { return clientId; }
    public void setClientId(UUID clientId) { this.clientId = clientId; }
    public UUID getTrainerId() { return trainerId; }
    public void setTrainerId(UUID trainerId) { this.trainerId = trainerId; }
}
