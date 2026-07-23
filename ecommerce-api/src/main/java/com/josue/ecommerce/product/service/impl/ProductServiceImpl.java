package com.josue.ecommerce.product.service.impl;

import com.josue.ecommerce.product.domain.Product;
import com.josue.ecommerce.product.domain.ProductCategory;
import com.josue.ecommerce.product.domain.Sku;
import com.josue.ecommerce.product.dto.CreateProduct;
import com.josue.ecommerce.product.dto.ProductResponse;
import com.josue.ecommerce.product.dto.UpdateProduct;
import com.josue.ecommerce.product.mapper.ProductMapper;
import com.josue.ecommerce.product.repository.ProductRepository;
import com.josue.ecommerce.product.repository.specification.ProductSpecifications;
import com.josue.ecommerce.product.service.ProductService;
import com.josue.ecommerce.shared.ValueObjects.Money;
import com.josue.ecommerce.shared.error.ApiException;

import java.time.Instant;
import java.util.UUID;

import com.josue.ecommerce.shared.error.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Transactional @Override public ProductResponse create(CreateProduct request) {
        Sku sku = new Sku(request.sku());

        if (productRepository.exists(ProductSpecifications.hasNormalizedSku(sku.value()))) {
            throw new BadRequestException(HttpStatus.CONFLICT, "Duplicate SKU", "A product with this SKU already exists");
        }

        UUID productId = UUID.randomUUID();
        Product product = productMapper.toEntity(request, sku, productId);

        return productMapper.toResponse(productRepository.save(product));
    }

    @Transactional(readOnly = true) @Override public ProductResponse get(UUID productId) {
        return productMapper.toResponse(findActive(productId));
    }

    @Transactional @Override public ProductResponse update(UUID productId, UpdateProduct request) {
        Product product = findActive(productId);
        if (product.getVersion() != request.version()) {
            throw new ApiException(HttpStatus.CONFLICT, "Concurrent update conflict",
                    "The product changed since it was loaded");
        }
        product.update(
                request.name(),
                request.description(),
                new ProductCategory(request.category()),
                new Money(request.price().amount(), request.price().currency()),
                request.stock(),
                request.weightKg(),
                request.imageUrl(),
                Instant.now()
        );
        productRepository.flush();
        return productMapper.toResponse(product);
    }

    @Transactional @Override public void delete(UUID productId) {
        Product product = findActive(productId);
        product.deactivate(Instant.now());
    }

    private Product findActive(UUID productId) {
        Product product = productRepository.findOne(
                        ProductSpecifications.hasId(productId).and(ProductSpecifications.isActive()))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found",
                        "No product exists with the supplied ID"));
        return product;
    }
}
