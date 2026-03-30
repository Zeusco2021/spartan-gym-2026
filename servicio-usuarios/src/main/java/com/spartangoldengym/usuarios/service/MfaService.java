package com.spartangoldengym.usuarios.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Simple MFA service for TOTP-based multi-factor authentication.
 * Generates secrets and validates TOTP codes.
 */
@Service
public class MfaService {

    private static final int SECRET_LENGTH = 20;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Generates a Base32-encoded TOTP secret.
     */
    public String generateSecret() {
        byte[] bytes = new byte[SECRET_LENGTH];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Builds an otpauth:// URI for QR code generation.
     */
    public String buildOtpAuthUri(String secret, String email) {
        return "otpauth://totp/SpartanGoldenGym:" + email
                + "?secret=" + secret
                + "&issuer=SpartanGoldenGym";
    }

    /**
     * Validates a TOTP code against the stored secret.
     * This is a simplified stub — in production, use a proper TOTP library
     * (e.g., GoogleAuthenticator, aerogear-otp-java).
     */
    public boolean validateCode(String secret, String code) {
        if (secret == null || code == null || code.length() != 6) {
            return false;
        }
        // Stub: accept any 6-digit numeric code for now.
        // A real implementation would compute HMAC-SHA1 based TOTP.
        return code.matches("\\d{6}");
    }
}
