package com.josue.ecommerce.product.service;

import com.josue.ecommerce.product.dto.CreateProductRequest;
import com.josue.ecommerce.product.dto.ProductResponse;
import com.josue.ecommerce.product.dto.UpdateProductRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface ProductService {
    @Transactional
    ProductResponse create(CreateProductRequest request);

    @Transactional(readOnly = true)
    ProductResponse get(UUID productId);

    @Transactional
    ProductResponse update(UUID productId, UpdateProductRequest request);

    @Transactional
    void delete(UUID productId);
}
