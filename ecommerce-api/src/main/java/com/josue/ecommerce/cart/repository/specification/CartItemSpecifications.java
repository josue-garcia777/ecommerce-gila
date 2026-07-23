package com.josue.ecommerce.cart.repository.specification;

import com.josue.ecommerce.cart.domain.CartItem;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class CartItemSpecifications {

    private CartItemSpecifications() {
    }

    public static Specification<CartItem> forCart(UUID cartId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("cartId"), cartId);
    }

    public static Specification<CartItem> forCartAndProduct(UUID cartId, UUID productId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.equal(root.get("cartId"), cartId),
                criteriaBuilder.equal(root.get("productId"), productId)
        );
    }
}
