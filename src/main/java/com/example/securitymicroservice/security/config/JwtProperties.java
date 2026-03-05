package com.example.securitymicroservice.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
/**
 * Application properties for JWT signing and token lifetime.
 *
 * @param secret shared HMAC secret used for HS256 signing
 * @param ttlSeconds token time-to-live in seconds
 */
public record JwtProperties(String secret, long ttlSeconds) {
}
