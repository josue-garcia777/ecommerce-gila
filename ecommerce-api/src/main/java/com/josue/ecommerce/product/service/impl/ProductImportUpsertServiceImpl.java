package com.josue.ecommerce.product.service.impl;

import com.josue.ecommerce.product.domain.Product;
import com.josue.ecommerce.product.domain.ProductCategory;
import com.josue.ecommerce.product.domain.Sku;
import com.josue.ecommerce.product.repository.ProductRepository;
import com.josue.ecommerce.product.repository.specification.ProductSpecifications;
import com.josue.ecommerce.product.service.ProductImportUpsertService;
import com.josue.ecommerce.product.service.cmd.ProductImportCommand;
import com.josue.ecommerce.product.service.cmd.ProductImportResult;
import com.josue.ecommerce.shared.ValueObjects.Money;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductImportUpsertServiceImpl implements ProductImportUpsertService {

    private final ProductRepository productRepository;

    public ProductImportUpsertServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    @Override
    public ProductImportResult upsert(Collection<ProductImportCommand> commands) {
        if (commands.isEmpty()) {
            return new ProductImportResult(0, 0);
        }

        Map<String, Product> existing = productRepository.findAll(
                        ProductSpecifications.hasNormalizedSkuIn(
                                commands.stream().map(ProductImportCommand::sku).toList()))
                .stream()
                .collect(Collectors.toMap(product -> product.getSku().value(), Function.identity()));

        int created = 0;
        int updated = 0;

        Instant now = Instant.now();

        for (ProductImportCommand command : commands) {
            Product product = existing.get(command.sku());

            if (product == null) {
                product = new Product(
                        new Sku(command.sku()),
                        command.name(),
                        command.description(),
                        new ProductCategory(command.category()),
                        new Money(command.price(), "USD"),
                        command.stock(),
                        command.weightKg(),
                        null,
                        now
                );
                productRepository.save(product);
                created++;
            } else {
                product.update(
                        command.name(),
                        command.description(),
                        new ProductCategory(command.category()),
                        new Money(command.price(), "USD"),
                        command.stock(),
                        command.weightKg(),
                        now
                );
                updated++;
            }
        }

        return new ProductImportResult(created, updated);
    }
}
