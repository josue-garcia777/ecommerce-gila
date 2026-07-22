package com.josue.ecommerce.product.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Locale;
import java.util.Objects;

@Embeddable
public class ProductCategory {

    @Column(name = "category", nullable = false, length = 100)
    private String value;

    protected ProductCategory() {
    }

    public ProductCategory(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Category is required");
        }

        this.value = value.trim();
    }

    public String value() {
        return value;
    }

    public String normalized() {
        return value.toLowerCase(Locale.ROOT);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof ProductCategory other && normalized().equals(other.normalized());
    }

    @Override
    public int hashCode() {
        return Objects.hash(normalized());
    }

    @Override
    public String toString() {
        return value;
    }
}
