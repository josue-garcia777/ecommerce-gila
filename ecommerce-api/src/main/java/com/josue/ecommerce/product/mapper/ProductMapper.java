package com.josue.ecommerce.product.mapper;

import com.josue.ecommerce.product.domain.Product;
import com.josue.ecommerce.product.domain.ProductCategory;
import com.josue.ecommerce.product.domain.Sku;
import com.josue.ecommerce.product.dto.CreateProduct;
import com.josue.ecommerce.product.dto.MoneyResponse;
import com.josue.ecommerce.product.dto.ProductResponse;
import com.josue.ecommerce.shared.ValueObjects.Money;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class ProductMapper {

    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku().value(),
                product.getName(),
                product.getDescription(),
                product.getCategory().value(),
                new MoneyResponse(product.getPrice().getAmount(), product.getPrice().getCurrency()),
                product.getStock(),
                product.getWeightKg(),
                product.getImageUrl(),
                product.isActive(),
                product.getVersion(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    public Product toEntity(CreateProduct productRequest, Sku sku, UUID id) {
        Instant now = Instant.now();
        return new Product(
                UUID.randomUUID(),
                sku,
                productRequest.name(),
                productRequest.description(),
                new ProductCategory(productRequest.category()),
                new Money(productRequest.price().amount(), productRequest.price().currency()),
                productRequest.stock(),
                productRequest.weightKg(),
                productRequest.imageUrl(),
                now
        );

    }
}
