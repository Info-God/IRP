package com.irp.sdk.core.event;

/**
 * Maps directly onto the three ingestion endpoints irp-core already exposes:
 * POST /api/v1/ingest/logs, /errors, /deployments. There is no fourth type for
 * trackEvent()/sendHealthStatus() on purpose - see LogEvent's factory methods.
 */
public enum EventType {
    LOG,
    ERROR,
    DEPLOYMENT
}
