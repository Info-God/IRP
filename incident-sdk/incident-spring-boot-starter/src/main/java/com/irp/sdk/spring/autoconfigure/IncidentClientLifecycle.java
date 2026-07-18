package com.irp.sdk.spring.autoconfigure;

import com.irp.sdk.core.DefaultIncidentClient;
import com.irp.sdk.core.IncidentClient;
import org.springframework.context.SmartLifecycle;

import java.time.Duration;

/**
 * Ensures events queued right before the application shuts down are actually flushed
 * instead of silently dropped. DefaultIncidentClient already starts its own worker
 * thread in its constructor, so start() here is a no-op marker - stop() is the part
 * that matters.
 */
class IncidentClientLifecycle implements SmartLifecycle {

    private static final Duration SHUTDOWN_FLUSH_TIMEOUT = Duration.ofSeconds(3);

    private final IncidentClient client;
    private volatile boolean running;

    IncidentClientLifecycle(IncidentClient client) {
        this.client = client;
    }

    @Override
    public void start() {
        running = true;
    }

    @Override
    public void stop() {
        running = false;
        if (client instanceof DefaultIncidentClient defaultClient) {
            defaultClient.stop(SHUTDOWN_FLUSH_TIMEOUT);
        } else {
            client.flush(SHUTDOWN_FLUSH_TIMEOUT);
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    /** Flush before web servers/connection pools shut down, not after. */
    @Override
    public int getPhase() {
        return Integer.MAX_VALUE - 100;
    }
}
