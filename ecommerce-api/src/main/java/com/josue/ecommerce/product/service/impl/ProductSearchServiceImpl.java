package com.josue.ecommerce.product.service.impl;

import com.josue.ecommerce.product.domain.Product;
import com.josue.ecommerce.product.dto.ProductPageResponse;
import com.josue.ecommerce.product.dto.ProductResponse;
import com.josue.ecommerce.product.mapper.ProductMapper;
import com.josue.ecommerce.product.repository.ProductRepository;
import com.josue.ecommerce.product.repository.specification.ProductSpecifications;
import com.josue.ecommerce.product.service.ProductSearchService;
import com.josue.ecommerce.shared.error.ApiException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductSearchServiceImpl implements ProductSearchService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductSearchServiceImpl(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Transactional(readOnly = true)
    @Override
    public ProductPageResponse search(String query, String category, int limit, String cursor) {
        SearchCursor decodedCursor = decode(cursor);
        String normalizedQuery = normalize(query);
        String normalizedCategory = normalize(category);
        List<Product> fetched = productRepository.findBy(
                ProductSpecifications.catalogSearch(
                        normalizedQuery == null ? null : escapeLike(normalizedQuery),
                        normalizedCategory,
                        decodedCursor == null ? null : decodedCursor.name(),
                        decodedCursor == null ? null : decodedCursor.id()
                ),
                queryResult -> queryResult.limit(limit + 1).all()
        );
        boolean hasMore = fetched.size() > limit;
        List<Product> page = hasMore ? new ArrayList<>(fetched.subList(0, limit)) : fetched;
        List<ProductResponse> items = page.stream().map(productMapper::toResponse).toList();
        String nextCursor = hasMore && !page.isEmpty() ? encode(page.get(page.size() - 1)) : null;
        return new ProductPageResponse(items, nextCursor, hasMore);
    }

    @Transactional(readOnly = true)
    @Override
    public List<String> categories() {
        return productRepository.findActiveCategories().stream()
                .collect(java.util.stream.Collectors.toMap(
                        value -> value.toLowerCase(Locale.ROOT),
                        value -> value,
                        (first, second) -> first,
                        java.util.TreeMap::new
                ))
                .values().stream().toList();
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String escapeLike(String value) {
        return value.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }

    private String encode(Product product) {
        String value = product.getName().toLowerCase(Locale.ROOT) + "\n" + product.getId();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private SearchCursor decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            int separator = decoded.lastIndexOf('\n');
            if (separator <= 0 || separator == decoded.length() - 1) {
                throw new IllegalArgumentException();
            }
            return new SearchCursor(decoded.substring(0, separator), UUID.fromString(decoded.substring(separator + 1)));
        } catch (IllegalArgumentException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid cursor",
                    "The product cursor is malformed or expired");
        }
    }

    private record SearchCursor(String name, UUID id) {
    }
}
