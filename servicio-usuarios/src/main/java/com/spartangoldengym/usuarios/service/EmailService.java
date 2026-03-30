package com.spartangoldengym.usuarios.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Stub for Amazon SES email verification.
 * In production, this would integrate with AWS SES SDK.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    public void sendVerificationEmail(String email, String name) {
        log.info("Sending verification email to {} ({})", email, name);
        // TODO: Integrate with Amazon SES
        // SES call should complete within 5 seconds per requirement 1.1
    }
}
