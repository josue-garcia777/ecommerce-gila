package com.josue.ecommerce.product.service;

import com.josue.ecommerce.product.domain.Product;
import com.josue.ecommerce.product.service.cmd.ProductDetails;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface ProductQueryService {
    @Transactional(readOnly = true)
    Map<UUID, ProductDetails> findByIds(Collection<UUID> productIds);

    default ProductDetails details(Product product) {
        return new ProductDetails(
                product.getId(), product.getSku().value(), product.getName(), product.getDescription(),
                product.getCategory().value(), product.getPrice(), product.getStock(), product.getWeightKg(),
                product.getImageUrl(), product.isActive()
        );
    }
}
