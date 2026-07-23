package com.josue.ecommerce.product.controller;

import com.josue.ecommerce.product.dto.CreateProduct;
import com.josue.ecommerce.product.dto.ProductPageResponse;
import com.josue.ecommerce.product.dto.ProductResponse;
import com.josue.ecommerce.product.dto.UpdateProduct;
import com.josue.ecommerce.product.service.ProductService;
import com.josue.ecommerce.product.service.ProductSearchService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Validated
public class ProductController {

    private final ProductService productService;
    private final ProductSearchService productSearchService;

    public ProductController(ProductService productService, ProductSearchService productSearchService) {
        this.productService = productService;
        this.productSearchService = productSearchService;
    }

    @GetMapping("/products")
    ProductPageResponse search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "20") @Min(1) int limit,
            @RequestParam(required = false) String cursor) {
        return productSearchService.search(q, category, limit, cursor);
    }

    @PostMapping("/products")
    ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProduct request) {
        ProductResponse response = productService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/products/" + response.id())).body(response);
    }

    @GetMapping("/products/{productId}")
    ProductResponse get(@PathVariable UUID productId) {
        return productService.get(productId);
    }

    @PutMapping("/products/{productId}")
    ProductResponse update(@PathVariable UUID productId, @Valid @RequestBody UpdateProduct request) {
        return productService.update(productId, request);
    }

    @DeleteMapping("/products/{productId}")
    ResponseEntity<Void> delete(@PathVariable UUID productId) {
        productService.delete(productId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/categories")
    List<String> categories() {
        return productSearchService.categories();
    }
}
