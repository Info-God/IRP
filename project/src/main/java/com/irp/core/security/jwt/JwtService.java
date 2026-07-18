package com.irp.core.security.jwt;

import com.irp.core.security.principal.AuthenticatedUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {

    private static final String CLAIM_USER_ID = "uid";
    private static final String CLAIM_ORG_ID = "org";
    private static final String CLAIM_ROLE = "role";

    private final JwtProperties jwtProperties;

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(AuthenticatedUser user) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(jwtProperties.expirationMinutes() * 60);

        return Jwts.builder()
                .subject(user.getUsername())
                .issuer(jwtProperties.issuer())
                .claim(CLAIM_USER_ID, user.getId().toString())
                .claim(CLAIM_ORG_ID, user.getOrganizationId().toString())
                .claim(CLAIM_ROLE, user.getRole())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey())
                .compact();
    }

    public long expirationSeconds() {
        return jwtProperties.expirationMinutes() * 60;
    }

    /** Returns the token's subject (user email) if the signature and expiry are valid, empty otherwise. */
    public Optional<String> extractSubjectIfValid(String token) {
        try {
            Claims claims = parseClaims(token);
            return Optional.ofNullable(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(parseClaims(token).get(CLAIM_USER_ID, String.class));
    }

    public UUID extractOrganizationId(String token) {
        return UUID.fromString(parseClaims(token).get(CLAIM_ORG_ID, String.class));
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
