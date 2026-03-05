package com.example.securitymicroservice.auth.facade;

import com.example.securitymicroservice.auth.dto.LoginRequest;
import com.example.securitymicroservice.auth.dto.LoginResponse;
import com.example.securitymicroservice.auth.dto.MeResponse;
import com.example.securitymicroservice.auth.mapper.AuthMapper;
import com.example.securitymicroservice.auth.service.AuthService;
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
