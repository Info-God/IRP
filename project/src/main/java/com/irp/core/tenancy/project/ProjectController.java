package com.irp.core.tenancy.project;

import com.irp.core.security.principal.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse createProject(@AuthenticationPrincipal AuthenticatedUser currentUser,
                                          @Valid @RequestBody CreateProjectRequest request) {
        Project project = projectService.createProject(currentUser.getOrganizationId(), currentUser.getUsername(), request);
        return ProjectResponse.from(project);
    }

    @GetMapping
    public List<ProjectResponse> listProjects(@AuthenticationPrincipal AuthenticatedUser currentUser) {
        return projectService.listProjects(currentUser.getOrganizationId()).stream()
                .map(ProjectResponse::from)
                .toList();
    }

    @GetMapping("/{projectId}")
    public ProjectResponse getProject(@AuthenticationPrincipal AuthenticatedUser currentUser,
                                       @PathVariable UUID projectId) {
        return ProjectResponse.from(projectService.getProject(currentUser.getOrganizationId(), projectId));
    }
}
