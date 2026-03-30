package com.spartangoldengym.seguimiento.controller;

import com.spartangoldengym.seguimiento.dto.*;
import com.spartangoldengym.seguimiento.service.WearableService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for wearable device integration and biometric data sync.
 *
 * Encryption: TLS 1.3 in transit at infrastructure level, AES-256 at rest by AWS.
 *
 * Validates: Requirements 8.1, 8.2, 8.3, 8.5, 8.6
 */
@RestController
@RequestMapping("/api/wearables")
public class WearableController {

    private final WearableService wearableService;

    public WearableController(WearableService wearableService) {
        this.wearableService = wearableService;
    }

    @PostMapping("/connect")
    public ResponseEntity<WearableConnectResponse> connect(
            @RequestBody WearableConnectRequest request) {
        WearableConnectResponse response = wearableService.connectWearable(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/sync")
    public ResponseEntity<WearableSyncResponse> sync(
            @RequestBody WearableSyncRequest request) {
        WearableSyncResponse response = wearableService.syncData(request);
        return ResponseEntity.ok(response);
    }
}
