package com.spartangoldengym.pagos.service;

import com.spartangoldengym.pagos.dto.TransactionResponse;
import com.spartangoldengym.pagos.entity.Transaction;
import com.spartangoldengym.pagos.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;

    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(transactionRepository);
    }

    @Test
    void getUserTransactions_returnsOrderedTransactions() {
        UUID userId = UUID.randomUUID();
        Transaction tx1 = new Transaction();
        tx1.setId(UUID.randomUUID());
        tx1.setUserId(userId);
        tx1.setAmount(new BigDecimal("19.99"));
        tx1.setCurrency("USD");
        tx1.setType("subscription");
        tx1.setStatus("completed");
        tx1.setPaymentProvider("stripe");
        tx1.setCreatedAt(Instant.now());

        Transaction tx2 = new Transaction();
        tx2.setId(UUID.randomUUID());
        tx2.setUserId(userId);
        tx2.setAmount(new BigDecimal("19.99"));
        tx2.setCurrency("USD");
        tx2.setType("refund");
        tx2.setStatus("completed");
        tx2.setPaymentProvider("stripe");
        tx2.setCreatedAt(Instant.now());

        when(transactionRepository.findByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(Arrays.asList(tx2, tx1));

        List<TransactionResponse> transactions = transactionService.getUserTransactions(userId);

        assertEquals(2, transactions.size());
        assertEquals("refund", transactions.get(0).getType());
        assertEquals("subscription", transactions.get(1).getType());
    }

    @Test
    void getUserTransactions_noTransactions_returnsEmptyList() {
        UUID userId = UUID.randomUUID();
        when(transactionRepository.findByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(Collections.emptyList());

        List<TransactionResponse> transactions = transactionService.getUserTransactions(userId);

        assertTrue(transactions.isEmpty());
    }
}
