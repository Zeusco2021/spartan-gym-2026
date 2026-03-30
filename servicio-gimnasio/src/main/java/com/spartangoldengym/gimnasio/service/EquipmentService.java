package com.spartangoldengym.gimnasio.service;

import com.spartangoldengym.common.exception.ResourceNotFoundException;
import com.spartangoldengym.gimnasio.dto.EquipmentRequest;
import com.spartangoldengym.gimnasio.dto.EquipmentResponse;
import com.spartangoldengym.gimnasio.entity.Gym;
import com.spartangoldengym.gimnasio.entity.GymEquipment;
import com.spartangoldengym.gimnasio.repository.GymEquipmentRepository;
import com.spartangoldengym.gimnasio.repository.GymRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EquipmentService {

    private final GymEquipmentRepository equipmentRepository;
    private final GymRepository gymRepository;

    public EquipmentService(GymEquipmentRepository equipmentRepository,
                            GymRepository gymRepository) {
        this.equipmentRepository = equipmentRepository;
        this.gymRepository = gymRepository;
    }

    @Transactional(readOnly = true)
    public List<EquipmentResponse> getEquipment(UUID gymId) {
        if (!gymRepository.existsById(gymId)) {
            throw new ResourceNotFoundException("Gym", gymId.toString());
        }
        return equipmentRepository.findByGymId(gymId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<EquipmentResponse> updateEquipment(UUID gymId, List<EquipmentRequest> requests) {
        Gym gym = gymRepository.findById(gymId)
                .orElseThrow(() -> new ResourceNotFoundException("Gym", gymId.toString()));

        // Replace all equipment for this gym
        List<GymEquipment> existing = equipmentRepository.findByGymId(gymId);
        equipmentRepository.deleteAll(existing);

        List<GymEquipment> newEquipment = requests.stream().map(req -> {
            GymEquipment eq = new GymEquipment();
            eq.setGym(gym);
            eq.setName(req.getName());
            eq.setCategory(req.getCategory());
            eq.setQuantity(req.getQuantity());
            eq.setStatus(req.getStatus() != null ? req.getStatus() : "available");
            return eq;
        }).collect(Collectors.toList());

        List<GymEquipment> saved = equipmentRepository.saveAll(newEquipment);

        return saved.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private EquipmentResponse toResponse(GymEquipment equipment) {
        EquipmentResponse response = new EquipmentResponse();
        response.setId(equipment.getId());
        response.setGymId(equipment.getGym().getId());
        response.setName(equipment.getName());
        response.setCategory(equipment.getCategory());
        response.setQuantity(equipment.getQuantity());
        response.setStatus(equipment.getStatus());
        return response;
    }
}
