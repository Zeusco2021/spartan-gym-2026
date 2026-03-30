package com.spartangoldengym.pagos.dto;

import javax.validation.constraints.NotNull;
import java.util.UUID;

public class RefundRequest {

    @NotNull
    private UUID userId;

    @NotNull
    private UUID transactionId;

    private String reason;

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public UUID getTransactionId() { return transactionId; }
    public void setTransactionId(UUID transactionId) { this.transactionId = transactionId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
