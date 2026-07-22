package com.josue.ecommerce.product.repository;

import com.josue.ecommerce.product.domain.Product;

import java.util.List;
import java.util.UUID;

public interface ProductSearchRepository {

    List<Product> search(String escapedQuery, String normalizedCategory, String cursorName, UUID cursorId,
                         int fetchSize);
}
