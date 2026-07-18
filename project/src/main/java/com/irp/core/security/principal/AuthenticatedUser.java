package com.irp.core.security.principal;

import com.irp.core.tenancy.user.AppUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Spring Security principal for JWT-authenticated dashboard users. Carries the
 * organization id alongside identity so controllers/services never have to re-fetch
 * the AppUser just to find out which tenant the caller belongs to.
 */
public class AuthenticatedUser implements UserDetails {

    private final UUID id;
    private final UUID organizationId;
    private final String email;
    private final String passwordHash;
    private final String role;

    public AuthenticatedUser(AppUser user) {
        this.id = user.getId();
        this.organizationId = user.getOrganizationId();
        this.email = user.getEmail();
        this.passwordHash = user.getPasswordHash();
        this.role = user.getRole().name();
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public String getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }
}
