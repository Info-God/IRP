package com.irp.core.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "irp.jwt")
public record JwtProperties(
        String secret,
        long expirationMinutes,
        String issuer
) {
}
