package com.spartangoldengym.gimnasio.service;

import com.spartangoldengym.common.config.KafkaTopics;
import com.spartangoldengym.gimnasio.entity.Gym;
import com.spartangoldengym.gimnasio.repository.GymRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OccupancyPublisherTest {

    @Mock
    private GymRepository gymRepository;
    @Mock
    private CheckinService checkinService;
    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private OccupancyPublisher occupancyPublisher;

    @BeforeEach
    void setUp() {
        occupancyPublisher = new OccupancyPublisher(gymRepository, checkinService, kafkaTemplate);
    }

    @Test
    void publishOccupancy_sendsMessageForOccupiedGyms() {
        UUID gymId = UUID.randomUUID();
        Gym gym = buildGym(gymId, 100);

        when(gymRepository.findAll()).thenReturn(Collections.singletonList(gym));
        when(checkinService.getCurrentOccupancy(gymId)).thenReturn(30);

        occupancyPublisher.publishOccupancy();

        verify(kafkaTemplate).send(eq(KafkaTopics.GYM_OCCUPANCY), eq(gymId.toString()), contains("\"currentOccupancy\":30"));
    }

    @Test
    void publishOccupancy_skipsGymsWithZeroOccupancy() {
        UUID gymId = UUID.randomUUID();
        Gym gym = buildGym(gymId, 100);

        when(gymRepository.findAll()).thenReturn(Collections.singletonList(gym));
        when(checkinService.getCurrentOccupancy(gymId)).thenReturn(0);

        occupancyPublisher.publishOccupancy();

        verify(kafkaTemplate, never()).send(anyString(), anyString(), anyString());
    }

    @Test
    void publishOccupancy_handlesMultipleGyms() {
        UUID gymId1 = UUID.randomUUID();
        UUID gymId2 = UUID.randomUUID();
        Gym gym1 = buildGym(gymId1, 100);
        Gym gym2 = buildGym(gymId2, 50);

        when(gymRepository.findAll()).thenReturn(Arrays.asList(gym1, gym2));
        when(checkinService.getCurrentOccupancy(gymId1)).thenReturn(10);
        when(checkinService.getCurrentOccupancy(gymId2)).thenReturn(5);

        occupancyPublisher.publishOccupancy();

        verify(kafkaTemplate, times(2)).send(eq(KafkaTopics.GYM_OCCUPANCY), anyString(), anyString());
    }

    private Gym buildGym(UUID gymId, int maxCapacity) {
        Gym gym = new Gym();
        gym.setId(gymId);
        gym.setName("Test Gym");
        gym.setAddress("123 Test St");
        gym.setLatitude(new BigDecimal("40.416775"));
        gym.setLongitude(new BigDecimal("-3.703790"));
        gym.setOperatingHours("{\"mon\":\"06:00-22:00\"}");
        gym.setMaxCapacity(maxCapacity);
        gym.setCreatedAt(Instant.now());
        return gym;
    }
}
