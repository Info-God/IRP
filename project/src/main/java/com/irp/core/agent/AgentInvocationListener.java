package com.irp.core.agent;

import com.irp.core.incident.IncidentCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Auto-triggers an AI investigation whenever an incident is created. Runs after the incident's
 * own transaction commits ({@link TransactionPhase#AFTER_COMMIT}) and off the request thread
 * ({@link Async}), so incident creation always returns immediately regardless of the AI
 * service's latency or availability.
 */
@Component
@RequiredArgsConstructor
public class AgentInvocationListener {

    private static final Logger log = LoggerFactory.getLogger(AgentInvocationListener.class);

    private final AiInvestigationClient aiInvestigationClient;
    private final AiServiceProperties properties;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onIncidentCreated(IncidentCreatedEvent event) {
        if (!properties.autoTriggerEnabled()) {
            return;
        }
        log.info("Auto-triggering AI investigation for incident {}", event.incidentId());
        aiInvestigationClient.triggerInvestigation(event.incidentId(), event.projectId(), event.organizationId());
    }
}
