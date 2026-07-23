package com.josue.ecommerce.product.service.impl;

import com.josue.ecommerce.product.domain.Product;
import com.josue.ecommerce.product.repository.ProductRepository;
import com.josue.ecommerce.product.repository.specification.ProductSpecifications;
import com.josue.ecommerce.product.service.InventoryService;
import com.josue.ecommerce.product.service.cmd.ProductDetails;
import com.josue.ecommerce.shared.error.InsufficientInventoryStock;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final ProductRepository productRepository;

    public InventoryServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    @Override
    public Map<UUID, ProductDetails> decrementInventoryAndLoad(Map<UUID, Integer> quantitiesByProductId) {

        for (Map.Entry<UUID, Integer> request : quantitiesByProductId.entrySet()) {
            UUID productId = request.getKey();
            int quantity = request.getValue();

            if (productRepository.decrementStock(productId, quantity, Instant.now()) != 1) {
                throw new InsufficientInventoryStock(
                        "Insufficient stock",
                        "A product is unavailable or does not have enough stock to complete checkout"
                );
            }
        }


        return productRepository
                .findAll(ProductSpecifications.hasIdIn(quantitiesByProductId.keySet())).stream()
                .map(this::details)
                .collect(Collectors.toMap(ProductDetails::id, Function.identity()));
    }

    private ProductDetails details(Product product) {
        return new ProductDetails(
                product.getId(),
                product.getSku().value(),
                product.getName(),
                product.getDescription(),
                product.getCategory().value(),
                product.getPrice(),
                product.getStock(),
                product.getWeightKg(),
                product.getImageUrl(),
                product.isActive()
        );
    }
}
