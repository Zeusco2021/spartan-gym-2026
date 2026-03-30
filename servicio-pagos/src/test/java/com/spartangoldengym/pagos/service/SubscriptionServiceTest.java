package com.spartangoldengym.pagos.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class SubscriptionServiceTest {

    @Mock private SubscriptionRepository subscriptionRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private PaymentGatewayFactory paymentGatewayFactory;
    @Mock private PlanPricingConfig planPricingConfig;
    @Mock private AuditService auditService;
    @Mock private KafkaTemplate<String, String> kafkaTemplate;
    @Mock private PaymentGateway paymentGateway;

    private SubscriptionService subscriptionService;

    @BeforeEach
    void setUp() {
        subscriptionService = new SubscriptionService(
                subscriptionRepository, transactionRepository,
                paymentGatewayFactory, planPricingConfig,
                auditService, kafkaTemplate);
    }

    @Test
    void subscribe_successfulPayment_createsActiveSubscription() {
        SubscribeRequest request = new SubscribeRequest();
        request.setUserId(UUID.randomUUID());
        request.setPlanType("premium");
        request.setPaymentProvider("stripe");
        request.setPaymentToken("tok_test");
        request.setCurrency("USD");

        when(planPricingConfig.getPrice("premium")).thenReturn(new BigDecimal("19.99"));
        when(planPricingConfig.getDurationDays("premium")).thenReturn(30);
        when(paymentGatewayFactory.getGateway("stripe")).thenReturn(paymentGateway);
        when(paymentGateway.charge("tok_test", new BigDecimal("19.99"), "USD"))
                .thenReturn(PaymentResult.success("stripe_ch_123"));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(inv -> {
            Subscription s = inv.getArgument(0);
            if (s.getId() == null) s.setId(UUID.randomUUID());
            return s;
        });
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            if (t.getId() == null) t.setId(UUID.randomUUID());
            return t;
        });

        SubscriptionResponse response = subscriptionService.subscribe(request);

        assertNotNull(response.getId());
        assertEquals("active", response.getStatus());
        assertEquals("premium", response.getPlanType());
        assertEquals(request.getUserId(), response.getUserId());
        assertNotNull(response.getStartedAt());
        assertNotNull(response.getExpiresAt());

        verify(transactionRepository).save(argThat(tx ->
                "completed".equals(tx.getStatus()) && "subscription".equals(tx.getType())));
        verify(auditService).log(eq(request.getUserId()), eq("SUBSCRIPTION_CREATED"),
                eq("subscription"), anyString(), anyString());
    }

    @Test
    void subscribe_failedPayment_throwsException() {
        SubscribeRequest request = new SubscribeRequest();
        request.setUserId(UUID.randomUUID());
        request.setPlanType("basic");
        request.setPaymentProvider("stripe");
        request.setPaymentToken("tok_fail");
        request.setCurrency("USD");

        when(planPricingConfig.getPrice("basic")).thenReturn(new BigDecimal("9.99"));
        when(paymentGatewayFactory.getGateway("stripe")).thenReturn(paymentGateway);
        when(paymentGateway.charge("tok_fail", new BigDecimal("9.99"), "USD"))
                .thenReturn(PaymentResult.failure("Card declined"));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            if (t.getId() == null) t.setId(UUID.randomUUID());
            return t;
        });

        assertThrows(RuntimeException.class, () -> subscriptionService.subscribe(request));

        verify(transactionRepository).save(argThat(tx -> "failed".equals(tx.getStatus())));
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void retryFailedPayment_successfulRetry_reactivatesSubscription() {
        UUID subId = UUID.randomUUID();
        Subscription subscription = new Subscription();
        subscription.setId(subId);
        subscription.setUserId(UUID.randomUUID());
        subscription.setPlanType("premium");
        subscription.setPaymentProvider("stripe");
        subscription.setStatus("payment_failed");
        subscription.setRetryCount(1);

        when(subscriptionRepository.findById(subId)).thenReturn(Optional.of(subscription));
        when(planPricingConfig.getPrice("premium")).thenReturn(new BigDecimal("19.99"));
        when(planPricingConfig.getDurationDays("premium")).thenReturn(30);
        when(paymentGatewayFactory.getGateway("stripe")).thenReturn(paymentGateway);
        when(paymentGateway.charge("tok_retry", new BigDecimal("19.99"), "USD"))
                .thenReturn(PaymentResult.success("stripe_ch_retry"));
        when(subscriptionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any())).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            if (t.getId() == null) t.setId(UUID.randomUUID());
            return t;
        });

        subscriptionService.retryFailedPayment(subId, "tok_retry");

        ArgumentCaptor<Subscription> captor = ArgumentCaptor.forClass(Subscription.class);
        verify(subscriptionRepository).save(captor.capture());
        assertEquals("active", captor.getValue().getStatus());
        assertEquals(0, captor.getValue().getRetryCount());
    }

    @Test
    void retryFailedPayment_thirdFailure_suspendsSubscription() {
        UUID subId = UUID.randomUUID();
        Subscription subscription = new Subscription();
        subscription.setId(subId);
        subscription.setUserId(UUID.randomUUID());
        subscription.setPlanType("basic");
        subscription.setPaymentProvider("adyen");
        subscription.setStatus("payment_failed");
        subscription.setRetryCount(2); // This will be the 3rd attempt

        when(subscriptionRepository.findById(subId)).thenReturn(Optional.of(subscription));
        when(planPricingConfig.getPrice("basic")).thenReturn(new BigDecimal("9.99"));
        when(paymentGatewayFactory.getGateway("adyen")).thenReturn(paymentGateway);
        when(paymentGateway.charge("tok_fail", new BigDecimal("9.99"), "USD"))
                .thenReturn(PaymentResult.failure("Insufficient funds"));
        when(subscriptionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any())).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            if (t.getId() == null) t.setId(UUID.randomUUID());
            return t;
        });

        subscriptionService.retryFailedPayment(subId, "tok_fail");

        ArgumentCaptor<Subscription> captor = ArgumentCaptor.forClass(Subscription.class);
        verify(subscriptionRepository, atLeastOnce()).save(captor.capture());
        Subscription saved = captor.getAllValues().get(captor.getAllValues().size() - 1);
        assertEquals("suspended", saved.getStatus());
    }

    @Test
    void retryFailedPayment_maxRetriesAlreadyReached_suspendsImmediately() {
        UUID subId = UUID.randomUUID();
        Subscription subscription = new Subscription();
        subscription.setId(subId);
        subscription.setUserId(UUID.randomUUID());
        subscription.setPlanType("basic");
        subscription.setPaymentProvider("stripe");
        subscription.setStatus("payment_failed");
        subscription.setRetryCount(3);

        when(subscriptionRepository.findById(subId)).thenReturn(Optional.of(subscription));
        when(subscriptionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        subscriptionService.retryFailedPayment(subId, "tok_any");

        ArgumentCaptor<Subscription> captor = ArgumentCaptor.forClass(Subscription.class);
        verify(subscriptionRepository).save(captor.capture());
        assertEquals("suspended", captor.getValue().getStatus());
        verify(paymentGatewayFactory, never()).getGateway(anyString());
    }
}
