package com.spartangoldengym.mensajeria.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MessageEncryptionService (AES-256-GCM).
 *
 * Validates: Requirement 25.8
 */
class MessageEncryptionServiceTest {

    private MessageEncryptionService encryptionService;

    @BeforeEach
    void setUp() {
        encryptionService = new MessageEncryptionService();
    }

    @Test
    void encryptDecrypt_roundTrip_returnsOriginal() {
        String original = "Hello, this is a secret message!";
        String encrypted = encryptionService.encrypt(original);
        String decrypted = encryptionService.decrypt(encrypted);

        assertNotEquals(original, encrypted);
        assertEquals(original, decrypted);
    }

    @Test
    void encrypt_nullInput_returnsNull() {
        assertNull(encryptionService.encrypt(null));
    }

    @Test
    void encrypt_emptyInput_returnsEmpty() {
        assertEquals("", encryptionService.encrypt(""));
    }

    @Test
    void decrypt_nullInput_returnsNull() {
        assertNull(encryptionService.decrypt(null));
    }

    @Test
    void encrypt_sameInput_producesDifferentCiphertext() {
        String original = "Same message";
        String encrypted1 = encryptionService.encrypt(original);
        String encrypted2 = encryptionService.encrypt(original);

        // GCM with random IV should produce different ciphertext each time
        assertNotEquals(encrypted1, encrypted2);
    }
}
