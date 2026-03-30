package com.spartangoldengym.pagos.controller;

import com.spartangoldengym.pagos.dto.*;
import com.spartangoldengym.pagos.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final SubscriptionService subscriptionService;
    private final TransactionService transactionService;
    private final PaymentMethodService paymentMethodService;
    private final RefundService refundService;
    private final DonationService donationService;

    public PaymentController(SubscriptionService subscriptionService,
                             TransactionService transactionService,
                             PaymentMethodService paymentMethodService,
                             RefundService refundService,
                             DonationService donationService) {
        this.subscriptionService = subscriptionService;
        this.transactionService = transactionService;
        this.paymentMethodService = paymentMethodService;
        this.refundService = refundService;
        this.donationService = donationService;
    }

    // --- Subscriptions ---

    @PostMapping("/subscribe")
    public ResponseEntity<SubscriptionResponse> subscribe(
            @Valid @RequestBody SubscribeRequest request) {
        SubscriptionResponse response = subscriptionService.subscribe(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // --- Transactions ---

    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionResponse>> getTransactions(
            @RequestParam UUID userId) {
        List<TransactionResponse> transactions = transactionService.getUserTransactions(userId);
        return ResponseEntity.ok(transactions);
    }

    // --- Payment Methods ---

    @GetMapping("/methods")
    public ResponseEntity<List<PaymentMethodResponse>> getPaymentMethods(
            @RequestParam UUID userId) {
        List<PaymentMethodResponse> methods = paymentMethodService.getUserPaymentMethods(userId);
        return ResponseEntity.ok(methods);
    }

    @PostMapping("/methods")
    public ResponseEntity<PaymentMethodResponse> addPaymentMethod(
            @Valid @RequestBody PaymentMethodRequest request) {
        PaymentMethodResponse response = paymentMethodService.addPaymentMethod(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/methods/{methodId}")
    public ResponseEntity<Void> deletePaymentMethod(@PathVariable UUID methodId) {
        paymentMethodService.deletePaymentMethod(methodId);
        return ResponseEntity.noContent().build();
    }

    // --- Refunds ---

    @PostMapping("/refund")
    public ResponseEntity<TransactionResponse> processRefund(
            @Valid @RequestBody RefundRequest request) {
        TransactionResponse response = refundService.processRefund(request);
        return ResponseEntity.ok(response);
    }

    // --- Donations ---

    @PostMapping("/donations")
    public ResponseEntity<DonationResponse> processDonation(
            @Valid @RequestBody DonationRequest request) {
        DonationResponse response = donationService.processDonation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
