package com.spartangoldengym.entrenamiento.service;

import com.spartangoldengym.common.exception.ForbiddenException;
import com.spartangoldengym.common.exception.ResourceNotFoundException;
import com.spartangoldengym.entrenamiento.dto.*;
import com.spartangoldengym.entrenamiento.entity.TrainingPlan;
import com.spartangoldengym.entrenamiento.repository.TrainingPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingPlanServiceTest {

    @Mock
    private TrainingPlanRepository planRepository;

    private TrainingPlanService service;

    @BeforeEach
    void setUp() {
        service = new TrainingPlanService(planRepository);
    }

    @Test
    void createPlan_savesAndReturnsResponse() {
        CreateTrainingPlanRequest request = new CreateTrainingPlanRequest();
        request.setUserId(UUID.randomUUID());
        request.setName("Strength Plan");
        request.setDescription("8-week strength program");

        TrainingPlan saved = buildPlan(request.getUserId(), null, "Strength Plan");
        when(planRepository.save(any(TrainingPlan.class))).thenReturn(saved);

        TrainingPlanResponse response = service.createPlan(request);

        assertNotNull(response.getId());
        assertEquals("Strength Plan", response.getName());
        assertEquals("active", response.getStatus());
        verify(planRepository).save(any(TrainingPlan.class));
    }

    @Test
    void listPlans_withUserId_filtersResults() {
        UUID userId = UUID.randomUUID();
        TrainingPlan plan = buildPlan(userId, null, "My Plan");
        when(planRepository.findByUserId(userId)).thenReturn(Collections.singletonList(plan));

        List<TrainingPlanResponse> results = service.listPlans(userId);

        assertEquals(1, results.size());
        assertEquals(userId, results.get(0).getUserId());
    }

    @Test
    void listPlans_withoutUserId_returnsAll() {
        when(planRepository.findAll()).thenReturn(Arrays.asList(
                buildPlan(UUID.randomUUID(), null, "Plan A"),
                buildPlan(UUID.randomUUID(), null, "Plan B")
        ));

        List<TrainingPlanResponse> results = service.listPlans(null);

        assertEquals(2, results.size());
    }

    @Test
    void getPlan_existingId_returnsResponse() {
        UUID planId = UUID.randomUUID();
        TrainingPlan plan = buildPlan(UUID.randomUUID(), null, "Test Plan");
        plan.setId(planId);
        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));

        TrainingPlanResponse response = service.getPlan(planId);

        assertEquals(planId, response.getId());
    }

    @Test
    void getPlan_nonExistingId_throwsNotFound() {
        UUID planId = UUID.randomUUID();
        when(planRepository.findById(planId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getPlan(planId));
    }

    @Test
    void updatePlan_updatesFieldsAndReturns() {
        UUID planId = UUID.randomUUID();
        TrainingPlan plan = buildPlan(UUID.randomUUID(), null, "Old Name");
        plan.setId(planId);

        UpdateTrainingPlanRequest request = new UpdateTrainingPlanRequest();
        request.setName("New Name");
        request.setStatus("paused");

        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(planRepository.save(any(TrainingPlan.class))).thenAnswer(inv -> inv.getArgument(0));

        TrainingPlanResponse response = service.updatePlan(planId, request);

        assertEquals("New Name", response.getName());
        assertEquals("paused", response.getStatus());
    }

    @Test
    void deletePlan_existingId_deletes() {
        UUID planId = UUID.randomUUID();
        TrainingPlan plan = buildPlan(UUID.randomUUID(), null, "To Delete");
        plan.setId(planId);
        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));

        service.deletePlan(planId);

        verify(planRepository).delete(plan);
    }

    @Test
    void deletePlan_nonExistingId_throwsNotFound() {
        UUID planId = UUID.randomUUID();
        when(planRepository.findById(planId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.deletePlan(planId));
    }

    @Test
    void assignPlan_setsClientAndTrainer() {
        UUID planId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        UUID trainerId = UUID.randomUUID();

        TrainingPlan plan = buildPlan(UUID.randomUUID(), null, "Assign Plan");
        plan.setId(planId);

        AssignPlanRequest request = new AssignPlanRequest();
        request.setClientId(clientId);
        request.setTrainerId(trainerId);

        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(planRepository.save(any(TrainingPlan.class))).thenAnswer(inv -> inv.getArgument(0));

        TrainingPlanResponse response = service.assignPlan(planId, request);

        assertEquals(clientId, response.getUserId());
        assertEquals(trainerId, response.getTrainerId());
    }

    @Test
    void assignPlan_withoutTrainerId_throwsForbidden() {
        UUID planId = UUID.randomUUID();
        TrainingPlan plan = buildPlan(UUID.randomUUID(), null, "Plan");
        plan.setId(planId);

        AssignPlanRequest request = new AssignPlanRequest();
        request.setClientId(UUID.randomUUID());
        request.setTrainerId(null);

        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));

        assertThrows(ForbiddenException.class, () -> service.assignPlan(planId, request));
    }

    private TrainingPlan buildPlan(UUID userId, UUID trainerId, String name) {
        TrainingPlan plan = new TrainingPlan();
        plan.setId(UUID.randomUUID());
        plan.setUserId(userId);
        plan.setTrainerId(trainerId);
        plan.setName(name);
        plan.setStatus("active");
        plan.setAiGenerated(false);
        plan.setCreatedAt(Instant.now());
        return plan;
    }
}
