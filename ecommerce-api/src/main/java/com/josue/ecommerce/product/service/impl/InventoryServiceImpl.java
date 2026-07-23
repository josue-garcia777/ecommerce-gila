package com.josue.ecommerce.product.service.impl;

import com.josue.ecommerce.product.domain.Product;
import com.josue.ecommerce.product.repository.ProductRepository;
import com.josue.ecommerce.product.repository.specification.ProductSpecifications;
import com.josue.ecommerce.product.service.InventoryService;
import com.josue.ecommerce.product.service.cmd.InventoryDecrement;
import com.josue.ecommerce.product.service.cmd.ProductDetails;
import com.josue.ecommerce.shared.error.InsufficientInventoryStock;

import java.time.Instant;
import java.util.*;
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
    public Map<UUID, ProductDetails> decrementInventoryAndLoad(List<InventoryDecrement> requests) {
        Map<UUID, Integer> quantitiesByProductId = groupQuantitiesByProductId(requests);
        decrementInventory(quantitiesByProductId);

        return productRepository.findAll(ProductSpecifications.hasIdIn(quantitiesByProductId.keySet())).stream()
                .map(this::details).collect(
                        Collectors.toUnmodifiableMap(ProductDetails::id, Function.identity())
                );
    }

    private Map<UUID, Integer> groupQuantitiesByProductId(List<InventoryDecrement> requests) {
        Map<UUID, Integer> quantitiesByProductId = new HashMap<>();

        for (InventoryDecrement request : requests) {
            quantitiesByProductId.merge(
                    request.productId(),
                    request.quantity(),
                    Math::addExact
            );
        }

        return quantitiesByProductId;
    }

    private void decrementInventory(
            Map<UUID, Integer> quantitiesByProductId
    ) {
        Instant updatedAt = Instant.now();

        List<UUID> productIds =
                new ArrayList<>(quantitiesByProductId.keySet());

        productIds.sort(UUID::compareTo);

        for (UUID productId : productIds) {
            int quantity = quantitiesByProductId.get(productId);

            int updatedRows = productRepository.decrementStock(
                    productId,
                    quantity,
                    updatedAt
            );

            if (updatedRows != 1) {
                throw new InsufficientInventoryStock(
                        "Insufficient stock",
                        "A product is unavailable or does not have enough stock to complete checkout"
                );
            }
        }
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
