package com.josue.ecommerce.product.repository.impl;

import com.josue.ecommerce.product.domain.Product;
import com.josue.ecommerce.product.repository.ProductSearchRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.UUID;

public class ProductSearchRepositoryImpl implements ProductSearchRepository {

    private final EntityManager entityManager;

    public ProductSearchRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<Product> search(String escapedQuery, String normalizedCategory, String cursorName, UUID cursorId,
                                int fetchSize) {
        StringBuilder jpql = new StringBuilder("select p from Product p where p.active = true");

        if (escapedQuery != null) {
            jpql.append(" and lower(p.name) like :query escape '!'");
        }

        if (normalizedCategory != null) {
            jpql.append(" and lower(p.category.value) = :category");
        }

        if (cursorName != null) {
            jpql.append(" and (lower(p.name) > :cursorName or (lower(p.name) = :cursorName and p.id > :cursorId))");
        }

        jpql.append(" order by lower(p.name), p.id");

        TypedQuery<Product> query = entityManager.createQuery(jpql.toString(), Product.class);

        if (escapedQuery != null) {
            query.setParameter("query", "%" + escapedQuery + "%");
        }

        if (normalizedCategory != null) {
            query.setParameter("category", normalizedCategory);

        }

        if (cursorName != null) {
            query.setParameter("cursorName", cursorName);
            query.setParameter("cursorId", cursorId);
        }

        return query.setMaxResults(fetchSize).getResultList();
    }
}
