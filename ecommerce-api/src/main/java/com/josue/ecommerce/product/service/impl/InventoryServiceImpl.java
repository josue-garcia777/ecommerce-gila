package com.josue.ecommerce.product.service.impl;

import com.josue.ecommerce.product.domain.Product;
import com.josue.ecommerce.product.repository.ProductRepository;
import com.josue.ecommerce.product.service.InventoryService;
import com.josue.ecommerce.product.service.cmd.ProductDetails;
import com.josue.ecommerce.shared.error.InsufficientInventoryStock;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
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
    public Map<UUID, ProductDetails> decrementAllAndLoad(Map<UUID, Integer> quantitiesByProductId) {
        List<Map.Entry<UUID, Integer>> orderedRequests = quantitiesByProductId.entrySet().stream()
                .sorted(Comparator.comparing(entry -> entry.getKey().toString()))
                .toList();

        for (Map.Entry<UUID, Integer> request : orderedRequests) {
            if (productRepository.decrementStock(request.getKey(), request.getValue(), Instant.now()) != 1) {
                throw new InsufficientInventoryStock(
                        "Insufficient stock",
                        "A product is unavailable or does not have enough stock to complete checkout"
                );
            }
        }

        Map<UUID, ProductDetails> products = productRepository.findAllByIdIn(quantitiesByProductId.keySet()).stream()
                .map(this::details)
                .collect(Collectors.toMap(ProductDetails::id, Function.identity()));
        if (products.size() != quantitiesByProductId.size()) {
            throw new IllegalStateException("Inventory changed during checkout");
        }
        return products;
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
