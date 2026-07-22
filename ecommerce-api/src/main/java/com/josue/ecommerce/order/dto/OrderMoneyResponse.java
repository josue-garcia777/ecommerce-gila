package com.josue.ecommerce.order.dto;

import java.math.BigDecimal;

public record OrderMoneyResponse(BigDecimal amount, String currency) {
}
