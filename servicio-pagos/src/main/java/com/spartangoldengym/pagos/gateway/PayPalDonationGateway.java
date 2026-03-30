package com.spartangoldengym.pagos.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * PayPal gateway integration for processing donations.
 * Uses PayPal order/capture flow — no raw payment data handled server-side.
 */
@Component
public class PayPalDonationGateway {

    private static final Logger log = LoggerFactory.getLogger(PayPalDonationGateway.class);

    public PaymentResult capture(String paypalToken, BigDecimal amount, String currency) {
        log.info("PayPal capture: token={} amount={} currency={}", paypalToken, amount, currency);
        // In production: call PayPal SDK OrdersCaptureRequest with the token
        String externalId = "paypal_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        return PaymentResult.success(externalId);
    }
}
