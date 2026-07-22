package com.josue.ecommerce.product.service.impl;

import com.josue.ecommerce.product.repository.ProductRepository;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.josue.ecommerce.product.service.cmd.ProductDetails;
import com.josue.ecommerce.product.service.ProductQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductQueryServiceImpl implements ProductQueryService {

    private final ProductRepository productRepository;

    public ProductQueryServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true) @Override public Map<UUID, ProductDetails> findByIds(Collection<UUID> productIds) {
        return productRepository.findAllByIdIn(productIds).stream()
                .map(this::details)
                .collect(Collectors.toMap(ProductDetails::id, Function.identity()));
    }

    }
