package com.josue.ecommerce.product.service.impl;

import com.josue.ecommerce.product.domain.Product;
import com.josue.ecommerce.product.dto.ProductPageResponse;
import com.josue.ecommerce.product.dto.ProductResponse;
import com.josue.ecommerce.product.mapper.ProductMapper;
import com.josue.ecommerce.product.repository.ProductRepository;
import com.josue.ecommerce.product.repository.specification.ProductSpecifications;
import com.josue.ecommerce.product.service.ProductQueryService;
import com.josue.ecommerce.product.service.ProductSearchService;
import com.josue.ecommerce.product.service.cmd.ProductDetails;
import com.josue.ecommerce.shared.error.ApiException;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.josue.ecommerce.shared.error.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.util.stream.Collectors.toMap;

@Service
public class ProductSearchServiceImpl implements ProductSearchService, ProductQueryService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductSearchServiceImpl(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Transactional(readOnly = true)
    @Override
    public ProductPageResponse search(String query, String category, int limit, String cursorStr) {
        SearchCursor cursor = decode(cursorStr);

        String search = normalizeAndEscapeQuery(query);
        String cat = normalize(category);
        String cursorName = cursor == null ? null : cursor.name();
        UUID cursorId = cursor == null ? null : cursor.id();

        List<Product> products = productRepository.findBy(
                ProductSpecifications.catalogSearch(
                        search,
                        cat,
                        cursorName,
                        cursorId
                ),
                queryResult -> queryResult.limit(limit + 1).all()
        );

        return toPageResponse(products, limit);
    }

    @Transactional(readOnly = true)
    @Override
    public List<String> categories() {
        TreeSet<String> categories = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

        categories.addAll(productRepository.findActiveCategories());
        return categories.stream().toList();
    }

    @Transactional(readOnly = true)
    @Override
    public Map<UUID, ProductDetails> findByIds(Collection<UUID> productIds) {
        if (productIds.isEmpty()) {
            return Map.of();
        }
        return productRepository.findAll(ProductSpecifications.hasIdIn(productIds)).stream()
                .map(this::details)
                .collect(toMap(ProductDetails::id, Function.identity()));
    }

    private ProductPageResponse toPageResponse(
            List<Product> fetchedProducts,
            int limit
    ) {
        boolean hasMore = fetchedProducts.size() > limit;

        List<Product> page = fetchedProducts.stream()
                .limit(limit)
                .toList();

        List<ProductResponse> items = page.stream()
                .map(productMapper::toResponse)
                .toList();

        String nextCursor = hasMore
                ? encode(page.getLast())
                : null;

        return new ProductPageResponse(items, nextCursor, hasMore);
    }

    private String normalizeAndEscapeQuery(String query) {
        String normalizedQuery = normalize(query);

        if (normalizedQuery == null) {
            return null;
        }

        return escapeLike(normalizedQuery);
    }


    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase();
    }

    private String escapeLike(String value) {
        return value.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }

    private String encode(Product product) {
        String value = product.getName().toLowerCase() + "\n" + product.getId();

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
                throw new BadRequestException(HttpStatus.BAD_REQUEST, "Invalid Cursor sent", "The provided coded cursor is invalid");
            }

            return new SearchCursor(decoded.substring(0, separator), UUID.fromString(decoded.substring(separator + 1)));

        } catch (IllegalArgumentException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid cursor",
                    "The product cursor is malformed");
        }
    }

    private record SearchCursor(String name, UUID id) {
    }
}
