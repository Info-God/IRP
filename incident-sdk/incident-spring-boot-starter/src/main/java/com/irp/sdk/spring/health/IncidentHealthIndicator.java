package com.irp.sdk.spring.health;

import com.irp.sdk.core.DefaultIncidentClient;
import com.irp.sdk.core.IncidentClient;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import java.time.Duration;
import java.time.Instant;

/**
 * Surfaces the SDK's OWN connectivity as a component under /actuator/health, separate
 * from whatever sendHealthStatus() reports to the platform. If the platform has been
 * unreachable for a while, or the internal queue has been dropping events, that shows
 * up here for the host application's own ops tooling to see - without ever failing the
 * application's overall health status (this is reported as an additional detail, not
 * wired to bring the whole app down over a telemetry outage).
 */
public class IncidentHealthIndicator implements HealthIndicator {

    private static final Duration STALE_AFTER = Duration.ofMinutes(5);

    private final IncidentClient client;

    public IncidentHealthIndicator(IncidentClient client) {
        this.client = client;
    }

    @Override
    public Health health() {
        Health.Builder builder = Health.up()
                .withDetail("droppedEvents", client.droppedEventCount());

        if (client instanceof DefaultIncidentClient defaultClient) {
            Instant lastSend = defaultClient.lastSuccessfulSend();
            builder.withDetail("lastSuccessfulSend", lastSend == null ? "never" : lastSend.toString());

            if (lastSend != null && Duration.between(lastSend, Instant.now()).compareTo(STALE_AFTER) > 0) {
                builder.status("DEGRADED").withDetail("reason", "No successful send in over 5 minutes");
            }
        }

        return builder.build();
    }
}
