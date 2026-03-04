package com.example.securitymicroservice.config;

import com.example.securitymicroservice.user.AppUserRepository;
import com.example.securitymicroservice.user.Permission;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/items/**").hasAuthority(Permission.DATA_READ.name())
                        .requestMatchers(HttpMethod.POST, "/api/items/**").hasAuthority(Permission.DATA_CREATE.name())
                        .requestMatchers(HttpMethod.PUT, "/api/items/**").hasAuthority(Permission.DATA_UPDATE.name())
                        .requestMatchers(HttpMethod.PATCH, "/api/items/**").hasAuthority(Permission.DATA_UPDATE.name())
                        .requestMatchers(HttpMethod.DELETE, "/api/items/**").hasAuthority(Permission.DATA_DELETE.name())
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        return http.build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Object authClaim = jwt.getClaims().get("auth");
            if (authClaim instanceof List<?> list) {
                return list.stream()
                        .filter(String.class::isInstance)
                        .map(String.class::cast)
                        .map(SimpleGrantedAuthority::new)
                        .map(GrantedAuthority.class::cast)
                        .toList();
            }
            return List.of();
        });
        return converter;
    }

    @Bean
    UserDetailsService userDetailsService(AppUserRepository appUserRepository) {
        return username -> appUserRepository.findByUsername(username)
                .map(user -> {
                    Set<GrantedAuthority> authorities = new HashSet<>();
                    authorities.addAll(user.getRole().getAuthorities().stream()
                            .map(a -> new SimpleGrantedAuthority(a.getName()))
                            .toList());
                    authorities.addAll(user.getAuthorities().stream()
                            .map(a -> new SimpleGrantedAuthority(a.getName()))
                            .toList());
                    authorities.add(new SimpleGrantedAuthority(user.getRole().authorityName()));

                    UserDetails details = User.withUsername(user.getUsername())
                            .password(user.getPasswordHash())
                            .authorities(authorities)
                            .build();
                    return details;
                })
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Bean
    DaoAuthenticationProvider daoAuthenticationProvider(UserDetailsService userDetailsService,
                                                        PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    JwtEncoder jwtEncoder(JwtProperties jwtProperties) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(hmacKey(jwtProperties)));
    }

    @Bean
    JwtDecoder jwtDecoder(JwtProperties jwtProperties) {
        return NimbusJwtDecoder.withSecretKey(hmacKey(jwtProperties)).build();
    }

    private SecretKey hmacKey(JwtProperties jwtProperties) {
        return new SecretKeySpec(jwtProperties.secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }
}
