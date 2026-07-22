package com.josue.ecommerce.cart.service.impl;

import com.josue.ecommerce.cart.domain.Cart;
import com.josue.ecommerce.cart.domain.CartItem;
import com.josue.ecommerce.cart.domain.CartStatus;
import com.josue.ecommerce.cart.dto.CartResponse;
import com.josue.ecommerce.cart.mapper.CartMapper;
import com.josue.ecommerce.cart.repository.CartItemRepository;
import com.josue.ecommerce.cart.repository.CartRepository;
import com.josue.ecommerce.cart.service.CartService;
import com.josue.ecommerce.identity.CurrentUserProvider;
import com.josue.ecommerce.product.service.cmd.ProductDetails;
import com.josue.ecommerce.product.service.ProductQueryService;
import com.josue.ecommerce.shared.error.ApiException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.josue.ecommerce.shared.error.BadRequestException;
import com.josue.ecommerce.shared.error.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductQueryService productQueryService;
    private final CurrentUserProvider currentUserProvider;
    private final CartMapper cartMapper;

    public CartServiceImpl(CartRepository cartRepository, CartItemRepository cartItemRepository,
                       ProductQueryService productQueryService, CurrentUserProvider currentUserProvider,
                       CartMapper cartMapper) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productQueryService = productQueryService;
        this.currentUserProvider = currentUserProvider;
        this.cartMapper = cartMapper;
    }

    @Transactional
    public CartResponse createOrGetActive() {
        UUID userId = currentUserProvider.demoPrincipalUserId();

        Cart cart = cartRepository.findFirstByUserIdAndStatusOrderByCreatedAtDesc(userId, CartStatus.ACTIVE)
                .orElseGet(() -> cartRepository.save(new Cart(UUID.randomUUID(), userId, Instant.now())));

        return buildCartResponse(cart);
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(UUID cartId) {
        return buildCartResponse(findCart(cartId));
    }

    @Transactional
    public CartResponse setQuantity(UUID cartId, UUID productId, int quantity) {
        Cart cart = findActiveCart(cartId);

        ProductDetails product = productQueryService.findByIds(List.of(productId)).get(productId);

        if (product == null || !product.active()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Product not found",
                    "The product is unavailable and cannot be added to a cart");
        }

        cartItemRepository.findByCartIdAndProductId(cartId, productId)
                .ifPresentOrElse(
                        item -> item.setQuantity(quantity),
                        () -> cartItemRepository.save(
                                new CartItem(UUID.randomUUID(), cartId, productId, quantity))
                );
        cart.touch(Instant.now());

        return buildCartResponse(cart);
    }

    @Transactional
    public CartResponse removeProductFromCart(UUID cartId, UUID productId) {
        Cart cart = findActiveCart(cartId);
        cartItemRepository.deleteByCartIdAndProductId(cartId, productId);
        cart.touch(Instant.now());

        return buildCartResponse(cart);
    }

    private CartResponse buildCartResponse(Cart cart) {
        List<CartItem> items = cartItemRepository.findAllByCartIdOrderByProductId(cart.getId());

        Map<UUID, ProductDetails> products = productQueryService.findByIds(
                items.stream().map(CartItem::getProductId).toList());

        return cartMapper.toResponse(cart, items, products);
    }

    private Cart findCart(UUID cartId) {
        Optional<Cart> cart = cartRepository.findById(cartId);

        if (cart.isEmpty() || !cart.get().getUserId().equals(currentUserProvider.demoPrincipalUserId())) {
            throw new NotFoundException("Cart not found", "No cart exists for the current user");
        }

        return cart.get();
    }

    private Cart findActiveCart(UUID cartId) {
        Cart cart = findCart(cartId);
        if (cart.getStatus() != CartStatus.ACTIVE) {
            throw new BadRequestException(HttpStatus.CONFLICT, "Invalid cart state", "Only an active cart can be changed");
        }
        return cart;
    }

}
