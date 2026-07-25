package com.josue.ecommerce.identity.dto;

import com.josue.ecommerce.identity.domain.Role;
import com.josue.ecommerce.shared.dto.AddressResponse;

import java.util.Set;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        Set<Role> roles,
        AddressResponse address
) {
}
