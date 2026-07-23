package com.josue.ecommerce.order.repository.specification;

import com.josue.ecommerce.order.domain.OrderItem;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class OrderItemSpecifications {

    private OrderItemSpecifications() {
    }

    public static Specification<OrderItem> forOrder(UUID orderId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("order").get("id"), orderId);
    }
}
