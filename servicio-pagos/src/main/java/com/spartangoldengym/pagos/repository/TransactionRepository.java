package com.spartangoldengym.pagos.repository;

import com.spartangoldengym.pagos.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Transaction> findBySubscriptionIdOrderByCreatedAtDesc(UUID subscriptionId);
}
