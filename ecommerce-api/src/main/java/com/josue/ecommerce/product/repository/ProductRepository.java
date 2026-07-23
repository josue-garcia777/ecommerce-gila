package com.josue.ecommerce.product.repository;

import com.josue.ecommerce.product.domain.Product;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

    @Query("select distinct p.category.value from Product p where p.active = true order by p.category.value")
    List<String> findActiveCategories();

    @Modifying
    @Query("update Product p set p.stock = p.stock - :quantity, p.version = p.version + 1, "
            + "p.updatedAt = :updatedAt where p.id = :productId and p.active = true and p.stock >= :quantity")
    int decrementStock(@Param("productId") UUID productId, @Param("quantity") int quantity,
                       @Param("updatedAt") Instant updatedAt);
}
