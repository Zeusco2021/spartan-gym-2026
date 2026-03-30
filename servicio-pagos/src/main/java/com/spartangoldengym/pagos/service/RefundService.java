package com.spartangoldengym.pagos.service;

import com.spartangoldengym.common.exception.ResourceNotFoundException;
import com.spartangoldengym.pagos.config.PlanPricingConfig;
import com.spartangoldengym.pagos.dto.RefundRequest;
import com.spartangoldengym.pagos.dto.TransactionResponse;
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

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class RefundService {

    private static final Logger log = LoggerFactory.getLogger(RefundService.class);
    private static final String PAYMENT_EVENTS_TOPIC = "payment.events";

    private final TransactionRepository transactionRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentGatewayFactory paymentGatewayFactory;
    private final PlanPricingConfig planPricingConfig;
    private final AuditService auditService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public RefundService(TransactionRepository transactionRepository,
                         SubscriptionRepository subscriptionRepository,
                         PaymentGatewayFactory paymentGatewayFactory,
                         PlanPricingConfig planPricingConfig,
                         AuditService auditService,
                         KafkaTemplate<String, String> kafkaTemplate) {
        this.transactionRepository = transactionRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.paymentGatewayFactory = paymentGatewayFactory;
        this.planPricingConfig = planPricingConfig;
        this.auditService = auditService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public TransactionResponse processRefund(RefundRequest request) {
        Transaction originalTx = transactionRepository.findById(request.getTransactionId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", request.getTransactionId().toString()));

        if (!"completed".equals(originalTx.getStatus())) {
            throw new IllegalStateException("Cannot refund a transaction with status: " + originalTx.getStatus());
        }

        int guaranteeDays = planPricingConfig.getRefundGuaranteeDays();
        Instant refundDeadline = originalTx.getCreatedAt().plus(guaranteeDays, ChronoUnit.DAYS);
        if (Instant.now().isAfter(refundDeadline)) {
            throw new IllegalStateException("Refund period expired. Refunds must be requested within "
                    + guaranteeDays + " days of the original transaction.");
        }

        PaymentGateway gateway = paymentGatewayFactory.getGateway(originalTx.getPaymentProvider());
        PaymentResult result = gateway.refund(originalTx.getExternalTransactionId(),
                originalTx.getAmount(), originalTx.getCurrency());

        if (!result.isSuccess()) {
            auditService.log(request.getUserId(), "REFUND_FAILED",
                    "transaction", request.getTransactionId().toString(), result.getErrorMessage());
            throw new RuntimeException("Refund failed: " + result.getErrorMessage());
        }

        originalTx.setStatus("refunded");
        transactionRepository.save(originalTx);

        Transaction refundTx = new Transaction();
        refundTx.setUserId(request.getUserId());
        refundTx.setSubscriptionId(originalTx.getSubscriptionId());
        refundTx.setAmount(originalTx.getAmount());
        refundTx.setCurrency(originalTx.getCurrency());
        refundTx.setType("refund");
        refundTx.setStatus("completed");
        refundTx.setPaymentProvider(originalTx.getPaymentProvider());
        refundTx.setExternalTransactionId(result.getExternalTransactionId());
        refundTx = transactionRepository.save(refundTx);

        if (originalTx.getSubscriptionId() != null) {
            subscriptionRepository.findById(originalTx.getSubscriptionId()).ifPresent(sub -> {
                sub.setStatus("cancelled");
                subscriptionRepository.save(sub);
            });
        }

        auditService.log(request.getUserId(), "REFUND_PROCESSED",
                "transaction", refundTx.getId().toString(),
                "{\"originalTxId\":\"" + request.getTransactionId() + "\",\"amount\":" + originalTx.getAmount() + "}");

        String event = "{\"userId\":\"" + request.getUserId() + "\",\"event\":\"refund_processed\""
                + ",\"transactionId\":\"" + refundTx.getId() + "\"}";
        kafkaTemplate.send(PAYMENT_EVENTS_TOPIC, request.getUserId().toString(), event);

        log.info("Refund processed: userId={} originalTxId={} refundTxId={}",
                request.getUserId(), request.getTransactionId(), refundTx.getId());
        return toResponse(refundTx);
    }

    private TransactionResponse toResponse(Transaction tx) {
        TransactionResponse r = new TransactionResponse();
        r.setId(tx.getId());
        r.setUserId(tx.getUserId());
        r.setSubscriptionId(tx.getSubscriptionId());
        r.setAmount(tx.getAmount());
        r.setCurrency(tx.getCurrency());
        r.setType(tx.getType());
        r.setStatus(tx.getStatus());
        r.setPaymentProvider(tx.getPaymentProvider());
        r.setExternalTransactionId(tx.getExternalTransactionId());
        r.setCreatedAt(tx.getCreatedAt());
        return r;
    }
}
