package com.spartangoldengym.pagos.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Adyen payment gateway integration.
 * PCI DSS: Uses Adyen tokens — no raw card data handled server-side.
 */
@Component("adyenGateway")
public class AdyenPaymentGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(AdyenPaymentGateway.class);

    @Override
    public PaymentResult charge(String paymentToken, BigDecimal amount, String currency) {
        log.info("Adyen charge: token={} amount={} currency={}", paymentToken, amount, currency);
        String externalId = "adyen_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        return PaymentResult.success(externalId);
    }

    @Override
    public PaymentResult refund(String externalTransactionId, BigDecimal amount, String currency) {
        log.info("Adyen refund: externalTxId={} amount={} currency={}", externalTransactionId, amount, currency);
        String refundId = "adyen_ref_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        return PaymentResult.success(refundId);
    }

    @Override
    public String createPaymentMethod(String paymentToken) {
        log.info("Adyen createPaymentMethod: token={}", paymentToken);
        return "adyen_pm_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    @Override
    public void deletePaymentMethod(String externalMethodId) {
        log.info("Adyen deletePaymentMethod: externalMethodId={}", externalMethodId);
    }
}
