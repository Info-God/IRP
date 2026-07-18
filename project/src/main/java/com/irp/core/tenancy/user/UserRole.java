package com.irp.core.tenancy.user;

/**
 * Deliberately flat: owner/admin/member is enough to demonstrate role-gated actions
 * without building a full permission matrix in Phase 1.
 */
public enum UserRole {
    OWNER,
    ADMIN,
    MEMBER
}
