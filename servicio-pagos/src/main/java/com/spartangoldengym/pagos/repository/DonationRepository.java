package com.spartangoldengym.pagos.repository;

import com.spartangoldengym.pagos.entity.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DonationRepository extends JpaRepository<Donation, UUID> {

    List<Donation> findByCreatorIdOrderByCreatedAtDesc(UUID creatorId);

    List<Donation> findByDonorIdOrderByCreatedAtDesc(UUID donorId);
}
