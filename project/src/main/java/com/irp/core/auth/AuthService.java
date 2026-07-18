package com.irp.core.auth;

import com.irp.core.common.audit.AuditService;
import com.irp.core.common.exception.ConflictException;
import com.irp.core.security.jwt.JwtService;
import com.irp.core.security.principal.AuthenticatedUser;
import com.irp.core.tenancy.organization.Organization;
import com.irp.core.tenancy.organization.OrganizationService;
import com.irp.core.tenancy.user.AppUser;
import com.irp.core.tenancy.user.AppUserRepository;
import com.irp.core.tenancy.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final OrganizationService organizationService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditService auditService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (appUserRepository.existsByEmail(request.email())) {
            throw new ConflictException("An account with this email already exists");
        }

        Organization organization = organizationService.createOrganization(request.organizationName());

        AppUser user = appUserRepository.save(new AppUser(
                organization.getId(), request.email(), passwordEncoder.encode(request.password()),
                request.fullName(), UserRole.OWNER));

        auditService.record(organization.getId(), null, user.getEmail(), "USER_REGISTERED", "AppUser", user.getId().toString(),
                Map.of("role", user.getRole().name()));

        return issueToken(new AuthenticatedUser(user));
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();

        auditService.record(authenticatedUser.getOrganizationId(), null, authenticatedUser.getUsername(),
                "LOGIN_SUCCESS", "AppUser", authenticatedUser.getId().toString(), Map.of());

        return issueToken(authenticatedUser);
    }

    private AuthResponse issueToken(AuthenticatedUser user) {
        String token = jwtService.generateToken(user);
        AppUser appUser = appUserRepository.findById(user.getId()).orElseThrow();
        return AuthResponse.of(token, jwtService.expirationSeconds(), UserSummary.from(appUser));
    }
}
