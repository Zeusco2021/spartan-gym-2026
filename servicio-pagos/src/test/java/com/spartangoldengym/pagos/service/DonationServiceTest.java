package com.spartangoldengym.pagos.service;

import com.spartangoldengym.pagos.dto.DonationRequest;
import com.spartangoldengym.pagos.dto.DonationResponse;
import com.spartangoldengym.pagos.entity.Donation;
import com.spartangoldengym.pagos.gateway.PayPalDonationGateway;
import com.spartangoldengym.pagos.gateway.PaymentResult;
import com.spartangoldengym.pagos.repository.DonationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DonationServiceTest {

    @Mock private DonationRepository donationRepository;
    @Mock private PayPalDonationGateway payPalDonationGateway;
    @Mock private AuditService auditService;
    @Mock private KafkaTemplate<String, String> kafkaTemplate;

    private DonationService donationService;

    @BeforeEach
    void setUp() {
        donationService = new DonationService(
                donationRepository, payPalDonationGateway,
                auditService, kafkaTemplate);
    }

    @Test
    void processDonation_success_returnsDonationAndPublishesEvent() {
        UUID donorId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();

        DonationRequest request = new DonationRequest();
        request.setDonorId(donorId);
        request.setCreatorId(creatorId);
        request.setAmount(new BigDecimal("25.00"));
        request.setCurrency("USD");
        request.setMessage("Great content!");
        request.setPaypalToken("paypal_token_abc");

        when(payPalDonationGateway.capture("paypal_token_abc", new BigDecimal("25.00"), "USD"))
                .thenReturn(PaymentResult.success("paypal_ext_123"));
        when(donationRepository.save(any(Donation.class))).thenAnswer(inv -> {
            Donation d = inv.getArgument(0);
            d.setId(UUID.randomUUID());
            return d;
        });

        DonationResponse response = donationService.processDonation(request);

        assertNotNull(response.getId());
        assertEquals(donorId, response.getDonorId());
        assertEquals(creatorId, response.getCreatorId());
        assertEquals(new BigDecimal("25.00"), response.getAmount());
        assertEquals("USD", response.getCurrency());
        assertEquals("Great content!", response.getMessage());
        assertEquals("paypal_ext_123", response.getPaypalTransactionId());

        verify(donationRepository).save(any(Donation.class));
        verify(auditService).log(eq(donorId), eq("DONATION_PROCESSED"),
                eq("donation"), anyString(), anyString());
        verify(kafkaTemplate).send(eq("payment.events"), eq(creatorId.toString()), contains("donation_received"));
    }

    @Test
    void processDonation_paypalFails_throwsException() {
        UUID donorId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();

        DonationRequest request = new DonationRequest();
        request.setDonorId(donorId);
        request.setCreatorId(creatorId);
        request.setAmount(new BigDecimal("10.00"));
        request.setCurrency("EUR");
        request.setPaypalToken("bad_token");

        when(payPalDonationGateway.capture("bad_token", new BigDecimal("10.00"), "EUR"))
                .thenReturn(PaymentResult.failure("Insufficient funds"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> donationService.processDonation(request));
        assertTrue(ex.getMessage().contains("Insufficient funds"));

        verify(donationRepository, never()).save(any());
        verify(auditService).log(eq(donorId), eq("DONATION_FAILED"),
                eq("donation"), isNull(), eq("Insufficient funds"));
    }

    @Test
    void processDonation_sameUserAsDonorAndCreator_throwsException() {
        UUID sameId = UUID.randomUUID();

        DonationRequest request = new DonationRequest();
        request.setDonorId(sameId);
        request.setCreatorId(sameId);
        request.setAmount(new BigDecimal("5.00"));
        request.setCurrency("USD");
        request.setPaypalToken("token");

        assertThrows(IllegalArgumentException.class,
                () -> donationService.processDonation(request));

        verifyNoInteractions(payPalDonationGateway);
        verifyNoInteractions(donationRepository);
    }

    @Test
    void processDonation_nullMessage_handledGracefully() {
        UUID donorId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();

        DonationRequest request = new DonationRequest();
        request.setDonorId(donorId);
        request.setCreatorId(creatorId);
        request.setAmount(new BigDecimal("15.50"));
        request.setCurrency("USD");
        request.setMessage(null);
        request.setPaypalToken("paypal_token_xyz");

        when(payPalDonationGateway.capture("paypal_token_xyz", new BigDecimal("15.50"), "USD"))
                .thenReturn(PaymentResult.success("paypal_ext_456"));
        when(donationRepository.save(any(Donation.class))).thenAnswer(inv -> {
            Donation d = inv.getArgument(0);
            d.setId(UUID.randomUUID());
            return d;
        });

        DonationResponse response = donationService.processDonation(request);

        assertNull(response.getMessage());
        assertEquals(new BigDecimal("15.50"), response.getAmount());

        ArgumentCaptor<String> eventCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(eq("payment.events"), eq(creatorId.toString()), eventCaptor.capture());
        assertTrue(eventCaptor.getValue().contains("\"message\":null"));
    }
}
