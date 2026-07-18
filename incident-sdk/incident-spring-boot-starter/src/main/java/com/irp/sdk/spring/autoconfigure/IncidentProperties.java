package com.irp.sdk.spring.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * Binds the "incident.*" namespace from application.yml. Only apiKey, endpoint, and
 * serviceName have no default - everything else has a sensible one so a developer can
 * add the starter and three lines of config and be done.
 */
@ConfigurationProperties(prefix = "incident")
public record IncidentProperties(
        @DefaultValue("true") boolean enabled,
        String apiKey,
        String endpoint,
        String serviceName,
        @DefaultValue("production") String environment,
        @DefaultValue Queue queue,
        @DefaultValue Retry retry,
        @DefaultValue LatencyCapture latencyCapture,
        @DefaultValue ErrorCapture errorCapture,
        @DefaultValue Health health
) {

    public record Queue(
            @DefaultValue("2000") int capacity,
            @DefaultValue("50") int batchSize,
            @DefaultValue("2s") Duration flushInterval
    ) {
    }

    public record Retry(
            @DefaultValue("3") int maxAttempts,
            @DefaultValue("200ms") Duration initialBackoff,
            @DefaultValue("5s") Duration maxBackoff
    ) {
    }

    public record LatencyCapture(
            @DefaultValue("true") boolean enabled,
            /** Only report requests slower than this. 0 = report every request. */
            @DefaultValue("0ms") Duration slowThreshold
    ) {
    }

    public record ErrorCapture(
            @DefaultValue("true") boolean enabled
    ) {
    }

    public record Health(
            @DefaultValue AutoReport autoReport
    ) {
        public record AutoReport(
                @DefaultValue("false") boolean enabled,
                @DefaultValue("60s") Duration interval
        ) {
        }
    }
}
