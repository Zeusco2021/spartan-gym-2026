package com.spartangoldengym.pagos.service;

import com.spartangoldengym.pagos.dto.TransactionResponse;
import com.spartangoldengym.pagos.entity.Transaction;
import com.spartangoldengym.pagos.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getUserTransactions(UUID userId) {
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    TransactionResponse toResponse(Transaction tx) {
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
