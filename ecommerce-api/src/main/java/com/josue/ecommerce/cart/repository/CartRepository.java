package com.josue.ecommerce.cart.repository;

import com.josue.ecommerce.cart.domain.Cart;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, UUID>, JpaSpecificationExecutor<Cart> {

    @EntityGraph(attributePaths = "items")
    Optional<Cart> findByIdAndUserId(UUID cartId, UUID userId);
}
