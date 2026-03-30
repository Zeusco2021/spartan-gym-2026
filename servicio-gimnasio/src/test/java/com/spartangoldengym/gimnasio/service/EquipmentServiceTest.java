package com.spartangoldengym.gimnasio.service;

import com.spartangoldengym.common.exception.ResourceNotFoundException;
import com.spartangoldengym.gimnasio.dto.EquipmentRequest;
import com.spartangoldengym.gimnasio.dto.EquipmentResponse;
import com.spartangoldengym.gimnasio.entity.Gym;
import com.spartangoldengym.gimnasio.entity.GymEquipment;
import com.spartangoldengym.gimnasio.repository.GymEquipmentRepository;
import com.spartangoldengym.gimnasio.repository.GymRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EquipmentServiceTest {

    @Mock
    private GymEquipmentRepository equipmentRepository;
    @Mock
    private GymRepository gymRepository;

    private EquipmentService equipmentService;

    @BeforeEach
    void setUp() {
        equipmentService = new EquipmentService(equipmentRepository, gymRepository);
    }

    @Test
    void getEquipment_returnsListForGym() {
        UUID gymId = UUID.randomUUID();
        Gym gym = buildGym(gymId);
        GymEquipment eq = buildEquipment(gym, "Treadmill", "Cardio", 5);

        when(gymRepository.existsById(gymId)).thenReturn(true);
        when(equipmentRepository.findByGymId(gymId)).thenReturn(Collections.singletonList(eq));

        List<EquipmentResponse> result = equipmentService.getEquipment(gymId);

        assertEquals(1, result.size());
        assertEquals("Treadmill", result.get(0).getName());
        assertEquals("Cardio", result.get(0).getCategory());
        assertEquals(5, result.get(0).getQuantity());
    }

    @Test
    void getEquipment_nonExistentGym_throwsNotFound() {
        UUID gymId = UUID.randomUUID();
        when(gymRepository.existsById(gymId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> equipmentService.getEquipment(gymId));
    }

    @Test
    void updateEquipment_replacesAllEquipment() {
        UUID gymId = UUID.randomUUID();
        Gym gym = buildGym(gymId);

        when(gymRepository.findById(gymId)).thenReturn(Optional.of(gym));
        when(equipmentRepository.findByGymId(gymId)).thenReturn(Collections.emptyList());
        when(equipmentRepository.saveAll(anyList())).thenAnswer(inv -> {
            List<GymEquipment> items = inv.getArgument(0);
            for (GymEquipment item : items) {
                item.setId(UUID.randomUUID());
            }
            return items;
        });

        EquipmentRequest req1 = new EquipmentRequest();
        req1.setName("Bench Press");
        req1.setCategory("Strength");
        req1.setQuantity(3);

        EquipmentRequest req2 = new EquipmentRequest();
        req2.setName("Dumbbell Set");
        req2.setCategory("Strength");
        req2.setQuantity(10);
        req2.setStatus("available");

        List<EquipmentResponse> result = equipmentService.updateEquipment(gymId, Arrays.asList(req1, req2));

        assertEquals(2, result.size());
        verify(equipmentRepository).deleteAll(anyList());
        verify(equipmentRepository).saveAll(anyList());
    }

    @Test
    void updateEquipment_nonExistentGym_throwsNotFound() {
        UUID gymId = UUID.randomUUID();
        when(gymRepository.findById(gymId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> equipmentService.updateEquipment(gymId, Collections.emptyList()));
    }

    @Test
    void updateEquipment_defaultsStatusToAvailable() {
        UUID gymId = UUID.randomUUID();
        Gym gym = buildGym(gymId);

        when(gymRepository.findById(gymId)).thenReturn(Optional.of(gym));
        when(equipmentRepository.findByGymId(gymId)).thenReturn(Collections.emptyList());
        when(equipmentRepository.saveAll(anyList())).thenAnswer(inv -> {
            List<GymEquipment> items = inv.getArgument(0);
            for (GymEquipment item : items) {
                item.setId(UUID.randomUUID());
            }
            return items;
        });

        EquipmentRequest req = new EquipmentRequest();
        req.setName("Rowing Machine");
        req.setCategory("Cardio");
        req.setQuantity(2);
        // status not set

        List<EquipmentResponse> result = equipmentService.updateEquipment(gymId, Collections.singletonList(req));

        assertEquals(1, result.size());
        assertEquals("available", result.get(0).getStatus());
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

    private GymEquipment buildEquipment(Gym gym, String name, String category, int quantity) {
        GymEquipment eq = new GymEquipment();
        eq.setId(UUID.randomUUID());
        eq.setGym(gym);
        eq.setName(name);
        eq.setCategory(category);
        eq.setQuantity(quantity);
        eq.setStatus("available");
        return eq;
    }
}
