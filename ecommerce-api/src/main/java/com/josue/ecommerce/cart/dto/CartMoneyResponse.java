package com.josue.ecommerce.cart.dto;

import java.math.BigDecimal;

public record CartMoneyResponse(BigDecimal amount, String currency) {
}
