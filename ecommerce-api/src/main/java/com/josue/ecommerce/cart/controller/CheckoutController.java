package com.josue.ecommerce.cart.controller;

import com.josue.ecommerce.order.dto.OrderResponse;
import com.josue.ecommerce.cart.service.CheckoutService;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/carts/{cartId}/checkout")
@PreAuthorize("hasRole('CUSTOMER')")
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping
    OrderResponse checkout(@PathVariable UUID cartId,
                           @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey) {
        return checkoutService.checkout(cartId, idempotencyKey);
    }
}
