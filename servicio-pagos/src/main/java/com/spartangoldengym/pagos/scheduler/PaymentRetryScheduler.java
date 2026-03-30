package com.spartangoldengym.pagos.scheduler;

import com.spartangoldengym.pagos.entity.Subscription;
import com.spartangoldengym.pagos.repository.SubscriptionRepository;
import com.spartangoldengym.pagos.service.SubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Scheduled job that retries failed recurring payments every 24 hours.
 * Requirement 7.3: Retry up to 3 times at 24h intervals, notify on each attempt.
 * Requirement 7.4: Suspend membership after 3 failed retries.
 */
@Component
public class PaymentRetryScheduler {

    private static final Logger log = LoggerFactory.getLogger(PaymentRetryScheduler.class);
    private static final int MAX_RETRIES = 3;

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionService subscriptionService;

    public PaymentRetryScheduler(SubscriptionRepository subscriptionRepository,
                                 SubscriptionService subscriptionService) {
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionService = subscriptionService;
    }

    @Scheduled(fixedRate = 86400000) // 24 hours in milliseconds
    public void retryFailedPayments() {
        List<Subscription> pendingRetries = subscriptionRepository
                .findByStatusAndRetryCountLessThan("payment_failed", MAX_RETRIES);

        log.info("Payment retry scheduler: found {} subscriptions pending retry", pendingRetries.size());

        for (Subscription subscription : pendingRetries) {
            try {
                // In production, the payment token would be retrieved from the stored payment method
                subscriptionService.retryFailedPayment(subscription.getId(), "stored_token");
            } catch (Exception e) {
                log.error("Error retrying payment for subscription {}: {}",
                        subscription.getId(), e.getMessage());
            }
        }
    }
}
