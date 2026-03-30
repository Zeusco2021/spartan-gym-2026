package com.spartangoldengym.pagos.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Stripe payment gateway integration.
 * PCI DSS: Uses Stripe tokens — no raw card data handled server-side.
 */
@Component("stripeGateway")
public class StripePaymentGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(StripePaymentGateway.class);

    @Override
    public PaymentResult charge(String paymentToken, BigDecimal amount, String currency) {
        log.info("Stripe charge: token={} amount={} currency={}", paymentToken, amount, currency);
        // In production: call Stripe SDK PaymentIntent.create(...)
        String externalId = "stripe_ch_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        return PaymentResult.success(externalId);
    }

    @Override
    public PaymentResult refund(String externalTransactionId, BigDecimal amount, String currency) {
        log.info("Stripe refund: externalTxId={} amount={} currency={}", externalTransactionId, amount, currency);
        // In production: call Stripe SDK Refund.create(...)
        String refundId = "stripe_re_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        return PaymentResult.success(refundId);
    }

    @Override
    public String createPaymentMethod(String paymentToken) {
        log.info("Stripe createPaymentMethod: token={}", paymentToken);
        return "stripe_pm_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    @Override
    public void deletePaymentMethod(String externalMethodId) {
        log.info("Stripe deletePaymentMethod: externalMethodId={}", externalMethodId);
    }
}
