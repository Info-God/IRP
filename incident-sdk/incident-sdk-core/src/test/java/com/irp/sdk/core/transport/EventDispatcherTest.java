package com.irp.sdk.core.transport;

import com.irp.sdk.core.IncidentClientConfig;
import com.irp.sdk.core.event.EventType;
import com.irp.sdk.core.event.LogEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EventDispatcherTest {

    @Mock
    private HttpSender sender;

    @Test
    void enqueueNeverBlocksAndDropsOnceQueueIsFull() {
        IncidentClientConfig config = testConfig(1); // capacity of exactly 1, worker never started
        EventDispatcher dispatcher = new EventDispatcher(config, sender);

        dispatcher.enqueue(logEvent());
        dispatcher.enqueue(logEvent()); // queue full - must be dropped, not block
        dispatcher.enqueue(logEvent());

        assertThat(dispatcher.droppedCount()).isEqualTo(2);
    }

    @Test
    void startedDispatcherFlushesQueuedEventsToTheSender() throws InterruptedException {
        IncidentClientConfig config = testConfig(100);
        EventDispatcher dispatcher = new EventDispatcher(config, sender);
        dispatcher.start();

        dispatcher.enqueue(logEvent());
        dispatcher.enqueue(logEvent());

        waitUntil(Duration.ofSeconds(2), () -> dispatcher.lastSuccessfulSend() != null);

        verify(sender).sendBatch(eq(EventType.LOG), any());
        dispatcher.stop(Duration.ofSeconds(1));
    }

    private void waitUntil(Duration timeout, java.util.function.BooleanSupplier condition) throws InterruptedException {
        Instant deadline = Instant.now().plus(timeout);
        while (!condition.getAsBoolean()) {
            if (Instant.now().isAfter(deadline)) {
                throw new AssertionError("Condition not met within " + timeout);
            }
            Thread.sleep(20);
        }
    }

    private LogEvent logEvent() {
        return LogEvent.of("test-service", "INFO", "hello", null, Map.of());
    }

    private IncidentClientConfig testConfig(int capacity) {
        return IncidentClientConfig.builder()
                .apiKey("irp_live_test")
                .endpoint("http://localhost:0")
                .serviceName("test-service")
                .queueCapacity(capacity)
                .batchSize(10)
                .flushInterval(Duration.ofMillis(100))
                .build();
    }
}
