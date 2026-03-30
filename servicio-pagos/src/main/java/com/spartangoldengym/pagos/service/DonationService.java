package com.spartangoldengym.pagos.service;

import com.spartangoldengym.pagos.dto.DonationRequest;
import com.spartangoldengym.pagos.dto.DonationResponse;
import com.spartangoldengym.pagos.entity.Donation;
import com.spartangoldengym.pagos.gateway.PayPalDonationGateway;
import com.spartangoldengym.pagos.gateway.PaymentResult;
import com.spartangoldengym.pagos.repository.DonationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DonationService {

    private static final Logger log = LoggerFactory.getLogger(DonationService.class);
    private static final String PAYMENT_EVENTS_TOPIC = "payment.events";

    private final DonationRepository donationRepository;
    private final PayPalDonationGateway payPalDonationGateway;
    private final AuditService auditService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public DonationService(DonationRepository donationRepository,
                           PayPalDonationGateway payPalDonationGateway,
                           AuditService auditService,
                           KafkaTemplate<String, String> kafkaTemplate) {
        this.donationRepository = donationRepository;
        this.payPalDonationGateway = payPalDonationGateway;
        this.auditService = auditService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public DonationResponse processDonation(DonationRequest request) {
        if (request.getDonorId().equals(request.getCreatorId())) {
            throw new IllegalArgumentException("Donor and creator cannot be the same user");
        }

        PaymentResult result = payPalDonationGateway.capture(
                request.getPaypalToken(), request.getAmount(), request.getCurrency());

        if (!result.isSuccess()) {
            auditService.log(request.getDonorId(), "DONATION_FAILED",
                    "donation", null, result.getErrorMessage());
            throw new RuntimeException("PayPal donation failed: " + result.getErrorMessage());
        }

        Donation donation = new Donation();
        donation.setDonorId(request.getDonorId());
        donation.setCreatorId(request.getCreatorId());
        donation.setAmount(request.getAmount());
        donation.setCurrency(request.getCurrency());
        donation.setMessage(request.getMessage());
        donation.setPaypalTransactionId(result.getExternalTransactionId());
        donation = donationRepository.save(donation);

        auditService.log(request.getDonorId(), "DONATION_PROCESSED",
                "donation", donation.getId().toString(),
                "{\"creatorId\":\"" + request.getCreatorId()
                        + "\",\"amount\":" + request.getAmount()
                        + ",\"currency\":\"" + request.getCurrency() + "\"}");

        // Publish notification event for the notification service to push to the creator
        String notificationEvent = "{\"type\":\"donation_received\""
                + ",\"creatorId\":\"" + request.getCreatorId() + "\""
                + ",\"donorId\":\"" + request.getDonorId() + "\""
                + ",\"amount\":" + request.getAmount()
                + ",\"currency\":\"" + request.getCurrency() + "\""
                + ",\"message\":" + (request.getMessage() != null
                        ? "\"" + request.getMessage().replace("\"", "\\\"") + "\"" : "null")
                + ",\"donationId\":\"" + donation.getId() + "\"}";
        kafkaTemplate.send(PAYMENT_EVENTS_TOPIC, request.getCreatorId().toString(), notificationEvent);

        log.info("Donation processed: donorId={} creatorId={} amount={} currency={}",
                request.getDonorId(), request.getCreatorId(), request.getAmount(), request.getCurrency());

        return toResponse(donation);
    }

    private DonationResponse toResponse(Donation donation) {
        DonationResponse r = new DonationResponse();
        r.setId(donation.getId());
        r.setDonorId(donation.getDonorId());
        r.setCreatorId(donation.getCreatorId());
        r.setAmount(donation.getAmount());
        r.setCurrency(donation.getCurrency());
        r.setMessage(donation.getMessage());
        r.setPaypalTransactionId(donation.getPaypalTransactionId());
        r.setCreatedAt(donation.getCreatedAt());
        return r;
    }
}
