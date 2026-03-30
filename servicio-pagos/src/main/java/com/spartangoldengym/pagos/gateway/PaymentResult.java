package com.spartangoldengym.pagos.gateway;

public class PaymentResult {

    private final boolean success;
    private final String externalTransactionId;
    private final String errorMessage;

    private PaymentResult(boolean success, String externalTransactionId, String errorMessage) {
        this.success = success;
        this.externalTransactionId = externalTransactionId;
        this.errorMessage = errorMessage;
    }

    public static PaymentResult success(String externalTransactionId) {
        return new PaymentResult(true, externalTransactionId, null);
    }

    public static PaymentResult failure(String errorMessage) {
        return new PaymentResult(false, null, errorMessage);
    }

    public boolean isSuccess() { return success; }
    public String getExternalTransactionId() { return externalTransactionId; }
    public String getErrorMessage() { return errorMessage; }
}
