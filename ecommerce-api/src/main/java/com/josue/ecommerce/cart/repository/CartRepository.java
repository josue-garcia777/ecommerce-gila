package com.josue.ecommerce.cart.repository;

import com.josue.ecommerce.cart.domain.Cart;
import com.josue.ecommerce.cart.domain.CartStatus;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CartRepository extends JpaRepository<Cart, UUID> {

    Optional<Cart> findFirstByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, CartStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Cart c where c.id = :cartId")
    Optional<Cart> findCartByIdForUpdate(@Param("cartId") UUID cartId);
}
