package com.example.securitymicroservice.common.exception;

import java.time.Instant;
import java.util.Map;

/** Standard error payload for API exception responses. */
public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String code,
        String message,
        String path,
        Map<String, String> details
) {
}
