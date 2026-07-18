package com.irp.sdk.core.transport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.irp.sdk.core.IncidentClientConfig;
import com.irp.sdk.core.event.DeploymentEvent;
import com.irp.sdk.core.event.EventType;
import com.irp.sdk.core.event.IncidentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

/**
 * Uses java.net.http.HttpClient (built into the JDK since 11) instead of WebClient or
 * RestTemplate on purpose: incident-sdk-core has zero Spring dependency, and pulling in
 * a reactive stack just to POST a JSON batch every couple of seconds would be a heavy,
 * unnecessary dependency for every consumer of this module - including non-Spring ones.
 *
 * <p>Every public method here catches everything and never throws - see EventDispatcher,
 * which is the only caller and depends on that guarantee to keep its worker thread alive
 * forever regardless of how badly the platform is behaving.
 */
public final class HttpSender {

    private static final Logger log = LoggerFactory.getLogger(HttpSender.class);

    private final IncidentClientConfig config;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final RetryPolicy retryPolicy;

    public HttpSender(IncidentClientConfig config, RetryPolicy retryPolicy) {
        this.config = config;
        this.retryPolicy = retryPolicy;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(config.connectTimeout())
                .build();
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Sends one same-type batch. LOG and ERROR events go to irp-core's batch endpoints
     * as {"events": [...]}; DEPLOYMENT events go one at a time because irp-core's
     * /ingest/deployments endpoint accepts a single object per request, not a list -
     * batching those would produce a JSON array where irp-core expects an object and
     * every deployment event in the batch would fail deserialization.
     */
    public void sendBatch(EventType type, List<IncidentEvent> events) {
        if (events.isEmpty()) {
            return;
        }
        try {
            if (type == EventType.DEPLOYMENT) {
                for (IncidentEvent event : events) {
                    sendWithRetry(pathFor(type), serialize(event));
                }
            } else {
                sendWithRetry(pathFor(type), serialize(new EventsBatch(events)));
            }
        } catch (Exception unexpected) {
            // Belt-and-braces: nothing above should throw, but a transport bug must
            // never become an application-crashing bug for whoever installed this SDK.
            log.warn("Incident SDK: unexpected error sending {} batch of {} events, dropping it.",
                    type, events.size(), unexpected);
        }
    }

    private void sendWithRetry(String path, String jsonBody) {
        int attempt = 0;
        while (true) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(config.endpoint() + path))
                        .timeout(config.readTimeout())
                        .header("Content-Type", "application/json")
                        .header("X-API-Key", config.apiKey())
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

                HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());

                if (response.statusCode() < 300) {
                    return; // success
                }
                if (response.statusCode() == 401) {
                    log.error("Incident SDK: API key rejected (401) by {} - check the 'incident.api-key' " +
                            "configuration. Not retrying this or future batches will keep failing identically.", path);
                    return;
                }
                if (!retryPolicy.isRetryable(response.statusCode())) {
                    log.warn("Incident SDK: {} rejected the batch with status {} - dropping it (not retryable).",
                            path, response.statusCode());
                    return;
                }
                log.debug("Incident SDK: {} returned {} (attempt {}/{}), will retry.",
                        path, response.statusCode(), attempt + 1, retryPolicy.maxAttempts());

            } catch (IOException | InterruptedException networkFailure) {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                log.debug("Incident SDK: network error sending to {} (attempt {}/{}): {}",
                        path, attempt + 1, retryPolicy.maxAttempts(), networkFailure.toString());
            }

            attempt++;
            if (attempt >= retryPolicy.maxAttempts()) {
                log.warn("Incident SDK: giving up on a batch to {} after {} attempts.", path, attempt);
                return;
            }
            sleepQuietly(retryPolicy.backoffFor(attempt));
        }
    }

    private void sleepQuietly(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String serialize(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            // Should be unreachable - every event type is a plain record of primitives/maps -
            // but if it ever happens, fail closed (drop) rather than throw into the caller.
            log.warn("Incident SDK: failed to serialize an event, dropping it.", e);
            return "{}";
        }
    }

    private static String pathFor(EventType type) {
        return switch (type) {
            case LOG -> "/logs";
            case ERROR -> "/errors";
            case DEPLOYMENT -> "/deployments";
        };
    }

    private record EventsBatch(List<? extends IncidentEvent> events) {
    }
}
