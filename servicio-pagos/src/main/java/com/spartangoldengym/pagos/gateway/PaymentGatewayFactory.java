package com.spartangoldengym.pagos.gateway;

import org.springframework.stereotype.Component;

@Component
public class PaymentGatewayFactory {

    private final PaymentGateway stripeGateway;
    private final PaymentGateway adyenGateway;

    public PaymentGatewayFactory(PaymentGateway stripeGateway, PaymentGateway adyenGateway) {
        this.stripeGateway = stripeGateway;
        this.adyenGateway = adyenGateway;
    }

    public PaymentGateway getGateway(String provider) {
        switch (provider.toLowerCase()) {
            case "stripe":
                return stripeGateway;
            case "adyen":
                return adyenGateway;
            default:
                throw new IllegalArgumentException("Unsupported payment provider: " + provider);
        }
    }
}
