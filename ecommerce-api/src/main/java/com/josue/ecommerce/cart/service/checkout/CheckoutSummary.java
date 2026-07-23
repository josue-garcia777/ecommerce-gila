package com.josue.ecommerce.cart.service.checkout;

import com.josue.ecommerce.shared.ValueObjects.Money;

import java.util.List;

public record CheckoutSummary(List<CheckoutLineSnapshot> lines, Money total) {
}
