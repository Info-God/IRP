package com.irp.sdk.core.transport;

import com.irp.sdk.core.IncidentClientConfig;
import com.irp.sdk.core.event.EventType;
import com.irp.sdk.core.event.IncidentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The only place a caller's thread touches this SDK's internals: {@link #enqueue} does
 * one cheap, non-blocking queue offer and returns. Everything expensive - batching,
 * HTTP, retries - happens on a single dedicated background thread, entirely off the
 * request/application thread that produced the event.
 */
public final class EventDispatcher {

    private static final Logger log = LoggerFactory.getLogger(EventDispatcher.class);

    private final BlockingQueue<IncidentEvent> queue;
    private final HttpSender sender;
    private final IncidentClientConfig config;
    private final ExecutorService worker;
    private final AtomicLong droppedCount = new AtomicLong();
    private final AtomicReference<Instant> lastSuccessfulSend = new AtomicReference<>();
    private volatile boolean running;

    public EventDispatcher(IncidentClientConfig config, HttpSender sender) {
        this.config = config;
        this.sender = sender;
        this.queue = new ArrayBlockingQueue<>(config.queueCapacity());
        this.worker = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "incident-sdk-dispatcher");
            thread.setDaemon(true); // never blocks JVM shutdown on its own
            return thread;
        });
    }

    /** Never blocks, never throws. Safe to call from a request-handling thread. */
    public void enqueue(IncidentEvent event) {
        if (event == null) {
            return;
        }
        if (!queue.offer(event)) {
            long total = droppedCount.incrementAndGet();
            if (total == 1 || total % 500 == 0) {
                log.warn("Incident SDK: event queue is full (capacity {}) - dropped {} event(s) so far. " +
                        "The platform may be unreachable or too slow; the application itself is unaffected.",
                        config.queueCapacity(), total);
            }
        }
    }

    public void start() {
        if (running) {
            return;
        }
        running = true;
        worker.submit(this::runLoop);
    }

    private void runLoop() {
        while (running || !queue.isEmpty()) {
            List<IncidentEvent> batch = drainOneBatch();
            if (batch.isEmpty()) {
                continue;
            }
            sendGroupedByType(batch);
        }
    }

    private List<IncidentEvent> drainOneBatch() {
        List<IncidentEvent> batch = new ArrayList<>(config.batchSize());
        IncidentEvent first = pollUpTo(config.flushInterval());
        if (first == null) {
            return batch;
        }
        batch.add(first);
        queue.drainTo(batch, config.batchSize() - 1);
        return batch;
    }

    private IncidentEvent pollUpTo(Duration timeout) {
        try {
            return queue.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    private void sendGroupedByType(List<IncidentEvent> batch) {
        Map<EventType, List<IncidentEvent>> byType = new EnumMap<>(EventType.class);
        for (IncidentEvent event : batch) {
            byType.computeIfAbsent(event.type(), t -> new ArrayList<>()).add(event);
        }
        byType.forEach((type, events) -> {
            sender.sendBatch(type, events);
            lastSuccessfulSend.set(Instant.now()); // sendBatch never throws; see HttpSender
        });
    }

    /** Drains whatever is left synchronously, bounded by timeout - used on graceful shutdown
     *  so events queued right before the app stops aren't silently lost. */
    public void flush(Duration timeout) {
        Instant deadline = Instant.now().plus(timeout);
        while (!queue.isEmpty() && Instant.now().isBefore(deadline)) {
            List<IncidentEvent> batch = new ArrayList<>(config.batchSize());
            queue.drainTo(batch, config.batchSize());
            if (!batch.isEmpty()) {
                sendGroupedByType(batch);
            }
        }
    }

    public void stop(Duration flushTimeout) {
        running = false;
        flush(flushTimeout);
        worker.shutdown();
    }

    public boolean isRunning() {
        return running;
    }

    public long droppedCount() {
        return droppedCount.get();
    }

    public Instant lastSuccessfulSend() {
        return lastSuccessfulSend.get();
    }
}
