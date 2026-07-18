package com.irp.core.common.audit;

import com.irp.core.common.web.PageResponse;
import com.irp.core.security.principal.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

/** Read-only - nothing in this codebase ever mutates an audit log entry once written. */
@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditService auditService;

    @GetMapping
    public PageResponse<AuditLogResponse> listAuditLogs(@AuthenticationPrincipal AuthenticatedUser currentUser,
                                                          @RequestParam(required = false) UUID projectId,
                                                          @PageableDefault(size = 50) Pageable pageable) {
        return PageResponse.from(auditService
                .list(currentUser.getOrganizationId(), Optional.ofNullable(projectId), pageable)
                .map(AuditLogResponse::from));
    }
}
