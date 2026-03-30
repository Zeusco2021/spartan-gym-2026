package com.spartangoldengym.usuarios.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", "test-secret-key-for-unit-tests");
        ReflectionTestUtils.setField(jwtService, "expirationMs", 3600000L);
    }

    @Test
    void generateToken_returnsValidJwtFormat() {
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateToken(userId, "test@example.com", "client");

        assertNotNull(token);
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length, "JWT should have 3 parts");
    }

    @Test
    void validateToken_returnsTrueForValidToken() {
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateToken(userId, "test@example.com", "client");

        assertTrue(jwtService.validateToken(token));
    }

    @Test
    void validateToken_returnsFalseForTamperedToken() {
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateToken(userId, "test@example.com", "client");

        String tampered = token + "x";
        assertFalse(jwtService.validateToken(tampered));
    }

    @Test
    void validateToken_returnsFalseForExpiredToken() {
        ReflectionTestUtils.setField(jwtService, "expirationMs", -1000L);
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateToken(userId, "test@example.com", "client");

        assertFalse(jwtService.validateToken(token));
    }

    @Test
    void extractSubject_returnsCorrectUserId() {
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateToken(userId, "test@example.com", "client");

        assertEquals(userId.toString(), jwtService.extractSubject(token));
    }

    @Test
    void validateToken_returnsFalseForGarbage() {
        assertFalse(jwtService.validateToken("not.a.jwt"));
    }
}
