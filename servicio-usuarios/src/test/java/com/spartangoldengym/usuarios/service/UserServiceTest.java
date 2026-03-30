package com.spartangoldengym.usuarios.service;

import com.spartangoldengym.common.constants.AppConstants;
import com.spartangoldengym.common.exception.ConflictException;
import com.spartangoldengym.common.exception.UnauthorizedException;
import com.spartangoldengym.usuarios.dto.AuthResponse;
import com.spartangoldengym.usuarios.dto.LoginRequest;
import com.spartangoldengym.usuarios.dto.RegisterRequest;
import com.spartangoldengym.usuarios.entity.User;
import com.spartangoldengym.usuarios.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private JwtService jwtService;
    @Mock private EmailService emailService;
    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;
    @Mock private MfaService mfaService;

    private UserService userService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(AppConstants.BCRYPT_COST_FACTOR);

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, jwtService, emailService, redisTemplate, mfaService);
    }

    @Test
    void register_createsUserAndReturnsToken() {
        RegisterRequest request = makeRegisterRequest("john@example.com", "John", "StrongPass1!");
        UUID userId = UUID.randomUUID();

        when(userRepository.existsByEmailAndDeletedAtIsNull("john@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(userId);
            return u;
        });
        when(jwtService.generateToken(eq(userId), eq("john@example.com"), eq("client")))
                .thenReturn("mock-jwt-token");
        when(jwtService.getExpirationMs()).thenReturn(3600000L);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        AuthResponse response = userService.register(request);

        assertEquals("mock-jwt-token", response.getToken());
        assertEquals(userId, response.getUserId());
        assertEquals("john@example.com", response.getEmail());
        assertEquals("John", response.getName());
        assertEquals("client", response.getRole());

        verify(emailService).sendVerificationEmail("john@example.com", "John");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertTrue(saved.getPasswordHash().startsWith("$2a$12$") || saved.getPasswordHash().startsWith("$2b$12$"),
                "Password should be bcrypt hashed with cost factor 12");
    }

    @Test
    void register_throwsConflictForDuplicateEmail() {
        RegisterRequest request = makeRegisterRequest("dup@example.com", "Dup", "StrongPass1!");
        when(userRepository.existsByEmailAndDeletedAtIsNull("dup@example.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_defaultsToClientRole() {
        RegisterRequest request = makeRegisterRequest("client@example.com", "Client", "StrongPass1!");
        request.setRole(null);

        when(userRepository.existsByEmailAndDeletedAtIsNull(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });
        when(jwtService.generateToken(any(), anyString(), anyString())).thenReturn("token");
        when(jwtService.getExpirationMs()).thenReturn(3600000L);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        AuthResponse response = userService.register(request);
        assertEquals("client", response.getRole());
    }

    @Test
    void register_supportsTrainerRole() {
        RegisterRequest request = makeRegisterRequest("trainer@example.com", "Trainer", "StrongPass1!");
        request.setRole("trainer");

        when(userRepository.existsByEmailAndDeletedAtIsNull(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });
        when(jwtService.generateToken(any(), anyString(), eq("trainer"))).thenReturn("token");
        when(jwtService.getExpirationMs()).thenReturn(3600000L);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        AuthResponse response = userService.register(request);
        assertEquals("trainer", response.getRole());
    }

    @Test
    void register_rejectsInvalidRole() {
        RegisterRequest request = makeRegisterRequest("bad@example.com", "Bad", "StrongPass1!");
        request.setRole("superadmin");

        when(userRepository.existsByEmailAndDeletedAtIsNull(anyString())).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> userService.register(request));
    }

    @Test
    void login_returnsTokenForValidCredentials() {
        LoginRequest request = new LoginRequest();
        request.setEmail("john@example.com");
        request.setPassword("StrongPass1!");

        User user = makeUser("john@example.com", "John", encoder.encode("StrongPass1!"));

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("lockout:john@example.com")).thenReturn(null);
        when(userRepository.findByEmailAndDeletedAtIsNull("john@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(eq(user.getId()), eq("john@example.com"), eq("client")))
                .thenReturn("login-jwt");
        when(jwtService.getExpirationMs()).thenReturn(3600000L);

        AuthResponse response = userService.login(request);

        assertEquals("login-jwt", response.getToken());
        assertEquals("john@example.com", response.getEmail());
        verify(redisTemplate).delete("lockout:john@example.com");
    }

    @Test
    void login_throwsUnauthorizedForWrongPassword() {
        LoginRequest request = new LoginRequest();
        request.setEmail("john@example.com");
        request.setPassword("WrongPass!");

        User user = makeUser("john@example.com", "John", encoder.encode("StrongPass1!"));

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("lockout:john@example.com")).thenReturn(null);
        when(userRepository.findByEmailAndDeletedAtIsNull("john@example.com")).thenReturn(Optional.of(user));
        when(valueOperations.increment("lockout:john@example.com")).thenReturn(1L);

        assertThrows(UnauthorizedException.class, () -> userService.login(request));
    }

    @Test
    void login_throwsUnauthorizedWhenAccountLocked() {
        LoginRequest request = new LoginRequest();
        request.setEmail("locked@example.com");
        request.setPassword("StrongPass1!");

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("lockout:locked@example.com"))
                .thenReturn(String.valueOf(AppConstants.MAX_FAILED_LOGIN_ATTEMPTS));

        assertThrows(UnauthorizedException.class, () -> userService.login(request));
        verify(userRepository, never()).findByEmailAndDeletedAtIsNull(anyString());
    }

    private RegisterRequest makeRegisterRequest(String email, String name, String password) {
        RegisterRequest req = new RegisterRequest();
        req.setEmail(email);
        req.setName(name);
        req.setPassword(password);
        req.setDateOfBirth(LocalDate.of(1990, 1, 15));
        return req;
    }

    private User makeUser(String email, String name, String passwordHash) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setName(name);
        user.setPasswordHash(passwordHash);
        user.setRole("client");
        user.setDateOfBirth(LocalDate.of(1990, 1, 15));
        user.setFailedLoginAttempts(0);
        return user;
    }
}
