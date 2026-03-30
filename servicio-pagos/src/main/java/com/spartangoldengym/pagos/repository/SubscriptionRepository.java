package com.spartangoldengym.pagos.repository;

import com.spartangoldengym.pagos.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    List<Subscription> findByUserId(UUID userId);

    Optional<Subscription> findByUserIdAndStatus(UUID userId, String status);

    List<Subscription> findByStatusAndRetryCountLessThan(String status, int maxRetries);
}
