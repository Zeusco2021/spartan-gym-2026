package com.spartangoldengym.usuarios.controller;

import com.spartangoldengym.common.exception.ConflictException;
import com.spartangoldengym.common.exception.UnauthorizedException;
import com.spartangoldengym.usuarios.dto.AuthResponse;
import com.spartangoldengym.usuarios.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.spartangoldengym.usuarios.dto.RegisterRequest;
import com.spartangoldengym.usuarios.dto.LoginRequest;

import java.time.LocalDate;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock private UserService userService;
    @InjectMocks private UserController userController;

    @Test
    void register_returns201WithAuthResponse() {
        UUID userId = UUID.randomUUID();
        AuthResponse authResponse = new AuthResponse("jwt-token", userId, "test@example.com", "Test", "client");
        when(userService.register(any())).thenReturn(authResponse);

        RegisterRequest request = new RegisterRequest();
        request.setName("Test");
        request.setEmail("test@example.com");
        request.setPassword("StrongPass1!");
        request.setDateOfBirth(LocalDate.of(1990, 1, 1));

        ResponseEntity<AuthResponse> response = userController.register(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("jwt-token", response.getBody().getToken());
    }

    @Test
    void login_returns200WithAuthResponse() {
        UUID userId = UUID.randomUUID();
        AuthResponse authResponse = new AuthResponse("jwt-token", userId, "test@example.com", "Test", "client");
        when(userService.login(any())).thenReturn(authResponse);

        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("StrongPass1!");

        ResponseEntity<AuthResponse> response = userController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("jwt-token", response.getBody().getToken());
    }
}
