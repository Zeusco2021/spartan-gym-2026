package com.spartangoldengym.entrenamiento.service;

import com.spartangoldengym.common.exception.ForbiddenException;
import com.spartangoldengym.common.exception.ResourceNotFoundException;
import com.spartangoldengym.entrenamiento.dto.*;
import com.spartangoldengym.entrenamiento.entity.TrainingPlan;
import com.spartangoldengym.entrenamiento.repository.TrainingPlanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TrainingPlanService {

    private static final Logger log = LoggerFactory.getLogger(TrainingPlanService.class);

    private final TrainingPlanRepository planRepository;

    public TrainingPlanService(TrainingPlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    @Transactional
    public TrainingPlanResponse createPlan(CreateTrainingPlanRequest request) {
        TrainingPlan plan = new TrainingPlan();
        plan.setUserId(request.getUserId());
        plan.setTrainerId(request.getTrainerId());
        plan.setName(request.getName());
        plan.setDescription(request.getDescription());
        plan.setAiGenerated(request.getAiGenerated() != null ? request.getAiGenerated() : false);

        plan = planRepository.save(plan);
        return toResponse(plan);
    }

    @Transactional(readOnly = true)
    public List<TrainingPlanResponse> listPlans(UUID userId) {
        List<TrainingPlan> plans = (userId != null)
                ? planRepository.findByUserId(userId)
                : planRepository.findAll();
        return plans.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TrainingPlanResponse getPlan(UUID id) {
        TrainingPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TrainingPlan", id.toString()));
        return toResponse(plan);
    }

    @Transactional
    public TrainingPlanResponse updatePlan(UUID id, UpdateTrainingPlanRequest request) {
        TrainingPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TrainingPlan", id.toString()));

        if (request.getName() != null) {
            plan.setName(request.getName());
        }
        if (request.getDescription() != null) {
            plan.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            plan.setStatus(request.getStatus());
        }

        plan = planRepository.save(plan);

        // Notify client via push when trainer modifies plan (Req 10.5)
        if (plan.getTrainerId() != null) {
            notifyClientPlanModified(plan);
        }

        return toResponse(plan);
    }

    @Transactional
    public void deletePlan(UUID id) {
        TrainingPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TrainingPlan", id.toString()));
        planRepository.delete(plan);
    }

    @Transactional
    public TrainingPlanResponse assignPlan(UUID planId, AssignPlanRequest request) {
        TrainingPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("TrainingPlan", planId.toString()));

        if (request.getTrainerId() == null) {
            throw new ForbiddenException("Trainer ID is required to assign a plan");
        }

        plan.setUserId(request.getClientId());
        plan.setTrainerId(request.getTrainerId());
        plan = planRepository.save(plan);

        notifyClientPlanModified(plan);

        return toResponse(plan);
    }

    private void notifyClientPlanModified(TrainingPlan plan) {
        // Stub: push notification to client when trainer modifies their plan (Req 10.5)
        log.info("PUSH NOTIFICATION: Plan '{}' (id={}) modified by trainer {} for client {}",
                plan.getName(), plan.getId(), plan.getTrainerId(), plan.getUserId());
    }

    TrainingPlanResponse toResponse(TrainingPlan plan) {
        TrainingPlanResponse response = new TrainingPlanResponse();
        response.setId(plan.getId());
        response.setUserId(plan.getUserId());
        response.setTrainerId(plan.getTrainerId());
        response.setName(plan.getName());
        response.setDescription(plan.getDescription());
        response.setAiGenerated(plan.getAiGenerated());
        response.setStatus(plan.getStatus());
        response.setCreatedAt(plan.getCreatedAt());
        return response;
    }
}
