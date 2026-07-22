package com.josue.ecommerce.product.repository;

import com.josue.ecommerce.product.domain.Product;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, UUID>, ProductSearchRepository {

    @Query("select p from Product p where p.sku.value = :sku")
    Optional<Product> findByNormalizedSku(@Param("sku") String sku);

    @Query("select p from Product p where p.sku.value in :skus")
    List<Product> findAllByNormalizedSkuIn(@Param("skus") Collection<String> skus);

    @Query("select p from Product p where p.id in :ids")
    List<Product> findAllByIdIn(@Param("ids") Collection<UUID> ids);

    @Query("select distinct p.category.value from Product p where p.active = true order by p.category.value")
    List<String> findActiveCategories();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Product p set p.stock = p.stock - :quantity, p.version = p.version + 1, "
            + "p.updatedAt = :updatedAt where p.id = :productId and p.active = true and p.stock >= :quantity")
    int decrementStock(@Param("productId") UUID productId, @Param("quantity") int quantity,
                       @Param("updatedAt") java.time.Instant updatedAt);
}
