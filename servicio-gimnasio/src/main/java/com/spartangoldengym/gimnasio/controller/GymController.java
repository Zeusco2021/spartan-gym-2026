package com.spartangoldengym.gimnasio.controller;

import com.spartangoldengym.gimnasio.dto.*;
import com.spartangoldengym.gimnasio.service.CheckinService;
import com.spartangoldengym.gimnasio.service.EquipmentService;
import com.spartangoldengym.gimnasio.service.GymService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/gyms")
public class GymController {

    private final GymService gymService;
    private final CheckinService checkinService;
    private final EquipmentService equipmentService;

    public GymController(GymService gymService,
                         CheckinService checkinService,
                         EquipmentService equipmentService) {
        this.gymService = gymService;
        this.checkinService = checkinService;
        this.equipmentService = equipmentService;
    }

    @PostMapping
    public ResponseEntity<GymResponse> createGym(@Valid @RequestBody CreateGymRequest request) {
        GymResponse response = gymService.createGym(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<GymResponse>> listGyms(Pageable pageable) {
        Page<GymResponse> gyms = gymService.listGyms(pageable);
        return ResponseEntity.ok(gyms);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GymResponse> getGym(@PathVariable UUID id) {
        GymResponse response = gymService.getGym(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GymResponse> updateGym(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateGymRequest request) {
        GymResponse response = gymService.updateGym(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<NearbyGymResponse>> findNearbyGyms(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "10") double radiusKm) {
        List<NearbyGymResponse> nearby = gymService.findNearbyGyms(latitude, longitude, radiusKm);
        return ResponseEntity.ok(nearby);
    }

    // --- Check-in endpoints (Req 2.4) ---

    @PostMapping("/{id}/checkin")
    public ResponseEntity<CheckinResponse> checkin(
            @PathVariable UUID id,
            @Valid @RequestBody CheckinRequest request) {
        CheckinResponse response = checkinService.checkin(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // --- Occupancy endpoint (Req 2.5) ---

    @GetMapping("/{id}/occupancy")
    public ResponseEntity<OccupancyResponse> getOccupancy(@PathVariable UUID id) {
        OccupancyResponse response = checkinService.getOccupancy(id);
        return ResponseEntity.ok(response);
    }

    // --- Equipment endpoints (Req 2.6) ---

    @GetMapping("/{id}/equipment")
    public ResponseEntity<List<EquipmentResponse>> getEquipment(@PathVariable UUID id) {
        List<EquipmentResponse> equipment = equipmentService.getEquipment(id);
        return ResponseEntity.ok(equipment);
    }

    @PutMapping("/{id}/equipment")
    public ResponseEntity<List<EquipmentResponse>> updateEquipment(
            @PathVariable UUID id,
            @Valid @RequestBody List<EquipmentRequest> requests) {
        List<EquipmentResponse> equipment = equipmentService.updateEquipment(id, requests);
        return ResponseEntity.ok(equipment);
    }
}
