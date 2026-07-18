package com.irp.sdk.core.transport;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class RetryPolicyTest {

    private final RetryPolicy policy = new RetryPolicy(3, Duration.ofMillis(100), Duration.ofSeconds(2));

    @Test
    void serverErrorsAreRetryable() {
        assertThat(policy.isRetryable(500)).isTrue();
        assertThat(policy.isRetryable(503)).isTrue();
    }

    @Test
    void unauthorizedIsNeverRetryable() {
        assertThat(policy.isRetryable(401)).isFalse();
    }

    @Test
    void otherClientErrorsAreNeverRetryable() {
        assertThat(policy.isRetryable(400)).isFalse();
        assertThat(policy.isRetryable(404)).isFalse();
        assertThat(policy.isRetryable(422)).isFalse();
    }

    @Test
    void backoffGrowsExponentiallyAndRespectsTheCap() {
        Duration first = policy.backoffFor(1);
        Duration second = policy.backoffFor(2);
        Duration farFuture = policy.backoffFor(20);

        assertThat(first.toMillis()).isGreaterThanOrEqualTo(200).isLessThan(300);
        assertThat(second.toMillis()).isGreaterThanOrEqualTo(400).isLessThan(500);
        assertThat(farFuture.toMillis()).isLessThanOrEqualTo(2100); // capped at maxBackoff + jitter
    }
}
