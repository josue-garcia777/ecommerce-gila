package com.josue.ecommerce.product.service;

import com.josue.ecommerce.product.dto.ProductPageResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ProductSearchService {
    @Transactional(readOnly = true)
    ProductPageResponse search(String query, String category, int limit, String cursor);

    @Transactional(readOnly = true)
    List<String> categories();
}
