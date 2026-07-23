package com.josue.ecommerce.order.repository.specification;

import com.josue.ecommerce.order.domain.CustomerOrder;
import jakarta.persistence.criteria.JoinType;

import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

public final class OrderSpecifications {

    private OrderSpecifications() {
    }

    public static Specification<CustomerOrder> hasCartId(UUID cartId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("cartId"), cartId);
    }

    public static Specification<CustomerOrder> hasIdempotencyKey(String idempotencyKey) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("idempotencyKey"), idempotencyKey);
    }

    public static Specification<CustomerOrder> hasIdAndUser(UUID orderId, UUID userId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.equal(root.get("id"), orderId),
                criteriaBuilder.equal(root.get("userId"), userId)
        );
    }

    public static Specification<CustomerOrder> hasUser(UUID userId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.equal(root.get("userId"), userId)
        );
    }

    public static Specification<CustomerOrder> forUser(UUID userId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("userId"), userId);
    }

    public static Specification<CustomerOrder> fetchItems() {
        return (root, query, criteriaBuilder) -> {

            root.fetch("items", JoinType.LEFT);
            query.distinct(true);

            return criteriaBuilder.conjunction();
        };
    }
}
