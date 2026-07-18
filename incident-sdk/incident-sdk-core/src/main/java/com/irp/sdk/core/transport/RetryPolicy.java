package com.irp.sdk.core.transport;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Decides two independent things: whether a given failure is worth retrying at all,
 * and how long to wait before the next attempt. Kept separate from HttpSender so both
 * rules can be unit tested without any actual HTTP involved.
 */
public final class RetryPolicy {

    private final int maxAttempts;
    private final Duration initialBackoff;
    private final Duration maxBackoff;

    public RetryPolicy(int maxAttempts, Duration initialBackoff, Duration maxBackoff) {
        this.maxAttempts = maxAttempts;
        this.initialBackoff = initialBackoff;
        this.maxBackoff = maxBackoff;
    }

    public int maxAttempts() {
        return maxAttempts;
    }

    /**
     * 401/other 4xx will never succeed on retry (bad key, bad payload shape) - only
     * network failures and 5xx responses are transient and worth retrying.
     */
    public boolean isRetryable(int httpStatusCode) {
        return httpStatusCode >= 500;
    }

    public boolean isRetryable(Exception networkException) {
        return true;
    }

    /** Exponential backoff with jitter, capped at maxBackoff, so many instances of the same
     *  service retrying at once don't all hammer the platform back in perfect unison. */
    public Duration backoffFor(int attemptNumber) {
        long exponential = initialBackoff.toMillis() * (1L << Math.min(attemptNumber, 20));
        long capped = Math.min(exponential, maxBackoff.toMillis());
        long jitterMillis = ThreadLocalRandom.current().nextLong(100);
        return Duration.ofMillis(capped + jitterMillis);
    }
}
