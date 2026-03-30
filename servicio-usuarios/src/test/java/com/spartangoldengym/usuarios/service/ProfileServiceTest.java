package com.spartangoldengym.usuarios.service;

import com.spartangoldengym.common.exception.ResourceNotFoundException;
import com.spartangoldengym.usuarios.dto.DataExportResponse;
import com.spartangoldengym.usuarios.dto.DeletionResponse;
import com.spartangoldengym.usuarios.dto.ProfileResponse;
import com.spartangoldengym.usuarios.dto.ProfileUpdateRequest;
import com.spartangoldengym.usuarios.entity.User;
import com.spartangoldengym.usuarios.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private JwtService jwtService;
    @Mock private EmailService emailService;
    @Mock private StringRedisTemplate redisTemplate;
    @Mock private MfaService mfaService;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, jwtService, emailService, redisTemplate, mfaService);
    }

    @Test
    void getProfile_returnsProfileForExistingUser() {
        UUID userId = UUID.randomUUID();
        User user = makeUser(userId, "test@example.com", "Test User");

        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));

        ProfileResponse profile = userService.getProfile(userId);

        assertEquals(userId, profile.getId());
        assertEquals("test@example.com", profile.getEmail());
        assertEquals("Test User", profile.getName());
        assertEquals("client", profile.getRole());
        assertEquals("es", profile.getLocale());
    }

    @Test
    void getProfile_throwsNotFoundForMissingUser() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getProfile(userId));
    }

    @Test
    void updateProfile_updatesNameAndGoals() {
        UUID userId = UUID.randomUUID();
        User user = makeUser(userId, "test@example.com", "Old Name");

        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setName("New Name");
        request.setFitnessGoals("{\"goal\":\"muscle_gain\"}");

        ProfileResponse profile = userService.updateProfile(userId, request);

        assertEquals("New Name", profile.getName());
        assertEquals("{\"goal\":\"muscle_gain\"}", profile.getFitnessGoals());
    }

    @Test
    void updateProfile_partialUpdatePreservesExistingFields() {
        UUID userId = UUID.randomUUID();
        User user = makeUser(userId, "test@example.com", "Original Name");
        user.setProfilePhotoUrl("https://example.com/photo.jpg");
        user.setFitnessGoals("{\"goal\":\"weight_loss\"}");

        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setLocale("en");
        // name, photo, goals not set — should remain unchanged

        ProfileResponse profile = userService.updateProfile(userId, request);

        assertEquals("Original Name", profile.getName());
        assertEquals("https://example.com/photo.jpg", profile.getProfilePhotoUrl());
        assertEquals("{\"goal\":\"weight_loss\"}", profile.getFitnessGoals());
        assertEquals("en", profile.getLocale());
    }

    @Test
    void requestDeletion_softDeletesAndNullsPersonalData() {
        UUID userId = UUID.randomUUID();
        User user = makeUser(userId, "test@example.com", "Test User");
        user.setProfilePhotoUrl("https://example.com/photo.jpg");
        user.setFitnessGoals("{\"goal\":\"muscle_gain\"}");
        user.setMedicalConditions("{\"condition\":\"none\"}");
        user.setMfaSecret("secret123");

        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        DeletionResponse response = userService.requestDeletion(userId);

        assertEquals(userId, response.getUserId());
        assertEquals("SCHEDULED", response.getStatus());
        assertNotNull(response.getScheduledDeletionDate());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();

        assertNotNull(saved.getDeletedAt());
        assertEquals("DELETED", saved.getName());
        assertNull(saved.getProfilePhotoUrl());
        assertNull(saved.getFitnessGoals());
        assertNull(saved.getMedicalConditions());
        assertNull(saved.getMfaSecret());
    }

    @Test
    void requestDeletion_throwsNotFoundForMissingUser() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.requestDeletion(userId));
    }

    @Test
    void requestDataExport_returnsProcessingStatus() {
        UUID userId = UUID.randomUUID();
        User user = makeUser(userId, "test@example.com", "Test User");

        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));

        DataExportResponse response = userService.requestDataExport(userId);

        assertEquals(userId, response.getUserId());
        assertEquals("PROCESSING", response.getStatus());
        assertNotNull(response.getRequestedAt());
        assertNotNull(response.getMessage());
    }

    @Test
    void requestDataExport_throwsNotFoundForMissingUser() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.requestDataExport(userId));
    }

    private User makeUser(UUID id, String email, String name) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setName(name);
        user.setPasswordHash("$2a$12$hashedpassword");
        user.setRole("client");
        user.setDateOfBirth(LocalDate.of(1990, 1, 15));
        user.setLocale("es");
        user.setFailedLoginAttempts(0);
        user.setOnboardingCompleted(false);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        return user;
    }
}
