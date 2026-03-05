package com.example.securitymicroservice.auth.mapper;

import com.example.securitymicroservice.auth.dto.MeResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

/** Maps authentication objects to auth DTO responses. */
@Component
public class AuthMapper {

    public MeResponse toMeResponse(Authentication authentication) {
        List<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .sorted()
                .toList();
        return new MeResponse(authentication.getName(), authorities);
    }
}
