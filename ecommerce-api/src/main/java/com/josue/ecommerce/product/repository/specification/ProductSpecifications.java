package com.josue.ecommerce.product.repository.specification;

import com.josue.ecommerce.product.domain.Product;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    public static Specification<Product> hasId(UUID productId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("id"), productId);
    }

    public static Specification<Product> hasIdIn(Collection<UUID> productIds) {
        return (root, query, criteriaBuilder) -> root.get("id").in(productIds);
    }

    public static Specification<Product> hasNormalizedSku(String sku) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("sku").get("value"), sku);
    }

    public static Specification<Product> hasNormalizedSkuIn(Collection<String> skus) {
        return (root, query, criteriaBuilder) -> root.get("sku").get("value").in(skus);
    }

    public static Specification<Product> isActive() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isTrue(root.get("active"));
    }

    public static Specification<Product> catalogSearch(String escapedQuery, String normalizedCategory,
                                                       String cursorName, UUID cursorId) {
        return (root, query, criteriaBuilder) -> {
            Expression<String> normalizedName = criteriaBuilder.lower(root.get("name"));
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.isTrue(root.get("active")));

            if (escapedQuery != null) {
                predicates.add(criteriaBuilder.like(normalizedName, "%" + escapedQuery + "%", '!'));
            }
            if (normalizedCategory != null) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("category").get("value")), normalizedCategory));
            }
            if (cursorName != null) {
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.greaterThan(normalizedName, cursorName),
                        criteriaBuilder.and(
                                criteriaBuilder.equal(normalizedName, cursorName),
                                criteriaBuilder.greaterThan(root.get("id"), cursorId)
                        )
                ));
            }

            query.orderBy(criteriaBuilder.asc(normalizedName), criteriaBuilder.asc(root.get("id")));
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
