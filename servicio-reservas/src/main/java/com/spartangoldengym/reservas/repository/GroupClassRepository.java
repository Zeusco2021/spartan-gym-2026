package com.spartangoldengym.reservas.repository;

import com.spartangoldengym.reservas.entity.GroupClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface GroupClassRepository extends JpaRepository<GroupClass, UUID>, JpaSpecificationExecutor<GroupClass> {

    @Query("SELECT gc FROM GroupClass gc WHERE gc.scheduledAt BETWEEN :from AND :to " +
           "AND gc.currentCapacity < (gc.maxCapacity / 2)")
    List<GroupClass> findLowOccupancyClassesStartingBetween(
            @Param("from") Instant from, @Param("to") Instant to);
}
