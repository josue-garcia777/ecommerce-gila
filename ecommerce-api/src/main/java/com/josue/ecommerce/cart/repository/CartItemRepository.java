package com.josue.ecommerce.cart.repository;

import com.josue.ecommerce.cart.domain.CartItem;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CartItemRepository extends JpaRepository<CartItem, UUID>, JpaSpecificationExecutor<CartItem> {

    void deleteByCartIdAndProductId(UUID cartId, UUID productId);
}
