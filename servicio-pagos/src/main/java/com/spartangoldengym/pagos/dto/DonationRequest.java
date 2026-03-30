package com.spartangoldengym.pagos.dto;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public class DonationRequest {

    @NotNull
    private UUID donorId;

    @NotNull
    private UUID creatorId;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    @NotBlank
    private String currency;

    private String message;

    @NotBlank
    private String paypalToken;

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
    public String getPaypalToken() { return paypalToken; }
    public void setPaypalToken(String paypalToken) { this.paypalToken = paypalToken; }
}
