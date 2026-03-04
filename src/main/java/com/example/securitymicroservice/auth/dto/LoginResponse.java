package com.example.securitymicroservice.auth.dto;

/** JWT token response returned after successful authentication. */
public record LoginResponse(String accessToken) {
}
