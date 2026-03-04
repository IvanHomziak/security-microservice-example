package com.example.securitymicroservice.user;

import java.util.Set;

public enum Role {
    CUSTOMER(Set.of(Permission.DATA_READ)),
    BUSINESS(Set.of(Permission.DATA_READ, Permission.DATA_UPDATE)),
    ADMIN(Set.of(Permission.DATA_READ, Permission.DATA_UPDATE, Permission.DATA_CREATE, Permission.DATA_DELETE));

    private final Set<Permission> permissions;

    Role(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public Set<Permission> permissions() {
        return permissions;
    }

    public String authorityName() {
        return "ROLE_" + name();
    }
}
