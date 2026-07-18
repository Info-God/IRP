package com.irp.sdk.core;

import com.irp.sdk.core.event.DeploymentEvent;
import com.irp.sdk.core.event.ErrorEvent;
import com.irp.sdk.core.event.IncidentEvent;
import com.irp.sdk.core.event.LogEvent;
import com.irp.sdk.core.transport.EventDispatcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

/**
 * Verifies two things about DefaultIncidentClient specifically: it builds the right
 * event for each method, and - the whole point of this SDK - it NEVER throws back into
 * the caller even when its internals are broken.
 */
@ExtendWith(MockitoExtension.class)
class DefaultIncidentClientTest {

    @Mock
    private EventDispatcher dispatcher;

    private final IncidentClientConfig config = IncidentClientConfig.builder()
            .apiKey("irp_live_test")
            .endpoint("http://localhost:8080/api/v1/ingest")
            .serviceName("checkout-service")
            .environment("test")
            .build();

    @Test
    void captureExceptionEnqueuesAnErrorEvent() {
        DefaultIncidentClient client = new DefaultIncidentClient(config, dispatcher);

        client.captureException(new IllegalStateException("boom"));

        ArgumentCaptor<IncidentEvent> captor = ArgumentCaptor.forClass(IncidentEvent.class);
        verify(dispatcher).enqueue(captor.capture());
        assertThat(captor.getValue()).isInstanceOf(ErrorEvent.class);
        ErrorEvent event = (ErrorEvent) captor.getValue();
        assertThat(event.service()).isEqualTo("checkout-service");
        assertThat(event.exceptionType()).isEqualTo("java.lang.IllegalStateException");
        assertThat(event.message()).isEqualTo("boom");
    }

    @Test
    void trackEventEnqueuesALogEventTaggedAsTrack() {
        DefaultIncidentClient client = new DefaultIncidentClient(config, dispatcher);

        client.trackEvent("payment_failed", Map.of("orderId", "123"));

        ArgumentCaptor<IncidentEvent> captor = ArgumentCaptor.forClass(IncidentEvent.class);
        verify(dispatcher).enqueue(captor.capture());
        LogEvent event = (LogEvent) captor.getValue();
        assertThat(event.message()).isEqualTo("payment_failed");
        assertThat(event.metadata()).containsEntry("orderId", "123");
    }

    @Test
    void markDeploymentEnqueuesADeploymentEventWithCommitHashInMetadata() {
        DefaultIncidentClient client = new DefaultIncidentClient(config, dispatcher);

        client.markDeployment("v1.4.2", "abc123");

        ArgumentCaptor<IncidentEvent> captor = ArgumentCaptor.forClass(IncidentEvent.class);
        verify(dispatcher).enqueue(captor.capture());
        DeploymentEvent event = (DeploymentEvent) captor.getValue();
        assertThat(event.version()).isEqualTo("v1.4.2");
        assertThat(event.status()).isEqualTo("SUCCEEDED");
        assertThat(event.metadata()).containsEntry("commitHash", "abc123");
    }

    @Test
    void sendHealthStatusEnqueuesAHealthTaggedLogEvent() {
        DefaultIncidentClient client = new DefaultIncidentClient(config, dispatcher);

        client.sendHealthStatus();

        ArgumentCaptor<IncidentEvent> captor = ArgumentCaptor.forClass(IncidentEvent.class);
        verify(dispatcher).enqueue(captor.capture());
        assertThat(captor.getValue()).isInstanceOf(LogEvent.class);
    }

    @Test
    void neverThrowsEvenWhenTheDispatcherIsBroken() {
        doThrow(new RuntimeException("dispatcher exploded")).when(dispatcher).enqueue(org.mockito.ArgumentMatchers.any());
        DefaultIncidentClient client = new DefaultIncidentClient(config, dispatcher);

        assertThatCode(() -> client.captureException(new RuntimeException("original error")))
                .doesNotThrowAnyException();
        assertThatCode(() -> client.trackEvent("name", Map.of()))
                .doesNotThrowAnyException();
        assertThatCode(() -> client.markDeployment("v1", "sha"))
                .doesNotThrowAnyException();
        assertThatCode(client::sendHealthStatus)
                .doesNotThrowAnyException();
    }

    @Test
    void nullThrowableIsIgnoredRatherThanCausingANullPointerException() {
        DefaultIncidentClient client = new DefaultIncidentClient(config, dispatcher);

        assertThatCode(() -> client.captureException(null)).doesNotThrowAnyException();
    }

    @Test
    void flushDelegatesToDispatcher() {
        DefaultIncidentClient client = new DefaultIncidentClient(config, dispatcher);

        client.flush(Duration.ofSeconds(1));

        verify(dispatcher).flush(Duration.ofSeconds(1));
    }
}
