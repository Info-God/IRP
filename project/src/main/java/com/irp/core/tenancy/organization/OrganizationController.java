package com.irp.core.tenancy.organization;

import com.irp.core.security.principal.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Read-only for Phase 1: an organization is created only via /api/v1/auth/register. */
@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @GetMapping("/me")
    public OrganizationResponse getMyOrganization(@AuthenticationPrincipal AuthenticatedUser currentUser) {
        return OrganizationResponse.from(organizationService.getById(currentUser.getOrganizationId()));
    }
}
