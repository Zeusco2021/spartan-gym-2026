package com.spartangoldengym.usuarios.service;

import com.spartangoldengym.common.exception.ResourceNotFoundException;
import com.spartangoldengym.usuarios.dto.MfaSetupResponse;
import com.spartangoldengym.usuarios.dto.OnboardingRequest;
import com.spartangoldengym.usuarios.dto.OnboardingResponse;
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
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MfaOnboardingServiceTest {

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

    // --- MFA Tests ---

    @Test
    void setupMfa_generatesSecretAndEnablesMfa() {
        UUID userId = UUID.randomUUID();
        User user = makeUser(userId, "test@example.com");

        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));
        when(mfaService.generateSecret()).thenReturn("GENERATED_SECRET");
        when(mfaService.buildOtpAuthUri("GENERATED_SECRET", "test@example.com"))
                .thenReturn("otpauth://totp/SpartanGoldenGym:test@example.com?secret=GENERATED_SECRET&issuer=SpartanGoldenGym");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        MfaSetupResponse response = userService.setupMfa(userId);

        assertEquals(userId, response.getUserId());
        assertEquals("GENERATED_SECRET", response.getSecret());
        assertTrue(response.getOtpAuthUri().contains("GENERATED_SECRET"));
        assertTrue(response.isMfaEnabled());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertEquals("GENERATED_SECRET", saved.getMfaSecret());
        assertTrue(saved.getMfaEnabled());
    }

    @Test
    void setupMfa_throwsNotFoundForMissingUser() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.setupMfa(userId));
    }

    // --- Onboarding GET Tests ---

    @Test
    void getOnboarding_returnsEmptyStateForNewUser() {
        UUID userId = UUID.randomUUID();
        User user = makeUser(userId, "new@example.com");

        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));

        OnboardingResponse response = userService.getOnboarding(userId);

        assertEquals(userId, response.getUserId());
        assertFalse(response.isCompleted());
        assertNull(response.getFitnessLevel());
        assertNull(response.getGoals());
        assertNull(response.getCurrentStep());
    }

    @Test
    void getOnboarding_returnsPartialProgress() {
        UUID userId = UUID.randomUUID();
        User user = makeUser(userId, "partial@example.com");
        user.setFitnessGoals("{\"fitnessLevel\":\"beginner\",\"onboardingStep\":2}");

        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));

        OnboardingResponse response = userService.getOnboarding(userId);

        assertEquals(userId, response.getUserId());
        assertFalse(response.isCompleted());
        assertEquals("beginner", response.getFitnessLevel());
        assertEquals(2, response.getCurrentStep());
    }

    @Test
    void getOnboarding_throwsNotFoundForMissingUser() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getOnboarding(userId));
    }

    // --- Onboarding POST Tests ---

    @Test
    void saveOnboarding_partialSavePreservesProgress() {
        UUID userId = UUID.randomUUID();
        User user = makeUser(userId, "partial@example.com");

        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        OnboardingRequest request = new OnboardingRequest();
        request.setFitnessLevel("intermediate");
        request.setCurrentStep(1);
        request.setPartial(true);

        OnboardingResponse response = userService.saveOnboarding(userId, request);

        assertFalse(response.isCompleted());
        assertEquals("intermediate", response.getFitnessLevel());
        assertEquals(1, response.getCurrentStep());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertFalse(captor.getValue().getOnboardingCompleted());
    }

    @Test
    void saveOnboarding_completeSaveMarksProfileActive() {
        UUID userId = UUID.randomUUID();
        User user = makeUser(userId, "complete@example.com");

        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        OnboardingRequest request = new OnboardingRequest();
        request.setFitnessLevel("advanced");
        request.setGoals(Arrays.asList("muscle_gain", "endurance"));
        request.setMedicalLimitations(Arrays.asList("back_pain"));
        request.setDesiredFrequency(5);
        request.setPartial(false);

        OnboardingResponse response = userService.saveOnboarding(userId, request);

        assertTrue(response.isCompleted());
        assertEquals("advanced", response.getFitnessLevel());
        assertEquals(5, response.getDesiredFrequency());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertTrue(saved.getOnboardingCompleted());
        assertNotNull(saved.getFitnessGoals());
        assertNotNull(saved.getMedicalConditions());
    }

    @Test
    void saveOnboarding_mergesWithExistingPartialData() {
        UUID userId = UUID.randomUUID();
        User user = makeUser(userId, "merge@example.com");
        user.setFitnessGoals("{\"fitnessLevel\":\"beginner\",\"onboardingStep\":1}");

        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        OnboardingRequest request = new OnboardingRequest();
        request.setGoals(Arrays.asList("weight_loss"));
        request.setCurrentStep(2);
        request.setPartial(true);

        OnboardingResponse response = userService.saveOnboarding(userId, request);

        // Previous fitnessLevel should be preserved
        assertEquals("beginner", response.getFitnessLevel());
        assertEquals(2, response.getCurrentStep());
        assertNotNull(response.getGoals());
        assertFalse(response.isCompleted());
    }

    @Test
    void saveOnboarding_throwsNotFoundForMissingUser() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.saveOnboarding(userId, new OnboardingRequest()));
    }

    private User makeUser(UUID id, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setName("Test User");
        user.setPasswordHash("$2a$12$hashedpassword");
        user.setRole("client");
        user.setDateOfBirth(LocalDate.of(1990, 1, 15));
        user.setLocale("es");
        user.setFailedLoginAttempts(0);
        user.setOnboardingCompleted(false);
        user.setMfaEnabled(false);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        return user;
    }
}
