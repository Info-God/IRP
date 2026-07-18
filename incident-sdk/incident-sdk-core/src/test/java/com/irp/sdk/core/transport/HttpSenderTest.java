package com.irp.sdk.core.transport;

import com.irp.sdk.core.IncidentClientConfig;
import com.irp.sdk.core.event.DeploymentEvent;
import com.irp.sdk.core.event.ErrorEvent;
import com.irp.sdk.core.event.EventType;
import com.irp.sdk.core.event.IncidentEvent;
import com.irp.sdk.core.event.LogEvent;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs HttpSender against a real (if tiny) HTTP server using only
 * com.sun.net.httpserver.HttpServer, built into the JDK - no WireMock or other test
 * dependency needed just to prove the wire format irp-core actually receives is correct.
 */
class HttpSenderTest {

    private HttpServer server;
    private final List<RecordedRequest> recordedRequests = new ArrayList<>();

    private record RecordedRequest(String path, String method, String apiKeyHeader, String body) {
    }

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void logAndErrorEventsAreSentAsABatchWrapperMatchingIrpCoresDtoShape() throws IOException {
        startServerRespondingWith(200);
        HttpSender sender = senderFor(server);

        sender.sendBatch(EventType.LOG, List.of(
                LogEvent.of("checkout", "INFO", "started", null, Map.of()),
                LogEvent.of("checkout", "ERROR", "failed", null, Map.of())));

        assertThat(recordedRequests).hasSize(1);
        RecordedRequest request = recordedRequests.get(0);
        assertThat(request.path()).isEqualTo("/logs");
        assertThat(request.method()).isEqualTo("POST");
        assertThat(request.apiKeyHeader()).isEqualTo("irp_live_test");
        assertThat(request.body()).contains("\"events\":[").contains("\"service\":\"checkout\"");
    }

    @Test
    void errorEventsGoToTheErrorsPath() throws IOException {
        startServerRespondingWith(200);
        HttpSender sender = senderFor(server);

        sender.sendBatch(EventType.ERROR, List.of(
                ErrorEvent.from(new RuntimeException("boom"), "checkout", Map.of())));

        assertThat(recordedRequests.get(0).path()).isEqualTo("/errors");
    }

    @Test
    void deploymentEventsAreSentOneAtATimeNotAsABatch() throws IOException {
        // irp-core's /ingest/deployments endpoint takes ONE object per request, not a list -
        // this is the exact bug this test exists to catch.
        startServerRespondingWith(200);
        HttpSender sender = senderFor(server);

        sender.sendBatch(EventType.DEPLOYMENT, List.of(
                DeploymentEvent.of("v1.0", "sha1", "checkout"),
                DeploymentEvent.of("v1.1", "sha2", "checkout")));

        assertThat(recordedRequests).hasSize(2);
        recordedRequests.forEach(r -> {
            assertThat(r.path()).isEqualTo("/deployments");
            assertThat(r.body()).doesNotContain("\"events\":["); // a single object, not a batch wrapper
            assertThat(r.body()).contains("\"status\":\"SUCCEEDED\"");
        });
    }

    @Test
    void retriesOnServerErrorAndEventuallySucceeds() throws IOException {
        AtomicInteger callCount = new AtomicInteger();
        startServerWithHandler(exchange -> {
            int call = callCount.incrementAndGet();
            return call < 3 ? 503 : 200; // fail twice, then succeed
        });
        HttpSender sender = senderFor(server, 5, Duration.ofMillis(5));

        sender.sendBatch(EventType.LOG, List.of(LogEvent.of("checkout", "INFO", "msg", null, Map.of())));

        assertThat(callCount.get()).isEqualTo(3);
    }

    @Test
    void doesNotRetryOn401Unauthorized() throws IOException {
        AtomicInteger callCount = new AtomicInteger();
        startServerWithHandler(exchange -> {
            callCount.incrementAndGet();
            return 401;
        });
        HttpSender sender = senderFor(server, 5, Duration.ofMillis(5));

        sender.sendBatch(EventType.LOG, List.of(LogEvent.of("checkout", "INFO", "msg", null, Map.of())));

        assertThat(callCount.get()).isEqualTo(1); // never retried
    }

    @Test
    void doesNotRetryOnOtherClientErrors() throws IOException {
        AtomicInteger callCount = new AtomicInteger();
        startServerWithHandler(exchange -> {
            callCount.incrementAndGet();
            return 400;
        });
        HttpSender sender = senderFor(server, 5, Duration.ofMillis(5));

        sender.sendBatch(EventType.LOG, List.of(LogEvent.of("checkout", "INFO", "msg", null, Map.of())));

        assertThat(callCount.get()).isEqualTo(1);
    }

    private void startServerRespondingWith(int status) throws IOException {
        startServerWithHandler(exchange -> status);
    }

    private interface StatusHandler {
        int statusFor(HttpExchange exchange);
    }

    private void startServerWithHandler(StatusHandler handler) throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", exchange -> {
            String body = new String(exchange.getRequestBody().readAllBytes());
            recordedRequests.add(new RecordedRequest(
                    exchange.getRequestURI().getPath(),
                    exchange.getRequestMethod(),
                    exchange.getRequestHeaders().getFirst("X-API-Key"),
                    body));
            int status = handler.statusFor(exchange);
            exchange.sendResponseHeaders(status, -1);
            exchange.close();
        });
        server.start();
    }

    private HttpSender senderFor(HttpServer server) {
        return senderFor(server, 3, Duration.ofMillis(50));
    }

    private HttpSender senderFor(HttpServer server, int maxAttempts, Duration initialBackoff) {
        IncidentClientConfig config = IncidentClientConfig.builder()
                .apiKey("irp_live_test")
                .endpoint("http://localhost:" + server.getAddress().getPort())
                .serviceName("checkout")
                .connectTimeout(Duration.ofSeconds(1))
                .readTimeout(Duration.ofSeconds(1))
                .maxRetryAttempts(maxAttempts)
                .initialBackoff(initialBackoff)
                .maxBackoff(Duration.ofMillis(200))
                .build();
        RetryPolicy retryPolicy = new RetryPolicy(config.maxRetryAttempts(), config.initialBackoff(), config.maxBackoff());
        return new HttpSender(config, retryPolicy);
    }
}
