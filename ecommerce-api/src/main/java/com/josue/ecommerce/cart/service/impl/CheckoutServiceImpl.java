package com.josue.ecommerce.cart.service.impl;

import com.josue.ecommerce.cart.domain.Cart;
import com.josue.ecommerce.cart.domain.CartStatus;
import com.josue.ecommerce.cart.mapper.CheckoutMapper;
import com.josue.ecommerce.cart.service.CartCheckoutService;
import com.josue.ecommerce.cart.service.CheckoutService;
import com.josue.ecommerce.cart.service.cmd.CheckoutCart;
import com.josue.ecommerce.cart.service.cmd.CheckoutCartItem;
import com.josue.ecommerce.identity.CurrentUserProvider;
import com.josue.ecommerce.order.domain.CustomerOrder;
import com.josue.ecommerce.order.dto.OrderResponse;
import com.josue.ecommerce.order.mapper.OrderMapper;
import com.josue.ecommerce.order.payment.PaymentGateway;
import com.josue.ecommerce.order.payment.PaymentGatewayException;
import com.josue.ecommerce.order.service.OrderService;
import com.josue.ecommerce.cart.service.checkout.CheckoutLineSnapshot;
import com.josue.ecommerce.cart.service.checkout.CheckoutSummary;
import com.josue.ecommerce.cart.service.checkout.CheckoutSummaryFactory;
import com.josue.ecommerce.product.service.InventoryService;
import com.josue.ecommerce.product.service.cmd.InventoryDecrement;
import com.josue.ecommerce.product.service.cmd.ProductDetails;
import com.josue.ecommerce.shared.ValueObjects.Money;
import com.josue.ecommerce.shared.error.ApiException;
import com.josue.ecommerce.shared.error.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class CheckoutServiceImpl implements CheckoutService {

    private final CurrentUserProvider currentUserProvider;
    private final CartCheckoutService cartCheckoutService;
    private final InventoryService inventoryService;
    private final OrderService orderService;
    private final PaymentGateway paymentGateway;
    private final CheckoutSummaryFactory checkoutSummaryFactory;
    private final OrderMapper orderMapper;
    private final CheckoutMapper checkoutMapper;

    @Transactional
    @Override
    public OrderResponse checkout(UUID cartId, String idempotencyKey) {

        log.info("Starting Checkout for: {} and {}", cartId, idempotencyKey);
        UUID userId = currentUserProvider.demoPrincipalUserId();
        String normalizedIdempotencyKey = normalizeIdempotencyKey(idempotencyKey);

        Optional<CustomerOrder> existingOrder = orderService.findByIdempotencyKeyAndUser(normalizedIdempotencyKey, userId);

        if (existingOrder.isPresent()) {
            return idempotentResponse(existingOrder.get(), cartId, userId);
        }

        return performNewCheckout(cartId, userId, normalizedIdempotencyKey);
    }

    private OrderResponse performNewCheckout(UUID cartId, UUID userId, String idempotencyKey) {
        Cart cart = cartCheckoutService.checkoutCartForUser(cartId, userId);
        if (cart.getStatus() == CartStatus.CHECKED_OUT) {
            return findExistingOrderResponse(cartId, userId);
        }
        if (cart.getStatus() != CartStatus.ACTIVE) {
            throw checkoutAlreadyInProgress();
        }

        CheckoutCart checkoutCart = checkoutMapper.toCheckOutCar(cart);
        validateCart(checkoutCart);

        cartCheckoutService.claimForCheckout(cart, Instant.now());

        Map<UUID, ProductDetails> products = decrementInventoryAndLoadProducts(checkoutCart.items());
        CheckoutSummary summary = checkoutSummaryFactory.create(checkoutCart.items(), products);

        CustomerOrder order = createPendingOrder(cartId, userId, summary.total(), idempotencyKey);
        addOrderItems(order, summary.lines());

        CustomerOrder savedOrder = orderService.savePendingOrder(order);

        String paymentReference = authorizePayment(savedOrder.getId(), summary.total(), idempotencyKey);

        savedOrder.confirm(paymentReference);

        cart.completeCheckout(Instant.now());

        return orderMapper.toResponse(savedOrder);
    }

    private OrderResponse idempotentResponse(CustomerOrder order, UUID cartId, UUID userId) {
        if (!order.getCartId().equals(cartId) || !order.getUserId().equals(userId)) {
            throw idempotencyConflict();
        }
        return response(order);
    }

    private OrderResponse findExistingOrderResponse(UUID cartId, UUID userId) {
        CustomerOrder order = orderService.findByCartIdAndUser(cartId, userId)
                .orElseThrow(this::checkedOutCartWithoutOrder);
        return response(order);
    }

    private OrderResponse response(CustomerOrder order) {
        return orderMapper.toResponse(order);
    }

    private void validateCart(CheckoutCart cart) {
        if (cart.items().isEmpty()) {
            throw emptyCartException();
        }
    }

    private Map<UUID, ProductDetails> decrementInventoryAndLoadProducts(List<CheckoutCartItem> cartItems) {
        List<InventoryDecrement> decrementCmd = cartItems
                .stream()
                .map(it -> new InventoryDecrement(it.productId(), it.quantity()))
                .toList();

        return inventoryService.decrementInventoryAndLoad(decrementCmd);
    }

    private CustomerOrder createPendingOrder(UUID cartId, UUID userId, Money total, String idempotencyKey) {
        return new CustomerOrder(cartId, userId, total, idempotencyKey, Instant.now());
    }

    private String authorizePayment(UUID orderId, Money total, String idempotencyKey) {
        try {
            return paymentGateway.authorize(orderId, total, idempotencyKey);
        } catch (PaymentGatewayException exception) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Payment authorization failed", "Payment could not be authorized; retry with the same idempotency key");
        }
    }

    private void addOrderItems(CustomerOrder order, List<CheckoutLineSnapshot> lines) {
        for (CheckoutLineSnapshot line : lines) {

            order.addItem(
                    line.productId(),
                    line.sku(),
                    line.productName(),
                    line.unitPrice(),
                    line.quantity(),
                    line.lineTotal()
            );
        }
    }

    private String normalizeIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new BadRequestException(HttpStatus.BAD_REQUEST, "Missing idempotency key", "The Idempotency-Key header is required for checkout");
        }
        return idempotencyKey.trim();
    }

    /*
         Errors
    * */
    private BadRequestException checkedOutCartWithoutOrder() {
        return new BadRequestException(HttpStatus.CONFLICT, "Invalid cart state", "The checked-out cart has no associated order");
    }

    private BadRequestException emptyCartException() {
        return new BadRequestException(HttpStatus.CONFLICT, "Invalid cart state", "An empty cart cannot be checked out");
    }

    private BadRequestException idempotencyConflict() {
        return new BadRequestException(HttpStatus.CONFLICT, "Idempotency key conflict", "The supplied idempotency key was already used for a different checkout");
    }

    private BadRequestException checkoutAlreadyInProgress() {
        return new BadRequestException(HttpStatus.CONFLICT, "This Checkout is in progress", "The supplied checkout is already being processed");

    }
}
