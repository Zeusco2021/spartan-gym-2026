package com.spartangoldengym.gateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class JwtAuthenticationFilterTest {

    private JwtAuthenticationFilter filter;
    private static final String SECRET = "test-secret-key";

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter();
        ReflectionTestUtils.setField(filter, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(filter, "publicPaths",
                Arrays.asList("/api/users/register", "/api/users/login"));
    }

    @Test
    void validateToken_validToken_returnsUserId() {
        String token = createToken("{\"alg\":\"HS256\"}", "{\"sub\":\"user-123\",\"exp\":" + (System.currentTimeMillis() / 1000 + 3600) + "}");
        String result = filter.validateToken(token);
        assertEquals("user-123", result);
    }

    @Test
    void validateToken_expiredToken_returnsNull() {
        String token = createToken("{\"alg\":\"HS256\"}", "{\"sub\":\"user-123\",\"exp\":1000}");
        assertNull(filter.validateToken(token));
    }

    @Test
    void validateToken_invalidSignature_returnsNull() {
        String header = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"HS256\"}".getBytes(StandardCharsets.UTF_8));
        String payload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"sub\":\"user-123\"}".getBytes(StandardCharsets.UTF_8));
        String token = header + "." + payload + ".invalidsignature";
        assertNull(filter.validateToken(token));
    }

    @Test
    void validateToken_malformedToken_returnsNull() {
        assertNull(filter.validateToken("not-a-jwt"));
        assertNull(filter.validateToken("only.two"));
        assertNull(filter.validateToken(""));
    }

    @Test
    void extractJsonField_extractsStringValue() {
        assertEquals("hello", JwtAuthenticationFilter.extractJsonField("{\"key\":\"hello\"}", "key"));
    }

    @Test
    void extractJsonField_extractsNumericValue() {
        assertEquals("42", JwtAuthenticationFilter.extractJsonField("{\"num\":42}", "num"));
    }

    @Test
    void extractJsonField_missingField_returnsNull() {
        assertNull(JwtAuthenticationFilter.extractJsonField("{\"other\":\"val\"}", "key"));
    }

    private String createToken(String headerJson, String payloadJson) {
        try {
            String header = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(headerJson.getBytes(StandardCharsets.UTF_8));
            String payload = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
            String headerPayload = header + "." + payload;

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] sig = mac.doFinal(headerPayload.getBytes(StandardCharsets.UTF_8));
            String signature = Base64.getUrlEncoder().withoutPadding().encodeToString(sig);

            return headerPayload + "." + signature;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
