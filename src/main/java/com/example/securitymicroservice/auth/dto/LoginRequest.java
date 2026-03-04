package com.example.securitymicroservice.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** Login request payload. */
public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password
) {
}
