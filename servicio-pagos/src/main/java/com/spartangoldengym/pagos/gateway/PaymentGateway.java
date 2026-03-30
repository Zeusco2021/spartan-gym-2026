package com.spartangoldengym.pagos.gateway;

import java.math.BigDecimal;

/**
 * Abstraction for payment providers (Stripe, Adyen).
 * PCI DSS: Only tokenized references are passed — no raw card data.
 */
public interface PaymentGateway {

    PaymentResult charge(String paymentToken, BigDecimal amount, String currency);

    PaymentResult refund(String externalTransactionId, BigDecimal amount, String currency);

    String createPaymentMethod(String paymentToken);

    void deletePaymentMethod(String externalMethodId);
}
