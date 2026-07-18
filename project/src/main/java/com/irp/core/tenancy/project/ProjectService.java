package com.irp.core.tenancy.project;

import com.irp.core.common.audit.AuditService;
import com.irp.core.common.exception.ConflictException;
import com.irp.core.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final AuditService auditService;

    @Transactional
    public Project createProject(UUID organizationId, String actor, CreateProjectRequest request) {
        if (projectRepository.existsByOrganizationIdAndName(organizationId, request.name())) {
            throw new ConflictException("A project named '%s' already exists in this organization".formatted(request.name()));
        }

        Project project = projectRepository.save(new Project(organizationId, request.name(), request.environment()));

        auditService.record(organizationId, project.getId(), actor, "PROJECT_CREATED", "Project", project.getId().toString(),
                Map.of("name", project.getName(), "environment", project.getEnvironment()));

        return project;
    }

    @Transactional(readOnly = true)
    public List<Project> listProjects(UUID organizationId) {
        return projectRepository.findByOrganizationIdOrderByCreatedAtDesc(organizationId);
    }

    @Transactional(readOnly = true)
    public Project getProject(UUID organizationId, UUID projectId) {
        return projectRepository.findByIdAndOrganizationId(projectId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", projectId));
    }
}
