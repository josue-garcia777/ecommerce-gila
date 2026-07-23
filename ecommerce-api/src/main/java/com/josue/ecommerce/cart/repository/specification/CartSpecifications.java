package com.josue.ecommerce.cart.repository.specification;

import com.josue.ecommerce.cart.domain.Cart;
import com.josue.ecommerce.cart.domain.CartStatus;
import jakarta.persistence.criteria.JoinType;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class CartSpecifications {

    private CartSpecifications() {
    }

    public static Specification<Cart> activeForUser(UUID userId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.equal(root.get("userId"), userId),
                criteriaBuilder.equal(root.get("status"), CartStatus.ACTIVE)
        );
    }

    public static Specification<Cart> hasIdAndUser(UUID cartId, UUID userId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.equal(root.get("id"), cartId),
                criteriaBuilder.equal(root.get("userId"), userId)
        );
    }

    public static Specification<Cart> fetchItems() {
        return (root, query, criteriaBuilder) -> {
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("items", JoinType.LEFT);
                query.distinct(true);
            }
            return criteriaBuilder.conjunction();
        };
    }
}
