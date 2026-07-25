package com.josue.ecommerce.cart.service.impl;

import com.josue.ecommerce.cart.domain.Cart;
import com.josue.ecommerce.cart.repository.CartRepository;
import com.josue.ecommerce.cart.repository.specification.CartSpecifications;
import com.josue.ecommerce.cart.service.CartCheckoutService;
import com.josue.ecommerce.cart.service.cmd.CheckoutCart;
import com.josue.ecommerce.cart.service.cmd.CheckoutCartItem;

import java.time.Instant;
import java.util.UUID;

import com.josue.ecommerce.shared.error.NotFoundException;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartCheckoutServiceImpl implements CartCheckoutService {

    private final CartRepository cartRepository;

    public CartCheckoutServiceImpl(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    @Override
    public Cart checkoutCartForUser(UUID cartId, UUID userId) {
        return cartRepository.findOne(CartSpecifications.hasIdAndUser(cartId, userId)
                        .and(CartSpecifications.fetchItems()))
                .orElseThrow(this::notFound);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    @Override
    public void beginCheckout(Cart cart, Instant now) {
        cart.beginCheckout(now);
        cartRepository.flush();
    }

    private NotFoundException notFound() {
        return new NotFoundException("Cart not found",
                "No cart exists with the supplied ID for the current user");
    }
}
