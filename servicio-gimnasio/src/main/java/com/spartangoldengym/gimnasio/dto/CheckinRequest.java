package com.spartangoldengym.gimnasio.dto;

import javax.validation.constraints.NotNull;
import java.util.UUID;

public class CheckinRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    private String qrCode;

    public CheckinRequest() {}

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }
}
