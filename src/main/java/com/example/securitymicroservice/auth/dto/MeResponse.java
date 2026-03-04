package com.example.securitymicroservice.auth.dto;

import java.util.List;

public record MeResponse(String username, List<String> authorities) {
}
