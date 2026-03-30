package com.spartangoldengym.usuarios.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

@Service
public class JwtService {

    private static final String HMAC_SHA256 = "HmacSHA256";

    @Value("${jwt.secret:spartan-golden-gym-default-secret-key-change-in-production}")
    private String secret;

    @Value("${jwt.expiration-ms:3600000}")
    private long expirationMs;

    public String generateToken(UUID userId, String email, String role) {
        long now = System.currentTimeMillis();
        long exp = now + expirationMs;

        String header = base64Encode("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String payload = base64Encode(
                "{\"sub\":\"" + userId.toString() + "\","
                + "\"email\":\"" + email + "\","
                + "\"role\":\"" + role + "\","
                + "\"iat\":" + (now / 1000) + ","
                + "\"exp\":" + (exp / 1000) + "}"
        );

        String signature = sign(header + "." + payload);
        return header + "." + payload + "." + signature;
    }

    public boolean validateToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return false;

            String expectedSignature = sign(parts[0] + "." + parts[1]);
            if (!expectedSignature.equals(parts[2])) return false;

            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            long exp = extractExpFromJson(payloadJson);
            return (exp * 1000) > System.currentTimeMillis();
        } catch (Exception e) {
            return false;
        }
    }

    public String extractSubject(String token) {
        String[] parts = token.split("\\.");
        String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        return extractFieldFromJson(payloadJson, "sub");
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    private String base64Encode(String value) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec keySpec = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(keySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to sign JWT", e);
        }
    }

    private long extractExpFromJson(String json) {
        String expStr = extractFieldFromJson(json, "exp");
        return Long.parseLong(expStr);
    }

    private String extractFieldFromJson(String json, String field) {
        String search = "\"" + field + "\":";
        int start = json.indexOf(search);
        if (start == -1) return "";
        start += search.length();
        // skip optional quote
        boolean quoted = json.charAt(start) == '"';
        if (quoted) {
            start++;
            int end = json.indexOf('"', start);
            return json.substring(start, end);
        } else {
            int end = start;
            while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}') {
                end++;
            }
            return json.substring(start, end).trim();
        }
    }
}
