package com.irp.core.security.apikey;

import com.irp.core.tenancy.apikey.ApiKey;
import com.irp.core.tenancy.apikey.ApiKeyHasher;
import com.irp.core.tenancy.apikey.ApiKeyRepository;
import com.irp.core.tenancy.project.ProjectRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

/**
 * Authenticates SDK ingestion requests via the "X-API-Key" header. Deliberately does not
 * reject the request itself on a missing/invalid key - it just leaves the SecurityContext
 * empty, so the filter chain's authorization rules (require authentication) produce a
 * uniform 401 through the same entry point as every other unauthenticated request.
 */
@Component
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-API-Key";

    private final ApiKeyRepository apiKeyRepository;
    private final ProjectRepository projectRepository;
    private final ApiKeyHasher apiKeyHasher;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {

        String rawKey = request.getHeader(HEADER);
        if (rawKey != null && !rawKey.isBlank()) {
            resolvePrincipal(rawKey).ifPresent(principal -> {
                var authentication = new ApiKeyAuthenticationToken(principal);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            });
        }

        filterChain.doFilter(request, response);
    }

    private Optional<ApiKeyPrincipal> resolvePrincipal(String rawKey) {
        String hash = apiKeyHasher.hash(rawKey);

        return apiKeyRepository.findByKeyHash(hash)
                .filter(key -> !key.isRevoked())
                .flatMap(this::toPrincipal);
    }

    private Optional<ApiKeyPrincipal> toPrincipal(ApiKey key) {
        return projectRepository.findById(key.getProjectId())
                .map(project -> {
                    key.setLastUsedAt(Instant.now());
                    apiKeyRepository.save(key);
                    return new ApiKeyPrincipal(key.getId(), project.getId(), project.getOrganizationId(), key.getKeyPrefix());
                });
    }
}
