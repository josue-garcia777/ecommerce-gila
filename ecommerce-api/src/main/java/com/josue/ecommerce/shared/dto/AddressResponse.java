package com.josue.ecommerce.shared.dto;

public record AddressResponse(
        String line1,
        String line2,
        String city,
        String state,
        String postalCode,
        String countryCode
) {
}
