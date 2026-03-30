package com.spartangoldengym.common.exception;

import com.spartangoldengym.common.dto.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleValidation_returns400() throws NoSuchMethodException {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("obj", "email", "must not be blank");
        when(bindingResult.getFieldErrors()).thenReturn(Collections.singletonList(fieldError));
        MethodParameter param = new MethodParameter(
                GlobalExceptionHandlerTest.class.getDeclaredMethod("dummyMethod", String.class), 0);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(param, bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("VALIDATION_ERROR", response.getBody().getError());
        assertTrue(response.getBody().getMessage().contains("email"));
        assertNotNull(response.getBody().getTraceId());
        assertNotNull(response.getBody().getTimestamp());
    }

    @SuppressWarnings("unused")
    private void dummyMethod(String arg) {
    }

    @Test
    void handleIllegalArgument_returns400() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid input");

        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("BAD_REQUEST", response.getBody().getError());
        assertEquals("Invalid input", response.getBody().getMessage());
    }

    @Test
    void handleUnauthorized_returns401() {
        UnauthorizedException ex = new UnauthorizedException("Invalid credentials");

        ResponseEntity<ErrorResponse> response = handler.handleUnauthorized(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("UNAUTHORIZED", response.getBody().getError());
        assertEquals("Invalid credentials", response.getBody().getMessage());
    }

    @Test
    void handleForbidden_returns403() {
        ForbiddenException ex = new ForbiddenException("Access denied");

        ResponseEntity<ErrorResponse> response = handler.handleForbidden(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("FORBIDDEN", response.getBody().getError());
        assertEquals("Access denied", response.getBody().getMessage());
    }

    @Test
    void handleResourceNotFound_returns404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("User", "123");

        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("RESOURCE_NOT_FOUND", response.getBody().getError());
        assertTrue(response.getBody().getMessage().contains("123"));
    }

    @Test
    void handleConflict_returns409() {
        ConflictException ex = new ConflictException("Email already exists", "DUPLICATE_EMAIL");

        ResponseEntity<ErrorResponse> response = handler.handleConflict(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("CONFLICT", response.getBody().getError());
    }

    @Test
    void handleRateLimit_returns429() {
        RateLimitExceededException ex = new RateLimitExceededException("Rate limit exceeded");

        ResponseEntity<ErrorResponse> response = handler.handleRateLimit(ex);

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertEquals("RATE_LIMIT_EXCEEDED", response.getBody().getError());
    }

    @Test
    void handleGeneral_returns500() {
        Exception ex = new RuntimeException("Something broke");

        ResponseEntity<ErrorResponse> response = handler.handleGeneral(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("INTERNAL_ERROR", response.getBody().getError());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
    }

    @Test
    void handleServiceUnavailable_returns503() {
        ServiceUnavailableException ex = new ServiceUnavailableException("UserService");

        ResponseEntity<ErrorResponse> response = handler.handleServiceUnavailable(ex);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("SERVICE_UNAVAILABLE", response.getBody().getError());
        assertTrue(response.getBody().getMessage().contains("UserService"));
    }

    @Test
    void allResponses_haveTraceIdAndTimestamp() {
        ResponseEntity<ErrorResponse> r1 = handler.handleUnauthorized(new UnauthorizedException("test"));
        ResponseEntity<ErrorResponse> r2 = handler.handleForbidden(new ForbiddenException("test"));

        assertNotNull(r1.getBody().getTraceId());
        assertNotNull(r1.getBody().getTimestamp());
        assertNotNull(r2.getBody().getTraceId());
        assertNotNull(r2.getBody().getTimestamp());
        // Each response should have a unique traceId
        assertNotEquals(r1.getBody().getTraceId(), r2.getBody().getTraceId());
    }
}
