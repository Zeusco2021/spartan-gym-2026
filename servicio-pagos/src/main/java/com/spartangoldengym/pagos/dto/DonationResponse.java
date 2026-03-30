package com.spartangoldengym.pagos.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class DonationResponse {

    private UUID id;
    private UUID donorId;
    private UUID creatorId;
    private BigDecimal amount;
    private String currency;
    private String message;
    private String paypalTransactionId;
    private Instant createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getDonorId() { return donorId; }
    public void setDonorId(UUID donorId) { this.donorId = donorId; }
    public UUID getCreatorId() { return creatorId; }
    public void setCreatorId(UUID creatorId) { this.creatorId = creatorId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getPaypalTransactionId() { return paypalTransactionId; }
    public void setPaypalTransactionId(String paypalTransactionId) { this.paypalTransactionId = paypalTransactionId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
