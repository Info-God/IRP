package com.irp.sdk.core.event;

import java.time.Instant;

/**
 * Common shape for everything the dispatcher queues. Sealed so HttpSender's
 * type-based routing (LOG/ERROR -> batched, DEPLOYMENT -> single-send) is a switch
 * the compiler can check is exhaustive.
 */
public sealed interface IncidentEvent permits LogEvent, ErrorEvent, DeploymentEvent {

    EventType type();

    Instant occurredAt();

    String service();
}
