package com.spartangoldengym.gimnasio.service;

import com.spartangoldengym.common.exception.ResourceNotFoundException;
import com.spartangoldengym.gimnasio.dto.*;
import com.spartangoldengym.gimnasio.entity.Gym;
import com.spartangoldengym.gimnasio.entity.GymChain;
import com.spartangoldengym.gimnasio.repository.GymChainRepository;
import com.spartangoldengym.gimnasio.repository.GymRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.geo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class GymService {

    private static final String GEO_KEY = "geofence:gyms";

    private final GymRepository gymRepository;
    private final GymChainRepository gymChainRepository;
    private final StringRedisTemplate redisTemplate;

    public GymService(GymRepository gymRepository,
                      GymChainRepository gymChainRepository,
                      StringRedisTemplate redisTemplate) {
        this.gymRepository = gymRepository;
        this.gymChainRepository = gymChainRepository;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public GymResponse createGym(CreateGymRequest request) {
        GymChain chain = null;
        if (request.getChainId() != null) {
            chain = gymChainRepository.findById(request.getChainId())
                    .orElseThrow(() -> new ResourceNotFoundException("GymChain", request.getChainId().toString()));
        }

        Gym gym = new Gym();
        gym.setChain(chain);
        gym.setName(request.getName());
        gym.setAddress(request.getAddress());
        gym.setLatitude(request.getLatitude());
        gym.setLongitude(request.getLongitude());
        gym.setOperatingHours(request.getOperatingHours());
        gym.setMaxCapacity(request.getMaxCapacity());

        gym = gymRepository.save(gym);

        addGymToGeoSet(gym);

        return toResponse(gym);
    }

    @Transactional(readOnly = true)
    public Page<GymResponse> listGyms(Pageable pageable) {
        return gymRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public GymResponse getGym(UUID id) {
        Gym gym = gymRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gym", id.toString()));
        return toResponse(gym);
    }

    @Transactional
    public GymResponse updateGym(UUID id, UpdateGymRequest request) {
        Gym gym = gymRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gym", id.toString()));

        if (request.getName() != null) {
            gym.setName(request.getName());
        }
        if (request.getAddress() != null) {
            gym.setAddress(request.getAddress());
        }
        if (request.getLatitude() != null) {
            gym.setLatitude(request.getLatitude());
        }
        if (request.getLongitude() != null) {
            gym.setLongitude(request.getLongitude());
        }
        if (request.getOperatingHours() != null) {
            gym.setOperatingHours(request.getOperatingHours());
        }
        if (request.getMaxCapacity() != null) {
            gym.setMaxCapacity(request.getMaxCapacity());
        }

        gym = gymRepository.save(gym);

        // Update geo position if coordinates changed
        if (request.getLatitude() != null || request.getLongitude() != null) {
            addGymToGeoSet(gym);
        }

        return toResponse(gym);
    }

    public List<NearbyGymResponse> findNearbyGyms(double latitude, double longitude, double radiusKm) {
        GeoOperations<String, String> geoOps = redisTemplate.opsForGeo();

        Point center = new Point(longitude, latitude);
        Distance radius = new Distance(radiusKm, Metrics.KILOMETERS);

        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs
                .newGeoRadiusArgs()
                .includeCoordinates()
                .includeDistance()
                .sortAscending();

        GeoResults<RedisGeoCommands.GeoLocation<String>> results =
                geoOps.radius(GEO_KEY, new Circle(center, radius), args);

        List<NearbyGymResponse> nearbyGyms = new ArrayList<>();
        if (results != null) {
            for (GeoResult<RedisGeoCommands.GeoLocation<String>> result : results) {
                String gymIdStr = result.getContent().getName();
                UUID gymId;
                try {
                    gymId = UUID.fromString(gymIdStr);
                } catch (IllegalArgumentException e) {
                    continue;
                }

                gymRepository.findById(gymId).ifPresent(gym -> {
                    NearbyGymResponse nearby = new NearbyGymResponse();
                    nearby.setId(gym.getId());
                    nearby.setName(gym.getName());
                    nearby.setAddress(gym.getAddress());
                    nearby.setLatitude(gym.getLatitude());
                    nearby.setLongitude(gym.getLongitude());
                    nearby.setDistanceKm(result.getDistance().getValue());
                    nearbyGyms.add(nearby);
                });
            }
        }
        return nearbyGyms;
    }

    private void addGymToGeoSet(Gym gym) {
        GeoOperations<String, String> geoOps = redisTemplate.opsForGeo();
        Point point = new Point(
                gym.getLongitude().doubleValue(),
                gym.getLatitude().doubleValue()
        );
        geoOps.add(GEO_KEY, point, gym.getId().toString());
    }

    private GymResponse toResponse(Gym gym) {
        GymResponse response = new GymResponse();
        response.setId(gym.getId());
        response.setName(gym.getName());
        response.setAddress(gym.getAddress());
        response.setLatitude(gym.getLatitude());
        response.setLongitude(gym.getLongitude());
        response.setOperatingHours(gym.getOperatingHours());
        response.setMaxCapacity(gym.getMaxCapacity());
        response.setCreatedAt(gym.getCreatedAt());

        GymChain chain = gym.getChain();
        if (chain != null) {
            response.setChainId(chain.getId());
            response.setChainName(chain.getName());
        }

        return response;
    }
}
