package com.spartangoldengym.gimnasio.service;

import com.spartangoldengym.common.exception.ForbiddenException;
import com.spartangoldengym.common.exception.ResourceNotFoundException;
import com.spartangoldengym.gimnasio.dto.CheckinRequest;
import com.spartangoldengym.gimnasio.dto.CheckinResponse;
import com.spartangoldengym.gimnasio.dto.OccupancyResponse;
import com.spartangoldengym.gimnasio.entity.Gym;
import com.spartangoldengym.gimnasio.entity.GymCheckin;
import com.spartangoldengym.gimnasio.repository.GymCheckinRepository;
import com.spartangoldengym.gimnasio.repository.GymRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckinServiceTest {

    @Mock
    private GymCheckinRepository checkinRepository;
    @Mock
    private GymRepository gymRepository;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private CheckinService checkinService;

    @BeforeEach
    void setUp() {
        checkinService = new CheckinService(checkinRepository, gymRepository, redisTemplate);
    }

    @Test
    void checkin_withValidMembership_createsCheckinAndUpdatesRedis() {
        UUID gymId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Gym gym = buildGym(gymId);
        GymCheckin savedCheckin = buildCheckin(gymId, userId);

        when(gymRepository.findById(gymId)).thenReturn(Optional.of(gym));
        when(checkinRepository.save(any(GymCheckin.class))).thenReturn(savedCheckin);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        CheckinRequest request = new CheckinRequest();
        request.setUserId(userId);

        CheckinResponse response = checkinService.checkin(gymId, request);

        assertNotNull(response);
        assertEquals(gymId, response.getGymId());
        assertEquals(userId, response.getUserId());
        verify(checkinRepository).save(any(GymCheckin.class));
        verify(valueOperations).increment("occupancy:" + gymId);
    }

    @Test
    void checkin_withNullUserId_throwsForbidden() {
        UUID gymId = UUID.randomUUID();
        Gym gym = buildGym(gymId);

        when(gymRepository.findById(gymId)).thenReturn(Optional.of(gym));

        CheckinRequest request = new CheckinRequest();
        request.setUserId(null);

        assertThrows(ForbiddenException.class, () -> checkinService.checkin(gymId, request));
        verify(checkinRepository, never()).save(any());
    }

    @Test
    void checkin_withNonExistentGym_throwsNotFound() {
        UUID gymId = UUID.randomUUID();
        when(gymRepository.findById(gymId)).thenReturn(Optional.empty());

        CheckinRequest request = new CheckinRequest();
        request.setUserId(UUID.randomUUID());

        assertThrows(ResourceNotFoundException.class, () -> checkinService.checkin(gymId, request));
    }

    @Test
    void getOccupancy_returnsCorrectPercentage() {
        UUID gymId = UUID.randomUUID();
        Gym gym = buildGym(gymId);
        gym.setMaxCapacity(100);

        when(gymRepository.findById(gymId)).thenReturn(Optional.of(gym));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("occupancy:" + gymId)).thenReturn("25");

        OccupancyResponse response = checkinService.getOccupancy(gymId);

        assertEquals(gymId, response.getGymId());
        assertEquals(25, response.getCurrentOccupancy());
        assertEquals(100, response.getMaxCapacity());
        assertEquals(25.0, response.getOccupancyPercentage(), 0.01);
    }

    @Test
    void getOccupancy_nonExistentGym_throwsNotFound() {
        UUID gymId = UUID.randomUUID();
        when(gymRepository.findById(gymId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> checkinService.getOccupancy(gymId));
    }

    @Test
    void getCurrentOccupancy_fallsBackToDbWhenRedisEmpty() {
        UUID gymId = UUID.randomUUID();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("occupancy:" + gymId)).thenReturn(null);
        when(checkinRepository.countByGymIdAndCheckedOutAtIsNull(gymId)).thenReturn(10L);

        int occupancy = checkinService.getCurrentOccupancy(gymId);

        assertEquals(10, occupancy);
        verify(valueOperations).set("occupancy:" + gymId, "10");
    }

    private Gym buildGym(UUID gymId) {
        Gym gym = new Gym();
        gym.setId(gymId);
        gym.setName("Test Gym");
        gym.setAddress("123 Test St");
        gym.setLatitude(new BigDecimal("40.416775"));
        gym.setLongitude(new BigDecimal("-3.703790"));
        gym.setOperatingHours("{\"mon\":\"06:00-22:00\"}");
        gym.setMaxCapacity(100);
        gym.setCreatedAt(Instant.now());
        return gym;
    }

    private GymCheckin buildCheckin(UUID gymId, UUID userId) {
        Gym gym = buildGym(gymId);
        GymCheckin checkin = new GymCheckin();
        checkin.setId(UUID.randomUUID());
        checkin.setGym(gym);
        checkin.setUserId(userId);
        checkin.setCheckedInAt(Instant.now());
        return checkin;
    }
}
