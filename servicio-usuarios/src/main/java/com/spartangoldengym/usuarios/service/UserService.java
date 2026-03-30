package com.spartangoldengym.usuarios.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spartangoldengym.common.constants.AppConstants;
import com.spartangoldengym.common.exception.ConflictException;
import com.spartangoldengym.common.exception.ResourceNotFoundException;
import com.spartangoldengym.common.exception.UnauthorizedException;
import com.spartangoldengym.usuarios.dto.*;
import com.spartangoldengym.usuarios.entity.User;
import com.spartangoldengym.usuarios.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private static final String LOCKOUT_KEY_PREFIX = "lockout:";
    private static final String SESSION_KEY_PREFIX = "session:";
    private static final List<String> VALID_ROLES = Arrays.asList(
            AppConstants.ROLE_CLIENT, AppConstants.ROLE_TRAINER, AppConstants.ROLE_ADMIN);

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final StringRedisTemplate redisTemplate;
    private final MfaService mfaService;
    private final ObjectMapper objectMapper;

    public UserService(UserRepository userRepository,
                       JwtService jwtService,
                       EmailService emailService,
                       StringRedisTemplate redisTemplate,
                       MfaService mfaService) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder(AppConstants.BCRYPT_COST_FACTOR);
        this.jwtService = jwtService;
        this.emailService = emailService;
        this.redisTemplate = redisTemplate;
        this.mfaService = mfaService;
        this.objectMapper = new ObjectMapper();
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailAndDeletedAtIsNull(request.getEmail())) {
            throw new ConflictException(
                    "Email already in use: " + request.getEmail(), "EMAIL_EXISTS");
        }

        String role = request.getRole() != null ? request.getRole() : AppConstants.ROLE_CLIENT;
        if (!VALID_ROLES.contains(role)) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setRole(role);

        user = userRepository.save(user);

        emailService.sendVerificationEmail(user.getEmail(), user.getName());

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole());
        storeSession(user.getId().toString(), token);

        return new AuthResponse(token, user.getId(), user.getEmail(), user.getName(), user.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        String lockoutKey = LOCKOUT_KEY_PREFIX + request.getEmail();

        // Check if account is locked via Redis
        String lockoutValue = redisTemplate.opsForValue().get(lockoutKey);
        if (lockoutValue != null) {
            int attempts = Integer.parseInt(lockoutValue);
            if (attempts >= AppConstants.MAX_FAILED_LOGIN_ATTEMPTS) {
                throw new UnauthorizedException(
                        "Account is locked. Try again in " + AppConstants.ACCOUNT_LOCKOUT_MINUTES + " minutes.");
            }
        }

        User user = userRepository.findByEmailAndDeletedAtIsNull(request.getEmail())
                .orElseThrow(() -> {
                    incrementFailedAttempts(lockoutKey);
                    return new UnauthorizedException("Invalid email or password");
                });

        // Check DB-level lock
        if (user.getAccountLockedUntil() != null && user.getAccountLockedUntil().isAfter(Instant.now())) {
            throw new UnauthorizedException(
                    "Account is locked. Try again in " + AppConstants.ACCOUNT_LOCKOUT_MINUTES + " minutes.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            incrementFailedAttempts(lockoutKey);
            throw new UnauthorizedException("Invalid email or password");
        }

        // Successful login — reset lockout
        redisTemplate.delete(lockoutKey);
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);
        userRepository.save(user);

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole());
        storeSession(user.getId().toString(), token);

        return new AuthResponse(token, user.getId(), user.getEmail(), user.getName(), user.getRole());
    }

    public ProfileResponse getProfile(UUID userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));
        return toProfileResponse(user);
    }

    @Transactional
    public ProfileResponse updateProfile(UUID userId, ProfileUpdateRequest request) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));

        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getProfilePhotoUrl() != null) {
            user.setProfilePhotoUrl(request.getProfilePhotoUrl());
        }
        if (request.getLocale() != null) {
            user.setLocale(request.getLocale());
        }
        if (request.getFitnessGoals() != null) {
            user.setFitnessGoals(request.getFitnessGoals());
        }
        if (request.getMedicalConditions() != null) {
            user.setMedicalConditions(request.getMedicalConditions());
        }

        user = userRepository.save(user);
        return toProfileResponse(user);
    }

    @Transactional
    public DeletionResponse requestDeletion(UUID userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));

        user.setDeletedAt(Instant.now());
        // Null out personal data for GDPR soft delete
        user.setName("DELETED");
        user.setEmail("deleted-" + userId + "@deleted.local");
        user.setProfilePhotoUrl(null);
        user.setFitnessGoals(null);
        user.setMedicalConditions(null);
        user.setMfaSecret(null);
        userRepository.save(user);

        Instant scheduledDate = Instant.now().plus(30, ChronoUnit.DAYS);
        return new DeletionResponse(userId, "SCHEDULED", scheduledDate,
                "Account deletion scheduled. Personal data will be fully removed within 30 days.");
    }

    public DataExportResponse requestDataExport(UUID userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));

        // Stub: In production this would queue an async job to generate the export
        // and deliver it within 72 hours. For now, we acknowledge the request.
        return new DataExportResponse(userId, "PROCESSING",
                "Data export request received. Your data will be available within 72 hours.");
    }

    @Transactional
    public MfaSetupResponse setupMfa(UUID userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));

        String secret = mfaService.generateSecret();
        String otpAuthUri = mfaService.buildOtpAuthUri(secret, user.getEmail());

        user.setMfaSecret(secret);
        user.setMfaEnabled(true);
        userRepository.save(user);

        return new MfaSetupResponse(userId, secret, otpAuthUri, true);
    }

    public OnboardingResponse getOnboarding(UUID userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));

        return buildOnboardingResponse(user);
    }

    @Transactional
    public OnboardingResponse saveOnboarding(UUID userId, OnboardingRequest request) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));

        boolean isPartial = Boolean.TRUE.equals(request.getPartial());

        // Build onboarding data map to store in fitness_goals JSONB
        Map<String, Object> onboardingData = parseOnboardingData(user.getFitnessGoals());

        if (request.getFitnessLevel() != null) {
            onboardingData.put("fitnessLevel", request.getFitnessLevel());
        }
        if (request.getGoals() != null) {
            onboardingData.put("goals", request.getGoals());
        }
        if (request.getMedicalLimitations() != null) {
            onboardingData.put("medicalLimitations", request.getMedicalLimitations());
        }
        if (request.getDesiredFrequency() != null) {
            onboardingData.put("desiredFrequency", request.getDesiredFrequency());
        }
        if (request.getCurrentStep() != null) {
            onboardingData.put("onboardingStep", request.getCurrentStep());
        }

        try {
            user.setFitnessGoals(objectMapper.writeValueAsString(onboardingData));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize onboarding data", e);
        }

        // Store medical limitations in the dedicated field as well
        if (request.getMedicalLimitations() != null) {
            try {
                user.setMedicalConditions(objectMapper.writeValueAsString(request.getMedicalLimitations()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to serialize medical conditions", e);
            }
        }

        if (!isPartial) {
            user.setOnboardingCompleted(true);
        }

        userRepository.save(user);
        return buildOnboardingResponse(user);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseOnboardingData(String fitnessGoalsJson) {
        if (fitnessGoalsJson == null || fitnessGoalsJson.isEmpty()) {
            return new LinkedHashMap<>();
        }
        try {
            return objectMapper.readValue(fitnessGoalsJson, Map.class);
        } catch (JsonProcessingException e) {
            return new LinkedHashMap<>();
        }
    }

    private OnboardingResponse buildOnboardingResponse(User user) {
        OnboardingResponse response = new OnboardingResponse();
        response.setUserId(user.getId());
        response.setCompleted(Boolean.TRUE.equals(user.getOnboardingCompleted()));

        Map<String, Object> data = parseOnboardingData(user.getFitnessGoals());
        response.setFitnessLevel((String) data.get("fitnessLevel"));

        Object goals = data.get("goals");
        if (goals != null) {
            try {
                response.setGoals(objectMapper.writeValueAsString(goals));
            } catch (JsonProcessingException e) {
                response.setGoals(goals.toString());
            }
        }

        Object medLimitations = data.get("medicalLimitations");
        if (medLimitations != null) {
            try {
                response.setMedicalLimitations(objectMapper.writeValueAsString(medLimitations));
            } catch (JsonProcessingException e) {
                response.setMedicalLimitations(medLimitations.toString());
            }
        }

        Object freq = data.get("desiredFrequency");
        if (freq instanceof Number) {
            response.setDesiredFrequency(((Number) freq).intValue());
        }

        Object step = data.get("onboardingStep");
        if (step instanceof Number) {
            response.setCurrentStep(((Number) step).intValue());
        }

        return response;
    }

    private ProfileResponse toProfileResponse(User user) {
        ProfileResponse response = new ProfileResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setName(user.getName());
        response.setDateOfBirth(user.getDateOfBirth());
        response.setRole(user.getRole());
        response.setLocale(user.getLocale());
        response.setProfilePhotoUrl(user.getProfilePhotoUrl());
        response.setFitnessGoals(user.getFitnessGoals());
        response.setMedicalConditions(user.getMedicalConditions());
        response.setOnboardingCompleted(user.getOnboardingCompleted());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }

    private void incrementFailedAttempts(String lockoutKey) {
        Long attempts = redisTemplate.opsForValue().increment(lockoutKey);
        if (attempts != null && attempts == 1) {
            redisTemplate.expire(lockoutKey,
                    AppConstants.ACCOUNT_LOCKOUT_MINUTES, TimeUnit.MINUTES);
        }
        if (attempts != null && attempts >= AppConstants.MAX_FAILED_LOGIN_ATTEMPTS) {
            // Ensure TTL is set for lockout duration
            redisTemplate.expire(lockoutKey,
                    AppConstants.ACCOUNT_LOCKOUT_MINUTES, TimeUnit.MINUTES);
        }
    }

    private void storeSession(String userId, String token) {
        String sessionKey = SESSION_KEY_PREFIX + userId;
        redisTemplate.opsForValue().set(sessionKey, token,
                Duration.ofMillis(jwtService.getExpirationMs()));
    }
}
