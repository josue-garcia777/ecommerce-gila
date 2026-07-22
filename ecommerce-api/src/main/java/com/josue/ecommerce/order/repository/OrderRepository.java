package com.josue.ecommerce.order.repository;

import com.josue.ecommerce.order.domain.CustomerOrder;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<CustomerOrder, UUID> {

    Optional<CustomerOrder> findByCartId(UUID cartId);

    Optional<CustomerOrder> findByIdempotencyKey(String idempotencyKey);

    Optional<CustomerOrder> findByIdAndUserId(UUID id, UUID userId);

    List<CustomerOrder> findAllByUserIdOrderByCreatedAtDesc(UUID userId);
}
