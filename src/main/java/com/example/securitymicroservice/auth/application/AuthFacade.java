package com.example.securitymicroservice.auth.application;

import com.example.securitymicroservice.auth.web.dto.LoginRequest;
import com.example.securitymicroservice.auth.web.dto.LoginResponse;
import com.example.securitymicroservice.auth.web.dto.MeResponse;
import com.example.securitymicroservice.auth.application.AuthMapper;
import com.example.securitymicroservice.auth.application.AuthService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/** Facade that coordinates authentication use-cases. */
@Component
public class AuthFacade {

    private final AuthService authService;
    private final AuthMapper authMapper;

    public AuthFacade(AuthService authService, AuthMapper authMapper) {
        this.authService = authService;
        this.authMapper = authMapper;
    }

    public LoginResponse login(LoginRequest request) {
        return authService.login(request);
    }

    public MeResponse me(Authentication authentication) {
        return authMapper.toMeResponse(authentication);
    }
}
