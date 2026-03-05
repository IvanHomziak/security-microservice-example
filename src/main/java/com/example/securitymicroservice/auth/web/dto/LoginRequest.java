package com.example.securitymicroservice.auth.web.dto;

import jakarta.validation.constraints.NotBlank;

/** Login request payload. */
public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password
) {
}
