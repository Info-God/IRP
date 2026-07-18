package com.irp.core.ingestion;

import com.irp.core.ingestion.dto.DeploymentEventRequest;
import com.irp.core.ingestion.dto.ErrorEventRequest;
import com.irp.core.ingestion.dto.LogEventRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IngestionService {

    private final LogEventRepository logEventRepository;
    private final ErrorEventRepository errorEventRepository;
    private final DeploymentEventRepository deploymentEventRepository;

    @Transactional
    public int ingestLogs(UUID projectId, List<LogEventRequest> requests) {
        List<LogEvent> events = requests.stream()
                .map(r -> new LogEvent(projectId, r.occurredAt(), LogLevel.valueOf(r.level()),
                        r.service(), r.message(), r.traceId(), r.metadata()))
                .toList();
        logEventRepository.saveAll(events);
        return events.size();
    }

    @Transactional
    public int ingestErrors(UUID projectId, List<ErrorEventRequest> requests) {
        List<ErrorEvent> events = requests.stream()
                .map(r -> new ErrorEvent(projectId, r.occurredAt(), r.service(), r.exceptionType(),
                        r.message(), r.stackTrace(), stackHash(r.exceptionType(), r.stackTrace()), r.metadata()))
                .toList();
        errorEventRepository.saveAll(events);
        return events.size();
    }

    @Transactional
    public int ingestDeployment(UUID projectId, DeploymentEventRequest request) {
        deploymentEventRepository.save(new DeploymentEvent(projectId, request.occurredAt(), request.service(),
                request.version(), DeploymentStatus.valueOf(request.status()), request.metadata()));
        return 1;
    }

    /** Reads consumed by the AI agent service - see AgentController - to investigate an incident. */
    @Transactional(readOnly = true)
    public Page<LogEvent> searchLogs(UUID projectId, String service, String level, Instant from, Instant to, Pageable pageable) {
        LogLevel levelFilter = level == null ? null : LogLevel.valueOf(level);
        return logEventRepository.search(projectId, from, to, service, levelFilter, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ErrorEvent> searchErrors(UUID projectId, String service, String stackHash, Instant from, Instant to, Pageable pageable) {
        return errorEventRepository.search(projectId, from, to, service, stackHash, pageable);
    }

    @Transactional(readOnly = true)
    public Page<DeploymentEvent> searchDeployments(UUID projectId, String service, Instant from, Instant to, Pageable pageable) {
        return deploymentEventRepository.search(projectId, from, to, service, pageable);
    }

    /** Groups repeat occurrences of "the same" error together: exception type + first stack frame. */
    private String stackHash(String exceptionType, String stackTrace) {
        String firstFrame = stackTrace == null ? "" : stackTrace.lines().findFirst().orElse("");
        String basis = exceptionType + "|" + firstFrame;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(digest.digest(basis.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
