package com.josue.ecommerce.identity.mapper;

import com.josue.ecommerce.identity.domain.Role;
import com.josue.ecommerce.identity.domain.User;
import com.josue.ecommerce.identity.dto.UserResponse;
import com.josue.ecommerce.shared.ValueObjects.Address;
import com.josue.ecommerce.shared.dto.AddressResponse;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class IdentityMapper {

    public UserResponse toResponse(User user, Set<Role> roles) {
        return new UserResponse(user.getId(), user.getEmail(), Set.copyOf(roles),
                address(user.getAddress()));
    }

    private AddressResponse address(Address address) {
        if (address == null) {
            return null;
        }
        return new AddressResponse(
                address.getLine1(), address.getLine2(), address.getCity(), address.getState(),
                address.getPostalCode(), address.getCountryCode()
        );
    }

}
