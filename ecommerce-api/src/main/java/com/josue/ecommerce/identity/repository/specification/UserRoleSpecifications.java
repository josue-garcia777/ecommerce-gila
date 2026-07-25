package com.josue.ecommerce.identity.repository.specification;

import com.josue.ecommerce.identity.domain.UserRole;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class UserRoleSpecifications {

    private UserRoleSpecifications() {
    }

    public static Specification<UserRole> hasUserId(UUID userId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("user").get("id"), userId);
    }
}
