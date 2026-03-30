package com.spartangoldengym.usuarios.dto;

import java.util.UUID;

public class MfaSetupResponse {

    private UUID userId;
    private String secret;
    private String otpAuthUri;
    private boolean mfaEnabled;

    public MfaSetupResponse() {}

    public MfaSetupResponse(UUID userId, String secret, String otpAuthUri, boolean mfaEnabled) {
        this.userId = userId;
        this.secret = secret;
        this.otpAuthUri = otpAuthUri;
        this.mfaEnabled = mfaEnabled;
    }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    public String getOtpAuthUri() { return otpAuthUri; }
    public void setOtpAuthUri(String otpAuthUri) { this.otpAuthUri = otpAuthUri; }
    public boolean isMfaEnabled() { return mfaEnabled; }
    public void setMfaEnabled(boolean mfaEnabled) { this.mfaEnabled = mfaEnabled; }
}
