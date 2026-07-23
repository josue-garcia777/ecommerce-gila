package com.josue.ecommerce.product.service;

import com.josue.ecommerce.product.dto.CreateProduct;
import com.josue.ecommerce.product.dto.ProductResponse;
import com.josue.ecommerce.product.dto.UpdateProduct;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface ProductService {
    @Transactional
    ProductResponse createProduct(CreateProduct request);

    @Transactional(readOnly = true)
    ProductResponse getProduct(UUID productId);

    @Transactional
    ProductResponse updateProduct(UUID productId, UpdateProduct request);

    @Transactional
    void deleteProduct(UUID productId);
}
