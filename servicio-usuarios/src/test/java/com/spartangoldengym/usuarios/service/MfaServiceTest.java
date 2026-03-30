package com.spartangoldengym.usuarios.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MfaServiceTest {

    private MfaService mfaService;

    @BeforeEach
    void setUp() {
        mfaService = new MfaService();
    }

    @Test
    void generateSecret_returnsNonEmptyString() {
        String secret = mfaService.generateSecret();
        assertNotNull(secret);
        assertFalse(secret.isEmpty());
    }

    @Test
    void generateSecret_returnsDifferentSecretsEachTime() {
        String s1 = mfaService.generateSecret();
        String s2 = mfaService.generateSecret();
        assertNotEquals(s1, s2);
    }

    @Test
    void buildOtpAuthUri_containsSecretAndEmail() {
        String uri = mfaService.buildOtpAuthUri("MYSECRET", "user@example.com");
        assertTrue(uri.startsWith("otpauth://totp/SpartanGoldenGym:"));
        assertTrue(uri.contains("secret=MYSECRET"));
        assertTrue(uri.contains("user@example.com"));
        assertTrue(uri.contains("issuer=SpartanGoldenGym"));
    }

    @Test
    void validateCode_acceptsValidSixDigitCode() {
        assertTrue(mfaService.validateCode("somesecret", "123456"));
    }

    @Test
    void validateCode_rejectsNullSecret() {
        assertFalse(mfaService.validateCode(null, "123456"));
    }

    @Test
    void validateCode_rejectsNullCode() {
        assertFalse(mfaService.validateCode("secret", null));
    }

    @Test
    void validateCode_rejectsNonNumericCode() {
        assertFalse(mfaService.validateCode("secret", "abcdef"));
    }

    @Test
    void validateCode_rejectsWrongLengthCode() {
        assertFalse(mfaService.validateCode("secret", "12345"));
        assertFalse(mfaService.validateCode("secret", "1234567"));
    }
}
