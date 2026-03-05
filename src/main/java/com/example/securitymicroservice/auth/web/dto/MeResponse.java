package com.example.securitymicroservice.auth.web.dto;

import java.util.List;

/** Response payload with current user identity and granted authorities. */
public record MeResponse(String username, List<String> authorities) {
}
