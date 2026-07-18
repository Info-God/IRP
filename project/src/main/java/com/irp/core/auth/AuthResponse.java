package com.irp.core.auth;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        UserSummary user
) {
    public static AuthResponse of(String accessToken, long expiresInSeconds, UserSummary user) {
        return new AuthResponse(accessToken, "Bearer", expiresInSeconds, user);
    }
}
