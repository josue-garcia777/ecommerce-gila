package com.josue.ecommerce.cart.controller;

import com.josue.ecommerce.cart.dto.CartResponse;
import com.josue.ecommerce.cart.dto.UpdateCartItem;
import com.josue.ecommerce.cart.service.CartService;
import jakarta.validation.Valid;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/carts")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping
    ResponseEntity<CartResponse> createOrGet() {
        CartResponse cart = cartService.createOrGetActive();
        return ResponseEntity.created(URI.create("/api/v1/carts/" + cart.id())).body(cart);
    }

    @GetMapping("/{cartId}")
    CartResponse getCart(@PathVariable UUID cartId) {
        return cartService.getCart(cartId);
    }

    @PutMapping("/{cartId}/items/{productId}")
    CartResponse setProductQuantity(@PathVariable UUID cartId, @PathVariable UUID productId,
                                    @Valid @RequestBody UpdateCartItem request) {
        return cartService.setQuantity(cartId, productId, request.quantity());
    }

    @DeleteMapping("/{cartId}/items/{productId}")
    CartResponse removeProductFromCart(@PathVariable UUID cartId, @PathVariable UUID productId) {
        return cartService.removeProductFromCart(cartId, productId);
    }
}
