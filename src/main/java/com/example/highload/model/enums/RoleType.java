package com.example.highload.model.enums;

import org.springframework.security.core.GrantedAuthority;

public enum RoleType implements GrantedAuthority {
    ARTIST,
    CLIENT,
    ADMIN;

    @Override
    public String getAuthority() {
        return name();
    }

}
