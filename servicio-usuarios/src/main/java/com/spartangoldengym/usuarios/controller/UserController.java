package com.spartangoldengym.usuarios.controller;

import com.spartangoldengym.usuarios.dto.*;
import com.spartangoldengym.usuarios.service.AuditService;
import com.spartangoldengym.usuarios.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AuditService auditService;

    public UserController(UserService userService, AuditService auditService) {
        this.userService = userService;
        this.auditService = auditService;
    }

    @PostMapping("/mfa/setup")
    public ResponseEntity<MfaSetupResponse> setupMfa(
            @RequestHeader("X-User-Id") String userIdHeader,
            HttpServletRequest request) {
        UUID userId = UUID.fromString(userIdHeader);
        MfaSetupResponse response = userService.setupMfa(userId);
        auditService.logAction(userId, "MFA_SETUP", "USER", userId.toString(),
                null, extractIp(request));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/onboarding")
    public ResponseEntity<OnboardingResponse> getOnboarding(
            @RequestHeader("X-User-Id") String userIdHeader) {
        UUID userId = UUID.fromString(userIdHeader);
        OnboardingResponse response = userService.getOnboarding(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/onboarding")
    public ResponseEntity<OnboardingResponse> saveOnboarding(
            @RequestHeader("X-User-Id") String userIdHeader,
            @Valid @RequestBody OnboardingRequest onboardingRequest,
            HttpServletRequest request) {
        UUID userId = UUID.fromString(userIdHeader);
        OnboardingResponse response = userService.saveOnboarding(userId, onboardingRequest);
        String action = response.isCompleted() ? "ONBOARDING_COMPLETED" : "ONBOARDING_PARTIAL_SAVE";
        auditService.logAction(userId, action, "USER", userId.toString(),
                null, extractIp(request));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<ProfileResponse> getProfile(
            @RequestHeader("X-User-Id") String userIdHeader,
            HttpServletRequest request) {
        UUID userId = UUID.fromString(userIdHeader);
        ProfileResponse profile = userService.getProfile(userId);
        auditService.logAction(userId, "PROFILE_VIEW", "USER", userId.toString(),
                null, extractIp(request));
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    public ResponseEntity<ProfileResponse> updateProfile(
            @RequestHeader("X-User-Id") String userIdHeader,
            @Valid @RequestBody ProfileUpdateRequest updateRequest,
            HttpServletRequest request) {
        UUID userId = UUID.fromString(userIdHeader);
        ProfileResponse profile = userService.updateProfile(userId, updateRequest);
        auditService.logAction(userId, "PROFILE_UPDATE", "USER", userId.toString(),
                null, extractIp(request));
        return ResponseEntity.ok(profile);
    }

    @DeleteMapping("/profile/delete")
    public ResponseEntity<DeletionResponse> requestDeletion(
            @RequestHeader("X-User-Id") String userIdHeader,
            HttpServletRequest request) {
        UUID userId = UUID.fromString(userIdHeader);
        DeletionResponse response = userService.requestDeletion(userId);
        auditService.logAction(userId, "GDPR_DELETION_REQUEST", "USER", userId.toString(),
                null, extractIp(request));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/data-export")
    public ResponseEntity<DataExportResponse> requestDataExport(
            @RequestHeader("X-User-Id") String userIdHeader,
            HttpServletRequest request) {
        UUID userId = UUID.fromString(userIdHeader);
        DataExportResponse response = userService.requestDataExport(userId);
        auditService.logAction(userId, "DATA_EXPORT_REQUEST", "USER", userId.toString(),
                null, extractIp(request));
        return ResponseEntity.accepted().body(response);
    }

    private String extractIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
