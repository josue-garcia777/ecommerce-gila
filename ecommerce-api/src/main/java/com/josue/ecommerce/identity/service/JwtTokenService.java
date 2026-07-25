package com.josue.ecommerce.identity.service;

import com.josue.ecommerce.identity.domain.Role;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public interface JwtTokenService {

    IssuedAccessToken issue(UUID userId, Set<Role> roles);

    record IssuedAccessToken(String value, Instant expiresAt) {
    }
}
