package com.josue.ecommerce.product.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateProduct(
        @NotBlank String name,
        @NotBlank String description,
        @NotBlank String category,
        @NotNull @Valid MoneyRequest price,
        @NotNull @Min(0) Integer stock,
        @NotNull @DecimalMin("0.000") @Digits(integer = 7, fraction = 3) BigDecimal weightKg,
        String imageUrl,
        @NotNull @PositiveOrZero Long version
) {
}
