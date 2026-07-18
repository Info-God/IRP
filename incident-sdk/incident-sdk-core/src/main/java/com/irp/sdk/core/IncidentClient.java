package com.irp.sdk.core;

import java.time.Duration;
import java.util.Map;

/**
 * The only type application code should depend on. Every method is fire-and-forget:
 * it returns immediately, never throws, and never blocks the calling thread on network I/O.
 */
public interface IncidentClient {

    void captureException(Throwable throwable);

    void captureException(Throwable throwable, Map<String, Object> metadata);

    void trackEvent(String name, Map<String, Object> metadata);

    void markDeployment(String version, String commitHash);

    void sendHealthStatus();

    /** Blocks up to {@code timeout} waiting for queued events to actually send - call this
     *  on graceful shutdown, not from request-handling code. */
    void flush(Duration timeout);

    /** Number of events dropped because the internal queue was full. Exposed for the
     *  Spring starter's actuator health indicator. */
    long droppedEventCount();
}
