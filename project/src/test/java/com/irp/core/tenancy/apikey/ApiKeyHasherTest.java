package com.irp.core.tenancy.apikey;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiKeyHasherTest {

    private final ApiKeyHasher hasher = new ApiKeyHasher();

    @Test
    void generateProducesUniqueHighEntropyKeys() {
        String first = hasher.generate();
        String second = hasher.generate();

        assertThat(first).startsWith("irp_live_").isNotEqualTo(second);
    }

    @Test
    void hashIsDeterministicForTheSameInput() {
        String key = hasher.generate();

        assertThat(hasher.hash(key)).isEqualTo(hasher.hash(key));
    }

    @Test
    void hashDiffersForDifferentKeys() {
        assertThat(hasher.hash(hasher.generate())).isNotEqualTo(hasher.hash(hasher.generate()));
    }

    @Test
    void displayPrefixNeverExposesTheFullSecret() {
        String key = hasher.generate();

        String prefix = hasher.displayPrefix(key);

        assertThat(prefix).isNotEqualTo(key).hasSizeLessThan(key.length());
        assertThat(key).startsWith(prefix);
    }
}
