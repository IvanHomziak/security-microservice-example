package com.example.securitymicroservice.auth.controller;

import com.example.securitymicroservice.auth.service.AuthService;
import com.example.securitymicroservice.auth.dto.LoginRequest;
import com.example.securitymicroservice.auth.dto.LoginResponse;
import com.example.securitymicroservice.auth.dto.MeResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/auth")
/** REST endpoints for authentication and current-user introspection. */
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** Authenticates user credentials and returns a signed JWT access token. */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /** Returns current principal name and authorities extracted from authentication context. */
    @GetMapping("/me")
    public MeResponse me(Authentication authentication) {
        List<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .sorted()
                .toList();
        return new MeResponse(authentication.getName(), authorities);
    }
}
