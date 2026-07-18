package com.irp.core.tenancy.organization;

import com.irp.core.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private static final Pattern NON_ALNUM = Pattern.compile("[^a-z0-9]+");

    private final OrganizationRepository organizationRepository;

    @Transactional
    public Organization createOrganization(String name) {
        String slug = uniqueSlug(name);
        return organizationRepository.save(new Organization(name, slug));
    }

    @Transactional(readOnly = true)
    public Organization getById(UUID id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", id));
    }

    private String uniqueSlug(String name) {
        String base = NON_ALNUM.matcher(name.toLowerCase(Locale.ROOT)).replaceAll("-").replaceAll("^-+|-+$", "");
        if (base.isBlank()) {
            base = "org";
        }
        String candidate = base;
        while (organizationRepository.existsBySlug(candidate)) {
            candidate = base + "-" + KeyGenerators.string().generateKey().substring(0, 6);
        }
        return candidate;
    }
}
