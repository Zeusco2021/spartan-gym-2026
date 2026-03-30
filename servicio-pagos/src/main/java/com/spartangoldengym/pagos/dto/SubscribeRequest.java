package com.spartangoldengym.pagos.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.UUID;

public class SubscribeRequest {

    @NotNull
    private UUID userId;

    @NotBlank
    private String planType;

    @NotBlank
    private String paymentProvider; // "stripe" or "adyen"

    @NotBlank
    private String paymentToken; // tokenized card data (PCI DSS compliant)

    @NotBlank
    private String currency;

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getPlanType() { return planType; }
    public void setPlanType(String planType) { this.planType = planType; }
    public String getPaymentProvider() { return paymentProvider; }
    public void setPaymentProvider(String paymentProvider) { this.paymentProvider = paymentProvider; }
    public String getPaymentToken() { return paymentToken; }
    public void setPaymentToken(String paymentToken) { this.paymentToken = paymentToken; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
