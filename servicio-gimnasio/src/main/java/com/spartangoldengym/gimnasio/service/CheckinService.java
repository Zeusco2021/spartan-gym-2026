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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CheckinService {

    private static final String OCCUPANCY_KEY_PREFIX = "occupancy:";

    private final GymCheckinRepository checkinRepository;
    private final GymRepository gymRepository;
    private final StringRedisTemplate redisTemplate;

    public CheckinService(GymCheckinRepository checkinRepository,
                          GymRepository gymRepository,
                          StringRedisTemplate redisTemplate) {
        this.checkinRepository = checkinRepository;
        this.gymRepository = gymRepository;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public CheckinResponse checkin(UUID gymId, CheckinRequest request) {
        Gym gym = gymRepository.findById(gymId)
                .orElseThrow(() -> new ResourceNotFoundException("Gym", gymId.toString()));

        // Stub membership verification: checks that userId is not null (real impl would call servicio-pagos)
        if (!hasActiveMembership(request.getUserId())) {
            throw new ForbiddenException("User does not have an active membership");
        }

        GymCheckin checkin = new GymCheckin();
        checkin.setGym(gym);
        checkin.setUserId(request.getUserId());
        checkin = checkinRepository.save(checkin);

        // Update occupancy in Redis
        redisTemplate.opsForValue().increment(OCCUPANCY_KEY_PREFIX + gymId);

        return toResponse(checkin, gymId);
    }

    @Transactional(readOnly = true)
    public OccupancyResponse getOccupancy(UUID gymId) {
        Gym gym = gymRepository.findById(gymId)
                .orElseThrow(() -> new ResourceNotFoundException("Gym", gymId.toString()));

        int currentOccupancy = getCurrentOccupancy(gymId);

        OccupancyResponse response = new OccupancyResponse();
        response.setGymId(gymId);
        response.setCurrentOccupancy(currentOccupancy);
        response.setMaxCapacity(gym.getMaxCapacity());
        response.setOccupancyPercentage(
                gym.getMaxCapacity() > 0
                        ? (double) currentOccupancy / gym.getMaxCapacity() * 100.0
                        : 0.0
        );
        return response;
    }

    public int getCurrentOccupancy(UUID gymId) {
        String value = redisTemplate.opsForValue().get(OCCUPANCY_KEY_PREFIX + gymId);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                // fallback to DB count
            }
        }
        long count = checkinRepository.countByGymIdAndCheckedOutAtIsNull(gymId);
        // Sync Redis with DB value
        redisTemplate.opsForValue().set(OCCUPANCY_KEY_PREFIX + gymId, String.valueOf(count));
        return (int) count;
    }

    /**
     * Stub membership verification. In production, this would call servicio-pagos
     * to verify the user has an active subscription.
     */
    boolean hasActiveMembership(UUID userId) {
        return userId != null;
    }

    private CheckinResponse toResponse(GymCheckin checkin, UUID gymId) {
        CheckinResponse response = new CheckinResponse();
        response.setId(checkin.getId());
        response.setGymId(gymId);
        response.setUserId(checkin.getUserId());
        response.setCheckedInAt(checkin.getCheckedInAt());
        return response;
    }
}
