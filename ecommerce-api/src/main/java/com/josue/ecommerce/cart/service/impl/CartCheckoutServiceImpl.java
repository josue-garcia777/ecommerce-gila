package com.josue.ecommerce.cart.service.impl;

import com.josue.ecommerce.cart.domain.Cart;
import com.josue.ecommerce.cart.domain.CartStatus;
import com.josue.ecommerce.cart.repository.CartItemRepository;
import com.josue.ecommerce.cart.repository.CartRepository;
import com.josue.ecommerce.cart.repository.specification.CartItemSpecifications;
import com.josue.ecommerce.cart.service.CartCheckoutService;
import com.josue.ecommerce.cart.service.cmd.CheckoutCart;
import com.josue.ecommerce.cart.service.cmd.CheckoutCartItem;

import java.time.Instant;
import java.util.UUID;

import com.josue.ecommerce.shared.error.BadRequestException;
import com.josue.ecommerce.shared.error.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartCheckoutServiceImpl implements CartCheckoutService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public CartCheckoutServiceImpl(CartRepository cartRepository, CartItemRepository cartItemRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }

    @Transactional
    @Override
    public CheckoutCart checkoutCartForUser(UUID cartId, UUID userId) {
        Cart cart = cartRepository.findCartByIdForUpdate(cartId)
                .orElseThrow(this::notFound);

        if (!cart.getUserId().equals(userId)) {
            throw notFound();
        }

       

        return new CheckoutCart(
                cart.getId(), cart.getUserId(), cart.getStatus(),
                cartItemRepository.findAll(
                                CartItemSpecifications.forCart(cartId), Sort.by("productId")).stream()
                        .map(item -> new CheckoutCartItem(item.getProductId(), item.getQuantity()))
                        .toList()
        );
    }

    @Transactional
    @Override
    public void markCheckedOut(UUID cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalStateException("Locked cart disappeared"));

        cart.checkOut(Instant.now());
    }

    private NotFoundException notFound() {
        return new NotFoundException("Cart not found",
                "No cart exists with the supplied ID for the current user");
    }
}
