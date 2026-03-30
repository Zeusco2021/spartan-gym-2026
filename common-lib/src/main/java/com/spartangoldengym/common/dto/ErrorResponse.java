package com.spartangoldengym.common.dto;

import java.time.Instant;

public class ErrorResponse {

    private String error;
    private String message;
    private Instant timestamp;
    private String traceId;

    public ErrorResponse() {
    }

    public ErrorResponse(String error, String message, String traceId) {
        this.error = error;
        this.message = message;
        this.timestamp = Instant.now();
        this.traceId = traceId;
    }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
}
