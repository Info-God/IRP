package com.irp.sdk.core.util;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SensitiveDataMaskerTest {

    @Test
    void masksKnownSensitiveKeysCaseInsensitively() {
        Map<String, Object> input = Map.of(
                "password", "hunter2",
                "Authorization", "Bearer abc",
                "userApiKey", "irp_live_xyz",
                "orderId", "123"
        );

        Map<String, Object> masked = SensitiveDataMasker.mask(input);

        assertThat(masked.get("password")).isEqualTo("***REDACTED***");
        assertThat(masked.get("Authorization")).isEqualTo("***REDACTED***");
        assertThat(masked.get("userApiKey")).isEqualTo("***REDACTED***");
        assertThat(masked.get("orderId")).isEqualTo("123"); // untouched
    }

    @Test
    void handlesNullAndEmptyMetadataSafely() {
        assertThat(SensitiveDataMasker.mask(null)).isEmpty();
        assertThat(SensitiveDataMasker.mask(Map.of())).isEmpty();
    }
}
