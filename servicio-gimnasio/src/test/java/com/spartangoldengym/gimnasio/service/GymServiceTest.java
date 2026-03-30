package com.spartangoldengym.gimnasio.service;

import com.spartangoldengym.common.exception.ResourceNotFoundException;
import com.spartangoldengym.gimnasio.dto.*;
import com.spartangoldengym.gimnasio.entity.Gym;
import com.spartangoldengym.gimnasio.entity.GymChain;
import com.spartangoldengym.gimnasio.repository.GymChainRepository;
import com.spartangoldengym.gimnasio.repository.GymRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GymServiceTest {

    @Mock
    private GymRepository gymRepository;
    @Mock
    private GymChainRepository gymChainRepository;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private GeoOperations<String, String> geoOperations;

    private GymService gymService;

    @BeforeEach
    void setUp() {
        gymService = new GymService(gymRepository, gymChainRepository, redisTemplate);
    }

    @Test
    void createGym_withoutChain_savesAndReturnsResponse() {
        CreateGymRequest request = buildCreateRequest(null);
        Gym savedGym = buildGym(null);

        when(redisTemplate.opsForGeo()).thenReturn(geoOperations);
        when(gymRepository.save(any(Gym.class))).thenReturn(savedGym);

        GymResponse response = gymService.createGym(request);

        assertNotNull(response);
        assertEquals("Test Gym", response.getName());
        assertNull(response.getChainId());
        verify(gymRepository).save(any(Gym.class));
        verify(geoOperations).add(eq("geofence:gyms"), any(Point.class), anyString());
    }

    @Test
    void createGym_withChain_associatesChain() {
        UUID chainId = UUID.randomUUID();
        GymChain chain = new GymChain();
        chain.setId(chainId);
        chain.setName("Spartan Chain");

        CreateGymRequest request = buildCreateRequest(chainId);
        Gym savedGym = buildGym(chain);

        when(gymChainRepository.findById(chainId)).thenReturn(Optional.of(chain));
        when(redisTemplate.opsForGeo()).thenReturn(geoOperations);
        when(gymRepository.save(any(Gym.class))).thenReturn(savedGym);

        GymResponse response = gymService.createGym(request);

        assertEquals(chainId, response.getChainId());
        assertEquals("Spartan Chain", response.getChainName());
    }

    @Test
    void createGym_withInvalidChain_throwsNotFound() {
        UUID chainId = UUID.randomUUID();
        CreateGymRequest request = buildCreateRequest(chainId);

        when(gymChainRepository.findById(chainId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> gymService.createGym(request));
    }

    @Test
    void getGym_existingId_returnsResponse() {
        UUID gymId = UUID.randomUUID();
        Gym gym = buildGym(null);
        gym.setId(gymId);

        when(gymRepository.findById(gymId)).thenReturn(Optional.of(gym));

        GymResponse response = gymService.getGym(gymId);

        assertEquals(gymId, response.getId());
        assertEquals("Test Gym", response.getName());
    }

    @Test
    void getGym_nonExistingId_throwsNotFound() {
        UUID gymId = UUID.randomUUID();
        when(gymRepository.findById(gymId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> gymService.getGym(gymId));
    }

    @Test
    void listGyms_returnsPagedResults() {
        Gym gym = buildGym(null);
        Page<Gym> page = new PageImpl<>(Collections.singletonList(gym));

        when(gymRepository.findAll(any(PageRequest.class))).thenReturn(page);

        Page<GymResponse> result = gymService.listGyms(PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals("Test Gym", result.getContent().get(0).getName());
    }

    @Test
    void updateGym_updatesFieldsAndReturns() {
        UUID gymId = UUID.randomUUID();
        Gym gym = buildGym(null);
        gym.setId(gymId);

        UpdateGymRequest request = new UpdateGymRequest();
        request.setName("Updated Gym");
        request.setMaxCapacity(200);

        when(gymRepository.findById(gymId)).thenReturn(Optional.of(gym));
        when(gymRepository.save(any(Gym.class))).thenAnswer(inv -> inv.getArgument(0));

        GymResponse response = gymService.updateGym(gymId, request);

        assertEquals("Updated Gym", response.getName());
        assertEquals(200, response.getMaxCapacity());
    }

    @Test
    void updateGym_withCoordinateChange_updatesGeoSet() {
        UUID gymId = UUID.randomUUID();
        Gym gym = buildGym(null);
        gym.setId(gymId);

        UpdateGymRequest request = new UpdateGymRequest();
        request.setLatitude(new BigDecimal("40.0"));

        when(gymRepository.findById(gymId)).thenReturn(Optional.of(gym));
        when(gymRepository.save(any(Gym.class))).thenAnswer(inv -> inv.getArgument(0));
        when(redisTemplate.opsForGeo()).thenReturn(geoOperations);

        gymService.updateGym(gymId, request);

        verify(geoOperations).add(eq("geofence:gyms"), any(Point.class), anyString());
    }

    @Test
    @SuppressWarnings("unchecked")
    void findNearbyGyms_returnsOrderedByDistance() {
        UUID gymId = UUID.randomUUID();
        Gym gym = buildGym(null);
        gym.setId(gymId);

        GeoResult<RedisGeoCommands.GeoLocation<String>> geoResult =
                new GeoResult<>(
                        new RedisGeoCommands.GeoLocation<>(gymId.toString(), new Point(-3.7, 40.4)),
                        new Distance(2.5, Metrics.KILOMETERS)
                );
        GeoResults<RedisGeoCommands.GeoLocation<String>> geoResults =
                new GeoResults<>(Collections.singletonList(geoResult));

        when(redisTemplate.opsForGeo()).thenReturn(geoOperations);
        when(geoOperations.radius(eq("geofence:gyms"), any(Circle.class),
                any(RedisGeoCommands.GeoRadiusCommandArgs.class))).thenReturn(geoResults);
        when(gymRepository.findById(gymId)).thenReturn(Optional.of(gym));

        List<NearbyGymResponse> results = gymService.findNearbyGyms(40.4, -3.7, 10);

        assertEquals(1, results.size());
        assertEquals(gymId, results.get(0).getId());
        assertEquals(2.5, results.get(0).getDistanceKm());
    }

    private CreateGymRequest buildCreateRequest(UUID chainId) {
        CreateGymRequest request = new CreateGymRequest();
        request.setChainId(chainId);
        request.setName("Test Gym");
        request.setAddress("123 Test St");
        request.setLatitude(new BigDecimal("40.416775"));
        request.setLongitude(new BigDecimal("-3.703790"));
        request.setOperatingHours("{\"mon\":\"06:00-22:00\"}");
        request.setMaxCapacity(100);
        return request;
    }

    private Gym buildGym(GymChain chain) {
        Gym gym = new Gym();
        gym.setId(UUID.randomUUID());
        gym.setChain(chain);
        gym.setName("Test Gym");
        gym.setAddress("123 Test St");
        gym.setLatitude(new BigDecimal("40.416775"));
        gym.setLongitude(new BigDecimal("-3.703790"));
        gym.setOperatingHours("{\"mon\":\"06:00-22:00\"}");
        gym.setMaxCapacity(100);
        gym.setCreatedAt(Instant.now());
        return gym;
    }
}
