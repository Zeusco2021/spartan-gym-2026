package com.spartangoldengym.calendario.repository;

import com.spartangoldengym.calendario.entity.CalendarEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface CalendarEventRepository extends JpaRepository<CalendarEvent, UUID> {

    List<CalendarEvent> findByUserIdOrderByStartsAtAsc(UUID userId);

    List<CalendarEvent> findByUserIdAndStartsAtBetweenOrderByStartsAtAsc(
            UUID userId, Instant from, Instant to);

    @Query("SELECT e FROM CalendarEvent e WHERE e.userId = :userId " +
           "AND e.startsAt < :endsAt AND e.endsAt > :startsAt")
    List<CalendarEvent> findOverlapping(@Param("userId") UUID userId,
                                        @Param("startsAt") Instant startsAt,
                                        @Param("endsAt") Instant endsAt);

    @Query("SELECT e FROM CalendarEvent e WHERE e.userId = :userId " +
           "AND e.id <> :excludeId " +
           "AND e.startsAt < :endsAt AND e.endsAt > :startsAt")
    List<CalendarEvent> findOverlappingExcluding(@Param("userId") UUID userId,
                                                  @Param("excludeId") UUID excludeId,
                                                  @Param("startsAt") Instant startsAt,
                                                  @Param("endsAt") Instant endsAt);
}
