package com.example.securitymicroservice.auth;

import java.util.List;

public record MeResponse(String username, List<String> authorities) {
}
