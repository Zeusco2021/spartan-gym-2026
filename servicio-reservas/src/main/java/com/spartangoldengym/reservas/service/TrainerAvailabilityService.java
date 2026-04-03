package com.spartangoldengym.reservas.service;

import com.spartangoldengym.reservas.dto.TrainerAvailabilityRequest;
import com.spartangoldengym.reservas.dto.TrainerAvailabilityResponse;
import com.spartangoldengym.reservas.entity.TrainerAvailability;
import com.spartangoldengym.reservas.repository.TrainerAvailabilityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TrainerAvailabilityService {

    private static final Logger log = LoggerFactory.getLogger(TrainerAvailabilityService.class);

    private final TrainerAvailabilityRepository availabilityRepository;

    public TrainerAvailabilityService(TrainerAvailabilityRepository availabilityRepository) {
        this.availabilityRepository = availabilityRepository;
    }

    @Transactional(readOnly = true)
    public TrainerAvailabilityResponse getAvailability(UUID trainerId) {
        List<TrainerAvailability> slots = availabilityRepository.findByTrainerId(trainerId);
        return toResponse(trainerId, slots);
    }

    @Transactional
    public TrainerAvailabilityResponse updateAvailability(UUID trainerId, TrainerAvailabilityRequest request) {
        availabilityRepository.deleteByTrainerId(trainerId);

        List<TrainerAvailability> newSlots = new ArrayList<>();
        for (TrainerAvailabilityRequest.AvailabilitySlot slot : request.getSlots()) {
            TrainerAvailability entity = new TrainerAvailability();
            entity.setTrainerId(trainerId);
            entity.setDayOfWeek(slot.getDayOfWeek());
            entity.setStartTime(slot.getStartTime());
            entity.setEndTime(slot.getEndTime());
            newSlots.add(entity);
        }

        List<TrainerAvailability> saved = availabilityRepository.saveAll(newSlots);
        log.info("Updated availability for trainer {}: {} slots", trainerId, saved.size());
        return toResponse(trainerId, saved);
    }

    private TrainerAvailabilityResponse toResponse(UUID trainerId, List<TrainerAvailability> entities) {
        TrainerAvailabilityResponse response = new TrainerAvailabilityResponse();
        response.setTrainerId(trainerId);
        response.setSlots(entities.stream().map(e -> {
            TrainerAvailabilityResponse.AvailabilitySlot slot = new TrainerAvailabilityResponse.AvailabilitySlot();
            slot.setId(e.getId());
            slot.setDayOfWeek(e.getDayOfWeek());
            slot.setStartTime(e.getStartTime());
            slot.setEndTime(e.getEndTime());
            return slot;
        }).collect(Collectors.toList()));
        return response;
    }
}
