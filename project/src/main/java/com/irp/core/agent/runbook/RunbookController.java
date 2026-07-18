package com.irp.core.agent.runbook;

import com.irp.core.agent.runbook.dto.CreateRunbookRequest;
import com.irp.core.agent.runbook.dto.RunbookResponse;
import com.irp.core.security.principal.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** Dashboard-facing: upload and list runbooks (JWT-only, see SecurityConfig). */
@RestController
@RequestMapping("/api/v1/projects/{projectId}/runbooks")
@RequiredArgsConstructor
public class RunbookController {

    private final RunbookService runbookService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RunbookResponse createRunbook(@AuthenticationPrincipal AuthenticatedUser currentUser,
                                          @PathVariable UUID projectId,
                                          @Valid @RequestBody CreateRunbookRequest request) {
        return RunbookResponse.from(runbookService.createRunbook(
                currentUser.getOrganizationId(), projectId, currentUser.getUsername(), request));
    }

    @GetMapping
    public List<RunbookResponse> listRunbooks(@AuthenticationPrincipal AuthenticatedUser currentUser,
                                               @PathVariable UUID projectId) {
        return runbookService.listRunbooks(currentUser.getOrganizationId(), projectId).stream()
                .map(RunbookResponse::from)
                .toList();
    }
}
