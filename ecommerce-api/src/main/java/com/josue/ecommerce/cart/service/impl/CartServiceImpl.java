package com.josue.ecommerce.cart.service.impl;

import com.josue.ecommerce.cart.domain.Cart;
import com.josue.ecommerce.cart.domain.CartItem;
import com.josue.ecommerce.cart.domain.CartStatus;
import com.josue.ecommerce.cart.dto.CartResponse;
import com.josue.ecommerce.cart.mapper.CartMapper;
import com.josue.ecommerce.cart.repository.CartItemRepository;
import com.josue.ecommerce.cart.repository.CartRepository;
import com.josue.ecommerce.cart.repository.specification.CartSpecifications;
import com.josue.ecommerce.cart.service.CartService;
import com.josue.ecommerce.identity.CurrentUserProvider;
import com.josue.ecommerce.identity.service.UserService;
import com.josue.ecommerce.product.service.cmd.ProductDetails;
import com.josue.ecommerce.product.service.ProductQueryService;
import com.josue.ecommerce.shared.error.ApiException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.josue.ecommerce.shared.error.BadRequestException;
import com.josue.ecommerce.shared.error.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductQueryService productQueryService;
    private final CurrentUserProvider currentUserProvider;
    private final UserService userAccountService;
    private final CartMapper cartMapper;

    public CartServiceImpl(CartRepository cartRepository, CartItemRepository cartItemRepository,
                           ProductQueryService productQueryService,
                           CurrentUserProvider currentUserProvider,
                           UserService userAccountService,
                           CartMapper cartMapper) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productQueryService = productQueryService;
        this.currentUserProvider = currentUserProvider;
        this.userAccountService = userAccountService;
        this.cartMapper = cartMapper;
    }

    @Transactional
    public CartResponse createOrGetActive() {
        UUID userId = currentUserProvider.userPrincipalId();

        Cart cart = cartRepository.findBy(
                        CartSpecifications.activeForUser(userId).and(CartSpecifications.fetchItems()),
                        query -> query.sortBy(Sort.by(Sort.Direction.DESC, "createdAt")).first())
                .orElseGet(() -> cartRepository.save(new Cart(
                        userAccountService.getUser(userId), Instant.now())));

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

        CartItem item = cart.setProductQuantity(productId, quantity);
        if (item.isNew()) {
            cartItemRepository.save(item);
        }
        cart.touch(Instant.now());

        return buildCartResponse(cart);
    }

    @Transactional
    public CartResponse removeProductFromCart(UUID cartId, UUID productId) {
        Cart cart = findActiveCart(cartId);
        cart.removeProduct(productId).ifPresent(cartItemRepository::delete);
        cart.touch(Instant.now());

        return buildCartResponse(cart);
    }

    private CartResponse buildCartResponse(Cart cart) {
        List<CartItem> items = cart.getItems();

        Map<UUID, ProductDetails> products = productQueryService.findByIds(
                items.stream().map(CartItem::getProductId).toList());

        return cartMapper.toResponse(cart, products);
    }

    private Cart findCart(UUID cartId) {
        Specification<Cart> cartWithItems = CartSpecifications
                .hasIdAndUser(cartId, currentUserProvider.userPrincipalId())
                .and(CartSpecifications.fetchItems());

        return cartRepository.findOne(cartWithItems)
                .orElseThrow(() -> new NotFoundException(
                        "Cart not found", "No cart exists for the current user"));
    }

    private Cart findActiveCart(UUID cartId) {
        Cart cart = findCart(cartId);
        if (cart.getStatus() != CartStatus.ACTIVE) {
            throw new BadRequestException(HttpStatus.CONFLICT, "Invalid cart state", "Only an active cart can be changed");
        }
        return cart;
    }

}
