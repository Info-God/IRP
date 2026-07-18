package com.irp.core.agent;

import com.irp.core.agent.dto.CreateAgentStepRequest;
import com.irp.core.agent.dto.FinishAgentRunRequest;
import com.irp.core.common.exception.ResourceNotFoundException;
import com.irp.core.incident.IncidentRepository;
import com.irp.core.tenancy.project.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AgentRunService {

    private final AgentRunRepository agentRunRepository;
    private final AgentStepRepository agentStepRepository;
    private final IncidentRepository incidentRepository;
    private final ProjectService projectService;

    @Transactional
    public AgentRun startRun(UUID projectId, UUID incidentId, String model) {
        getIncidentForProject(projectId, incidentId);
        return agentRunRepository.save(new AgentRun(incidentId, projectId, model));
    }

    /** stepIndex is server-assigned (the count of steps already recorded for this run),
     * not client-supplied - keeps ordering correct even if the caller retries a request. */
    @Transactional
    public AgentStep addStep(UUID projectId, UUID runId, CreateAgentStepRequest request) {
        AgentRun run = getRunForProject(projectId, runId);
        int nextIndex = agentStepRepository.countByAgentRunId(run.getId());
        return agentStepRepository.save(new AgentStep(
                run.getId(), nextIndex, request.toolName(), request.toolInput(), request.toolOutput()));
    }

    @Transactional
    public AgentRun finishRun(UUID projectId, UUID runId, FinishAgentRunRequest request) {
        AgentRun run = getRunForProject(projectId, runId);
        run.finish(AgentRunStatus.valueOf(request.status()), request.tokenUsage());
        return agentRunRepository.save(run);
    }

    @Transactional(readOnly = true)
    public List<AgentRun> listRuns(UUID organizationId, UUID projectId, UUID incidentId) {
        projectService.getProject(organizationId, projectId);
        getIncidentForProject(projectId, incidentId);
        return agentRunRepository.findByIncidentIdOrderByStartedAtDesc(incidentId);
    }

    @Transactional(readOnly = true)
    public List<AgentStep> listSteps(UUID agentRunId) {
        return agentStepRepository.findByAgentRunIdOrderByStepIndexAsc(agentRunId);
    }

    private void getIncidentForProject(UUID projectId, UUID incidentId) {
        incidentRepository.findByIdAndProjectId(incidentId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", incidentId));
    }

    private AgentRun getRunForProject(UUID projectId, UUID runId) {
        return agentRunRepository.findByIdAndProjectId(runId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException("AgentRun", runId));
    }
}
