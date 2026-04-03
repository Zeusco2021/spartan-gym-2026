package com.spartangoldengym.reservas.dto;

import javax.validation.constraints.NotNull;
import java.util.UUID;

public class ReserveRequest {

    @NotNull
    private UUID userId;

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
}
