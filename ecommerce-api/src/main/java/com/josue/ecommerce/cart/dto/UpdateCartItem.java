package com.josue.ecommerce.cart.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateCartItem(@NotNull @Positive Integer quantity) {
}
