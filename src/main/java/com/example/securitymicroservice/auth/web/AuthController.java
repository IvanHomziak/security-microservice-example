package com.example.securitymicroservice.auth.web;

import com.example.securitymicroservice.auth.web.dto.LoginRequest;
import com.example.securitymicroservice.auth.web.dto.LoginResponse;
import com.example.securitymicroservice.auth.web.dto.MeResponse;
import com.example.securitymicroservice.auth.application.AuthFacade;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
/** REST endpoints for authentication and current-user introspection. */
public class AuthController {

    private final AuthFacade authFacade;

    public AuthController(AuthFacade authFacade) {
        this.authFacade = authFacade;
    }

    /** Authenticates user credentials and returns a signed JWT access token. */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authFacade.login(request));
    }

    /** Returns current principal name and authorities extracted from authentication context. */
    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(Authentication authentication) {
        return ResponseEntity.ok(authFacade.me(authentication));
    }
}
