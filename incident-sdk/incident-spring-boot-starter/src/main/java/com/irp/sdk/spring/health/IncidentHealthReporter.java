package com.irp.sdk.spring.health;

import com.irp.sdk.core.IncidentClient;
import org.springframework.context.SmartLifecycle;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Optional scheduled heartbeat (incident.health.auto-report.enabled=true). Runs its own
 * daemon-thread scheduler rather than relying on the host application's @EnableScheduling -
 * a library shouldn't assume the consumer has scheduling wired up at all.
 */
public class IncidentHealthReporter implements SmartLifecycle {

    private final IncidentClient client;
    private final Duration interval;
    private ScheduledExecutorService scheduler;
    private volatile boolean running;

    public IncidentHealthReporter(IncidentClient client, Duration interval) {
        this.client = client;
        this.interval = interval;
    }

    @Override
    public void start() {
        scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "incident-sdk-health-reporter");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleAtFixedRate(client::sendHealthStatus, 0, interval.toMillis(), TimeUnit.MILLISECONDS);
        running = true;
    }

    @Override
    public void stop() {
        running = false;
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
