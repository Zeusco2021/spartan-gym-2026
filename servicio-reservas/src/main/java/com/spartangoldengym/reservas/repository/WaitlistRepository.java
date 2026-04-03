package com.spartangoldengym.reservas.repository;

import com.spartangoldengym.reservas.entity.Waitlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WaitlistRepository extends JpaRepository<Waitlist, UUID> {

    List<Waitlist> findByClassIdOrderByPositionAsc(UUID classId);

    Optional<Waitlist> findFirstByClassIdOrderByPositionAsc(UUID classId);

    Optional<Waitlist> findByClassIdAndUserId(UUID classId, UUID userId);

    int countByClassId(UUID classId);
}
