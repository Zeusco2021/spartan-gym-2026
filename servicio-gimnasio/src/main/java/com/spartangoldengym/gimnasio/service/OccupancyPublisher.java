package com.spartangoldengym.gimnasio.service;

import com.spartangoldengym.common.config.KafkaTopics;
import com.spartangoldengym.gimnasio.entity.Gym;
import com.spartangoldengym.gimnasio.repository.GymRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OccupancyPublisher {

    private static final Logger log = LoggerFactory.getLogger(OccupancyPublisher.class);

    private final GymRepository gymRepository;
    private final CheckinService checkinService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OccupancyPublisher(GymRepository gymRepository,
                              CheckinService checkinService,
                              KafkaTemplate<String, String> kafkaTemplate) {
        this.gymRepository = gymRepository;
        this.checkinService = checkinService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedRate = 60000)
    public void publishOccupancy() {
        List<Gym> gyms = gymRepository.findAll();
        for (Gym gym : gyms) {
            int occupancy = checkinService.getCurrentOccupancy(gym.getId());
            if (occupancy > 0) {
                String payload = String.format(
                        "{\"gymId\":\"%s\",\"currentOccupancy\":%d,\"maxCapacity\":%d}",
                        gym.getId(), occupancy, gym.getMaxCapacity()
                );
                kafkaTemplate.send(KafkaTopics.GYM_OCCUPANCY, gym.getId().toString(), payload);
                log.debug("Published occupancy for gym {}: {}", gym.getId(), occupancy);
            }
        }
    }
}
