package com.irp.core.tenancy.apikey;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * API keys are high-entropy random secrets, not user-chosen passwords, so a fast
 * deterministic SHA-256 hash is the right tool here - unlike password storage, there's
 * no need for a slow salted KDF (BCrypt) to defend against offline guessing.
 */
@Component
public class ApiKeyHasher {

    private static final String KEY_PREFIX = "irp_live_";
    private static final int SECRET_BYTES = 32;
    private static final int DISPLAY_PREFIX_LENGTH = 12;

    // SHA1PRNG seeds once from the OS then runs as a fast software PRNG - avoids the
    // platform-default SecureRandom blocking on OS entropy that hung /api/v1/auth/register
    // (see SecurityConfig.passwordEncoder for the full explanation).
    private final SecureRandom secureRandom = newSecureRandom();

    private static SecureRandom newSecureRandom() {
        try {
            return SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA1PRNG not available", e);
        }
    }

    /** Generates a brand-new plaintext API key, e.g. {@code irp_live_9f3a1b2c...}. Shown to the caller once. */
    public String generate() {
        byte[] randomBytes = new byte[SECRET_BYTES];
        secureRandom.nextBytes(randomBytes);
        String secret = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        return KEY_PREFIX + secret;
    }

    /** First few characters of the random part, safe to store and display for identification. */
    public String displayPrefix(String plaintextKey) {
        String withoutPrefix = plaintextKey.startsWith(KEY_PREFIX)
                ? plaintextKey.substring(KEY_PREFIX.length())
                : plaintextKey;
        int end = Math.min(DISPLAY_PREFIX_LENGTH, withoutPrefix.length());
        return KEY_PREFIX + withoutPrefix.substring(0, end);
    }

    public String hash(String plaintextKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(plaintextKey.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
