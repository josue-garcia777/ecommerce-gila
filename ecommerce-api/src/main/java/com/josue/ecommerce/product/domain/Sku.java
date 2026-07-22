package com.josue.ecommerce.product.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;

import java.util.Locale;
import java.util.Objects;

@Embeddable
@EqualsAndHashCode
public class Sku {

    @Column(name = "sku", nullable = false, length = 64, updatable = false)
    private String value;

    protected Sku() {
    }

    public Sku(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("SKU is required");
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (normalized.length() > 64) {
            throw new IllegalArgumentException("SKU must not exceed 64 characters");
        }
        this.value = normalized;
    }

    public String value() {
        return value;
    }


    @Override
    public String toString() {
        return value;
    }
}
