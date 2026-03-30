package com.spartangoldengym.pagos.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefundServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private SubscriptionRepository subscriptionRepository;
    @Mock private PaymentGatewayFactory paymentGatewayFactory;
    @Mock private PlanPricingConfig planPricingConfig;
    @Mock private AuditService auditService;
    @Mock private KafkaTemplate<String, String> kafkaTemplate;
    @Mock private PaymentGateway paymentGateway;

    private RefundService refundService;

    @BeforeEach
    void setUp() {
        refundService = new RefundService(
                transactionRepository, subscriptionRepository,
                paymentGatewayFactory, planPricingConfig,
                auditService, kafkaTemplate);
    }

    @Test
    void processRefund_withinGuaranteePeriod_succeeds() {
        UUID userId = UUID.randomUUID();
        UUID txId = UUID.randomUUID();
        UUID subId = UUID.randomUUID();

        Transaction originalTx = new Transaction();
        originalTx.setId(txId);
        originalTx.setUserId(userId);
        originalTx.setSubscriptionId(subId);
        originalTx.setAmount(new BigDecimal("19.99"));
        originalTx.setCurrency("USD");
        originalTx.setStatus("completed");
        originalTx.setPaymentProvider("stripe");
        originalTx.setExternalTransactionId("stripe_ch_123");
        originalTx.setCreatedAt(Instant.now().minus(2, ChronoUnit.DAYS));

        RefundRequest request = new RefundRequest();
        request.setUserId(userId);
        request.setTransactionId(txId);
        request.setReason("Not satisfied");

        when(transactionRepository.findById(txId)).thenReturn(Optional.of(originalTx));
        when(planPricingConfig.getRefundGuaranteeDays()).thenReturn(7);
        when(paymentGatewayFactory.getGateway("stripe")).thenReturn(paymentGateway);
        when(paymentGateway.refund("stripe_ch_123", new BigDecimal("19.99"), "USD"))
                .thenReturn(PaymentResult.success("stripe_re_456"));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            if (t.getId() == null) t.setId(UUID.randomUUID());
            return t;
        });

        Subscription sub = new Subscription();
        sub.setId(subId);
        sub.setStatus("active");
        when(subscriptionRepository.findById(subId)).thenReturn(Optional.of(sub));
        when(subscriptionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TransactionResponse response = refundService.processRefund(request);

        assertEquals("refund", response.getType());
        assertEquals("completed", response.getStatus());
        assertEquals(new BigDecimal("19.99"), response.getAmount());

        verify(subscriptionRepository).save(argThat(s -> "cancelled".equals(s.getStatus())));
    }

    @Test
    void processRefund_outsideGuaranteePeriod_throwsException() {
        UUID txId = UUID.randomUUID();

        Transaction originalTx = new Transaction();
        originalTx.setId(txId);
        originalTx.setStatus("completed");
        originalTx.setCreatedAt(Instant.now().minus(10, ChronoUnit.DAYS));

        RefundRequest request = new RefundRequest();
        request.setUserId(UUID.randomUUID());
        request.setTransactionId(txId);

        when(transactionRepository.findById(txId)).thenReturn(Optional.of(originalTx));
        when(planPricingConfig.getRefundGuaranteeDays()).thenReturn(7);

        assertThrows(IllegalStateException.class, () -> refundService.processRefund(request));
    }

    @Test
    void processRefund_nonCompletedTransaction_throwsException() {
        UUID txId = UUID.randomUUID();

        Transaction originalTx = new Transaction();
        originalTx.setId(txId);
        originalTx.setStatus("failed");

        RefundRequest request = new RefundRequest();
        request.setUserId(UUID.randomUUID());
        request.setTransactionId(txId);

        when(transactionRepository.findById(txId)).thenReturn(Optional.of(originalTx));

        assertThrows(IllegalStateException.class, () -> refundService.processRefund(request));
    }
}
