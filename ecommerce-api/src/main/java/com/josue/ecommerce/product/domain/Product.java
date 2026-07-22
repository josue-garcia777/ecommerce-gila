package com.josue.ecommerce.product.domain;

import com.josue.ecommerce.shared.ValueObjects.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "products")
@Getter
public class Product {

    @Id
    private UUID id;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "sku", nullable = false, length = 64,
            updatable = false))
    private Sku sku;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 2000)
    private String description;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "category", nullable = false, length = 100))
    private ProductCategory category;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "price_amount", nullable = false,
                    precision = 19, scale = 2)),
            @AttributeOverride(name = "currency", column = @Column(name = "price_currency", nullable = false,
                    length = 3, columnDefinition = "CHAR(3)"))
    })
    private Money price;

    @Column(nullable = false)
    private int stock;

    @Column(name = "weight_kg", nullable = false, precision = 10, scale = 3)
    private BigDecimal weightKg;

    @Column(name = "image_url", length = 2048)
    private String imageUrl;

    @Column(nullable = false)
    private boolean active;

    @Version
    @Column(nullable = false)
    private long version;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Product() {
    }

    public Product(UUID id, Sku sku, String name, String description, ProductCategory category, Money price,
                   int stock, BigDecimal weightKg, String imageUrl, Instant now) {
        this.id = id;
        this.sku = sku;
        applyProductDetails(name, description, category, price, stock, weightKg, imageUrl);
        this.active = true;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void update(String name, String description, ProductCategory category, Money price, int stock,
                       BigDecimal weightKg, String imageUrl, Instant now) {
        applyProductDetails(name, description, category, price, stock, weightKg, imageUrl);
        this.updatedAt = now;
    }

    public void update(String name, String description, ProductCategory category, Money price, int stock,
                       BigDecimal weightKg, Instant now) {
        applyProductDetails(name, description, category, price, stock, weightKg, imageUrl);
        this.updatedAt = now;
    }

    public void deactivate(Instant now) {
        active = false;
        updatedAt = now;
    }

    private void applyProductDetails(String name, String description, ProductCategory category,
                                     Money price, int stock, BigDecimal weightKg, String imageUrl) {
        this.name = required(name, "Name", 200);
        this.description = required(description, "Description", 2000);

        if (category == null) {
            throw new IllegalArgumentException("Category is required");
        }

        if (price == null) {
            throw new IllegalArgumentException("Price is required");
        }

        if (stock < 0) {
            throw new IllegalArgumentException("Stock must not be negative");
        }

        if (weightKg == null || weightKg.signum() < 0) {
            throw new IllegalArgumentException("Weight must not be negative");
        }

        this.category = category;
        this.price = price;
        this.stock = stock;
        this.weightKg = weightKg;
        this.imageUrl = imageUrl;
    }

    private String required(String value, String field, int maximumLength) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        String trimmed = value.trim();
        if (trimmed.length() > maximumLength) {
            throw new IllegalArgumentException(field + " must not exceed " + maximumLength + " characters");
        }
        return trimmed;
    }

}
