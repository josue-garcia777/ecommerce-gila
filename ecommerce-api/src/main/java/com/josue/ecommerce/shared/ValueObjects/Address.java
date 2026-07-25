package com.josue.ecommerce.shared.ValueObjects;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;
import lombok.Getter;

import java.util.Locale;

@Embeddable
@Getter
public class Address {

    @Column(name = "address_line1", length = 200)
    private String line1;

    @Column(name = "address_line2", length = 200)
    private String line2;

    @Column(name = "address_city", length = 120)
    private String city;

    @Column(name = "address_state", length = 120)
    private String state;

    @Column(name = "address_postal_code", length = 32)
    private String postalCode;

    @Column(name = "address_country_code", length = 2)
    private String countryCode;

    protected Address() {
    }

    public Address(String line1, String line2, String city, String state,
                   String postalCode, String countryCode) {
        this.line1 = required(line1, "Address line 1", 200);
        this.line2 = optional(line2, "Address line 2", 200);
        this.city = required(city, "City", 120);
        this.state = optional(state, "State", 120);
        this.postalCode = required(postalCode, "Postal code", 32);
        this.countryCode = countryCode(countryCode);
    }

    private static String required(String value, String field, int maximumLength) {
        String normalized = optional(value, field, maximumLength);
        if (normalized == null) {
            throw new IllegalArgumentException(field + " is required");
        }
        return normalized;
    }

    private static String optional(String value, String field, int maximumLength) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() > maximumLength) {
            throw new IllegalArgumentException(field + " must not exceed " + maximumLength + " characters");
        }
        return normalized;
    }

    private static String countryCode(String value) {
        String normalized = required(value, "Country code", 2).toUpperCase(Locale.ROOT);
        if (normalized.length() != 2 || !normalized.chars().allMatch(Character::isLetter)) {
            throw new IllegalArgumentException("Country code must contain two letters");
        }
        return normalized;
    }
}
