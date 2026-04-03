package com.spartangoldengym.reservas.scheduler;

import com.spartangoldengym.common.config.KafkaTopics;
import com.spartangoldengym.reservas.entity.GroupClass;
import com.spartangoldengym.reservas.repository.GroupClassRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Scheduled job that checks for classes starting within 24 hours
 * that have less than 50% occupancy and publishes an alert event.
 * Requirement 23.9: Alert admin if class has < 50% occupancy 24h before start.
 */
@Component
public class LowOccupancyAlertScheduler {

    private static final Logger log = LoggerFactory.getLogger(LowOccupancyAlertScheduler.class);

    private final GroupClassRepository classRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public LowOccupancyAlertScheduler(GroupClassRepository classRepository,
                                      KafkaTemplate<String, String> kafkaTemplate) {
        this.classRepository = classRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedRate = 3600000) // every hour
    public void checkLowOccupancyClasses() {
        Instant now = Instant.now();
        Instant in24h = now.plus(24, ChronoUnit.HOURS);

        List<GroupClass> lowOccupancy = classRepository
                .findLowOccupancyClassesStartingBetween(now, in24h);

        log.info("Low occupancy alert scheduler: found {} classes with < 50% occupancy within 24h",
                lowOccupancy.size());

        for (GroupClass gc : lowOccupancy) {
            String event = "{\"classId\":\"" + gc.getId()
                    + "\",\"gymId\":\"" + gc.getGymId()
                    + "\",\"event\":\"low_occupancy_alert\""
                    + ",\"className\":\"" + gc.getName()
                    + "\",\"currentCapacity\":" + gc.getCurrentCapacity()
                    + ",\"maxCapacity\":" + gc.getMaxCapacity()
                    + ",\"scheduledAt\":\"" + gc.getScheduledAt()
                    + "\",\"timestamp\":\"" + Instant.now() + "\"}";
            kafkaTemplate.send(KafkaTopics.BOOKINGS_EVENTS, gc.getId().toString(), event);
            log.warn("Low occupancy alert: class={} ({}/{}) scheduled at {}",
                    gc.getName(), gc.getCurrentCapacity(), gc.getMaxCapacity(), gc.getScheduledAt());
        }
    }
}
