package com.irp.core.incident.signals;

import com.irp.core.incident.signals.dto.RelatedSignalsResponse;
import com.irp.core.security.principal.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Dashboard-facing: logs/errors/deployments around one incident's opened_at, for the
 * Related Signals tab (JWT-only, see SecurityConfig). */
@RestController
@RequestMapping("/api/v1/projects/{projectId}/signals")
@RequiredArgsConstructor
public class SignalsController {

    private final SignalsService signalsService;

    @GetMapping
    public RelatedSignalsResponse getRelatedSignals(@AuthenticationPrincipal AuthenticatedUser currentUser,
                                                      @PathVariable UUID projectId,
                                                      @RequestParam UUID incidentId,
                                                      @RequestParam(defaultValue = "30") int windowMinutes) {
        return signalsService.getRelatedSignals(currentUser.getOrganizationId(), projectId, incidentId, windowMinutes);
    }
}
