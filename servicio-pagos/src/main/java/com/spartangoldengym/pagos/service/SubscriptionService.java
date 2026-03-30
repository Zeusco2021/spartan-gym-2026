package com.spartangoldengym.pagos.service;

import com.spartangoldengym.common.exception.ResourceNotFoundException;
import com.spartangoldengym.pagos.config.PlanPricingConfig;
import com.spartangoldengym.pagos.dto.SubscribeRequest;
import com.spartangoldengym.pagos.dto.SubscriptionResponse;
import com.spartangoldengym.pagos.entity.Subscription;
import com.spartangoldengym.pagos.entity.Transaction;
import com.spartangoldengym.pagos.gateway.PaymentGateway;
import com.spartangoldengym.pagos.gateway.PaymentGatewayFactory;
import com.spartangoldengym.pagos.gateway.PaymentResult;
import com.spartangoldengym.pagos.repository.SubscriptionRepository;
import com.spartangoldengym.pagos.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionService.class);
    private static final int MAX_RETRY_COUNT = 3;
    private static final String PAYMENT_EVENTS_TOPIC = "payment.events";

    private final SubscriptionRepository subscriptionRepository;
    private final TransactionRepository transactionRepository;
    private final PaymentGatewayFactory paymentGatewayFactory;
    private final PlanPricingConfig planPricingConfig;
    private final AuditService auditService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public SubscriptionService(SubscriptionRepository subscriptionRepository,
                               TransactionRepository transactionRepository,
                               PaymentGatewayFactory paymentGatewayFactory,
                               PlanPricingConfig planPricingConfig,
                               AuditService auditService,
                               KafkaTemplate<String, String> kafkaTemplate) {
        this.subscriptionRepository = subscriptionRepository;
        this.transactionRepository = transactionRepository;
        this.paymentGatewayFactory = paymentGatewayFactory;
        this.planPricingConfig = planPricingConfig;
        this.auditService = auditService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public SubscriptionResponse subscribe(SubscribeRequest request) {
        BigDecimal amount = planPricingConfig.getPrice(request.getPlanType());
        int durationDays = planPricingConfig.getDurationDays(request.getPlanType());

        PaymentGateway gateway = paymentGatewayFactory.getGateway(request.getPaymentProvider());
        PaymentResult result = gateway.charge(request.getPaymentToken(), amount, request.getCurrency());

        if (!result.isSuccess()) {
            Transaction failedTx = createTransaction(request.getUserId(), null, amount,
                    request.getCurrency(), "subscription", "failed",
                    request.getPaymentProvider(), null);
            transactionRepository.save(failedTx);
            auditService.log(request.getUserId(), "SUBSCRIPTION_PAYMENT_FAILED",
                    "transaction", failedTx.getId().toString(), result.getErrorMessage());
            throw new RuntimeException("Payment failed: " + result.getErrorMessage());
        }

        Instant now = Instant.now();
        Subscription subscription = new Subscription();
        subscription.setUserId(request.getUserId());
        subscription.setPlanType(request.getPlanType());
        subscription.setStatus("active");
        subscription.setPaymentProvider(request.getPaymentProvider());
        subscription.setExternalSubscriptionId(result.getExternalTransactionId());
        subscription.setStartedAt(now);
        subscription.setExpiresAt(now.plus(durationDays, ChronoUnit.DAYS));
        subscription.setRetryCount(0);
        subscription = subscriptionRepository.save(subscription);

        Transaction tx = createTransaction(request.getUserId(), subscription.getId(), amount,
                request.getCurrency(), "subscription", "completed",
                request.getPaymentProvider(), result.getExternalTransactionId());
        transactionRepository.save(tx);

        auditService.log(request.getUserId(), "SUBSCRIPTION_CREATED",
                "subscription", subscription.getId().toString(),
                "{\"planType\":\"" + request.getPlanType() + "\",\"amount\":" + amount + "}");

        publishPaymentEvent(request.getUserId(), "subscription_created", subscription.getId().toString());

        log.info("Subscription created: userId={} plan={} subscriptionId={}",
                request.getUserId(), request.getPlanType(), subscription.getId());
        return toResponse(subscription);
    }

    @Transactional
    public void retryFailedPayment(UUID subscriptionId, String paymentToken) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", subscriptionId.toString()));

        if (subscription.getRetryCount() >= MAX_RETRY_COUNT) {
            suspendSubscription(subscription);
            return;
        }

        BigDecimal amount = planPricingConfig.getPrice(subscription.getPlanType());
        PaymentGateway gateway = paymentGatewayFactory.getGateway(subscription.getPaymentProvider());
        PaymentResult result = gateway.charge(paymentToken, amount, "USD");

        subscription.setRetryCount(subscription.getRetryCount() + 1);

        if (result.isSuccess()) {
            subscription.setStatus("active");
            subscription.setRetryCount(0);
            subscription.setExpiresAt(Instant.now().plus(
                    planPricingConfig.getDurationDays(subscription.getPlanType()), ChronoUnit.DAYS));
            subscriptionRepository.save(subscription);

            Transaction tx = createTransaction(subscription.getUserId(), subscription.getId(),
                    amount, "USD", "retry", "completed",
                    subscription.getPaymentProvider(), result.getExternalTransactionId());
            transactionRepository.save(tx);

            auditService.log(subscription.getUserId(), "PAYMENT_RETRY_SUCCESS",
                    "subscription", subscription.getId().toString(),
                    "{\"retryCount\":" + subscription.getRetryCount() + "}");

            publishPaymentEvent(subscription.getUserId(), "payment_retry_success", subscription.getId().toString());
            log.info("Payment retry succeeded: subscriptionId={} attempt={}", subscriptionId, subscription.getRetryCount());
        } else {
            Transaction failedTx = createTransaction(subscription.getUserId(), subscription.getId(),
                    amount, "USD", "retry", "failed",
                    subscription.getPaymentProvider(), null);
            transactionRepository.save(failedTx);

            auditService.log(subscription.getUserId(), "PAYMENT_RETRY_FAILED",
                    "subscription", subscription.getId().toString(),
                    "{\"retryCount\":" + subscription.getRetryCount() + ",\"error\":\"" + result.getErrorMessage() + "\"}");

            publishPaymentEvent(subscription.getUserId(), "payment_retry_failed", subscription.getId().toString());

            if (subscription.getRetryCount() >= MAX_RETRY_COUNT) {
                suspendSubscription(subscription);
            } else {
                subscriptionRepository.save(subscription);
                log.info("Payment retry failed: subscriptionId={} attempt={}", subscriptionId, subscription.getRetryCount());
            }
        }
    }

    private void suspendSubscription(Subscription subscription) {
        subscription.setStatus("suspended");
        subscriptionRepository.save(subscription);

        auditService.log(subscription.getUserId(), "SUBSCRIPTION_SUSPENDED",
                "subscription", subscription.getId().toString(),
                "{\"reason\":\"max_retries_exceeded\"}");

        publishPaymentEvent(subscription.getUserId(), "subscription_suspended", subscription.getId().toString());
        log.warn("Subscription suspended after {} retries: subscriptionId={} userId={}",
                MAX_RETRY_COUNT, subscription.getId(), subscription.getUserId());
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getUserSubscriptions(UUID userId) {
        return subscriptionRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private Transaction createTransaction(UUID userId, UUID subscriptionId, BigDecimal amount,
                                          String currency, String type, String status,
                                          String paymentProvider, String externalTransactionId) {
        Transaction tx = new Transaction();
        tx.setUserId(userId);
        tx.setSubscriptionId(subscriptionId);
        tx.setAmount(amount);
        tx.setCurrency(currency);
        tx.setType(type);
        tx.setStatus(status);
        tx.setPaymentProvider(paymentProvider);
        tx.setExternalTransactionId(externalTransactionId);
        return tx;
    }

    private void publishPaymentEvent(UUID userId, String eventType, String subscriptionId) {
        String event = "{\"userId\":\"" + userId + "\",\"event\":\"" + eventType
                + "\",\"subscriptionId\":\"" + subscriptionId + "\"}";
        kafkaTemplate.send(PAYMENT_EVENTS_TOPIC, userId.toString(), event);
    }

    SubscriptionResponse toResponse(Subscription s) {
        SubscriptionResponse r = new SubscriptionResponse();
        r.setId(s.getId());
        r.setUserId(s.getUserId());
        r.setPlanType(s.getPlanType());
        r.setStatus(s.getStatus());
        r.setPaymentProvider(s.getPaymentProvider());
        r.setStartedAt(s.getStartedAt());
        r.setExpiresAt(s.getExpiresAt());
        return r;
    }
}
