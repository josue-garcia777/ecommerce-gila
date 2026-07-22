package com.josue.ecommerce.product.dto;

import java.math.BigDecimal;

public record MoneyResponse(BigDecimal amount, String currency) {
}
