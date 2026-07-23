package com.josue.ecommerce.cart.service.impl;

import com.josue.ecommerce.cart.domain.CartStatus;
import com.josue.ecommerce.cart.service.CartCheckoutService;
import com.josue.ecommerce.cart.service.cmd.CheckoutCart;
import com.josue.ecommerce.cart.service.cmd.CheckoutCartItem;
import com.josue.ecommerce.identity.CurrentUserProvider;
import com.josue.ecommerce.order.domain.CustomerOrder;
import com.josue.ecommerce.order.domain.OrderItem;
import com.josue.ecommerce.order.dto.OrderResponse;
import com.josue.ecommerce.order.mapper.OrderMapper;
import com.josue.ecommerce.order.payment.PaymentGateway;
import com.josue.ecommerce.order.payment.PaymentGatewayException;
import com.josue.ecommerce.order.repository.OrderItemRepository;
import com.josue.ecommerce.order.repository.OrderRepository;
import com.josue.ecommerce.order.repository.specification.OrderItemSpecifications;
import com.josue.ecommerce.order.repository.specification.OrderSpecifications;
import com.josue.ecommerce.cart.service.CheckoutService;
import com.josue.ecommerce.product.service.InventoryService;
import com.josue.ecommerce.product.service.cmd.ProductDetails;
import com.josue.ecommerce.shared.ValueObjects.Money;
import com.josue.ecommerce.shared.error.ApiException;
import com.josue.ecommerce.shared.error.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CheckoutServiceImpl implements CheckoutService {

    private final CurrentUserProvider currentUserProvider;
    private final CartCheckoutService cartCheckoutService;
    private final InventoryService inventoryService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentGateway paymentGateway;
    private final OrderMapper orderMapper;

    @Transactional
    @Override
    public OrderResponse checkout(UUID cartId, String idempotencyKey) {
        String normalizedIdempotencyKey = normalizeIdempotencyKey(idempotencyKey);
        UUID userId = currentUserProvider.demoPrincipalUserId();

        Optional<CustomerOrder> existingOrder = orderRepository.findOne(OrderSpecifications.hasIdempotencyKey(normalizedIdempotencyKey));

        if (existingOrder.isPresent()) {
            return idempotentResponse(existingOrder.get(), cartId, userId);
        }

        return performNewCheckout(cartId, userId, normalizedIdempotencyKey);
    }

    private OrderResponse performNewCheckout(UUID cartId, UUID userId, String idempotencyKey) {
        CheckoutCart cart = cartCheckoutService.checkoutCartForUser(cartId, userId);
        if (isAlreadyCheckedOut(cart)) {
            return findExistingOrderResponse(cartId);
        }

        validateCart(cart);
        CheckoutSummary summary = createCheckoutSummary(cart.items());
        CustomerOrder order = createPendingOrder(cartId, userId, summary.total(), idempotencyKey);
        List<OrderItem> orderItems = createOrderItems(order.getId(), summary.lines());
        order = savePendingCheckout(order, orderItems);
        String paymentReference = authorizePayment(order.getId(), summary.total(), idempotencyKey);

        order.confirm(paymentReference);
        cartCheckoutService.markCheckedOut(cartId);

        return orderMapper.toResponse(order, orderItems);
    }

    private boolean isAlreadyCheckedOut(CheckoutCart cart) {
        return cart.status() == CartStatus.CHECKED_OUT;
    }

    private OrderResponse idempotentResponse(CustomerOrder order, UUID cartId, UUID userId) {
        if (!order.getCartId().equals(cartId) || !order.getUserId().equals(userId)) {
            throw idempotencyConflict();
        }
        return response(order);
    }

    private OrderResponse findExistingOrderResponse(UUID cartId) {
        CustomerOrder order = orderRepository.findOne(OrderSpecifications.hasCartId(cartId)).orElseThrow(this::checkedOutCartWithoutOrder);
        return response(order);
    }

    private OrderResponse response(CustomerOrder order) {
        return orderMapper.toResponse(order, orderItemRepository.findAll(OrderItemSpecifications.forOrder(order.getId()), Sort.by("productName")));
    }

    private void validateCart(CheckoutCart cart) {
        if (cart.items().isEmpty()) {
            throw emptyCartException();
        }
    }


    private CheckoutSummary createCheckoutSummary(List<CheckoutCartItem> cartItems) {
        Map<UUID, ProductDetails> products = decrementInventoryAndLoadProducts(cartItems);

        List<LineSnapshot> lines = cartItems.stream().map(cartItem -> createLineSnapshot(cartItem, products)).toList();

        Money total = calculateTotal(lines);

        return new CheckoutSummary(lines, total);
    }

    private Map<UUID, ProductDetails> decrementInventoryAndLoadProducts(List<CheckoutCartItem> cartItems) {
        Map<UUID, Integer> quantitiesByProductId = cartItems.stream().collect(Collectors.toMap(CheckoutCartItem::productId, CheckoutCartItem::quantity));

        return inventoryService.decrementInventoryAndLoad(quantitiesByProductId);
    }

    private LineSnapshot createLineSnapshot(CheckoutCartItem cartItem, Map<UUID, ProductDetails> products) {
        ProductDetails product = getAvailableProduct(cartItem.productId(), products);

        Money lineTotal = product.price().multiply(cartItem.quantity());

        return new LineSnapshot(product, cartItem.quantity(), lineTotal);
    }

    private ProductDetails getAvailableProduct(UUID productId, Map<UUID, ProductDetails> products) {
        ProductDetails product = products.get(productId);

        if (product == null || !product.active()) {
            throw productUnavailableException();
        }

        return product;
    }

    private Money calculateTotal(List<LineSnapshot> lines) {
        return lines.stream().map(LineSnapshot::lineTotal).reduce(this::addMoney).orElseThrow(() -> new IllegalStateException("Cannot calculate the total of an empty checkout"));
    }

    private Money addMoney(Money currentTotal, Money lineTotal) {
        if (!currentTotal.getCurrency().equals(lineTotal.getCurrency())) {
            throw mixedCurrenciesException();
        }
        return currentTotal.add(lineTotal);
    }

    private CustomerOrder createPendingOrder(UUID cartId, UUID userId, Money total, String idempotencyKey) {
        String orderIdentity = userId + ":" + cartId + ":" + idempotencyKey;
        UUID orderId = UUID.nameUUIDFromBytes(orderIdentity.getBytes(StandardCharsets.UTF_8));

        return new CustomerOrder(orderId, cartId, userId, total, idempotencyKey, Instant.now());
    }

    private String authorizePayment(UUID orderId, Money total, String idempotencyKey) {
        try {
            return paymentGateway.authorize(orderId, total, idempotencyKey);
        } catch (PaymentGatewayException exception) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Payment authorization failed", "Payment could not be authorized; retry with the same idempotency key");
        }
    }

    private List<OrderItem> createOrderItems(UUID orderId, List<LineSnapshot> lines) {
        return lines.stream().map(line -> createOrderItem(orderId, line)).toList();
    }

    private OrderItem createOrderItem(UUID orderId, LineSnapshot line) {
        ProductDetails product = line.product();

        return new OrderItem(UUID.randomUUID(), orderId, product.id(), product.sku(), product.name(), product.price(), line.quantity(), line.lineTotal());
    }

    private CustomerOrder savePendingCheckout(CustomerOrder order, List<OrderItem> orderItems) {
        CustomerOrder managedOrder = orderRepository.save(order);
        orderItemRepository.saveAll(orderItems);
        orderRepository.flush();
        return managedOrder;
    }

    private String normalizeIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new BadRequestException(HttpStatus.BAD_REQUEST, "Missing idempotency key", "The Idempotency-Key header is required for checkout");
        }
        return idempotencyKey.trim();
    }

    private BadRequestException checkedOutCartWithoutOrder() {
        return new BadRequestException(HttpStatus.CONFLICT, "Invalid cart state", "The checked-out cart has no associated order");
    }

    private BadRequestException emptyCartException() {
        return new BadRequestException(HttpStatus.CONFLICT, "Invalid cart state", "An empty cart cannot be checked out");
    }

    private BadRequestException productUnavailableException() {
        return new BadRequestException(HttpStatus.CONFLICT, "Product unavailable", "A product in the cart is no longer available");
    }

    private BadRequestException mixedCurrenciesException() {
        return new BadRequestException(HttpStatus.CONFLICT, "Invalid cart state", "Products with different currencies cannot be checked out together");
    }

    private BadRequestException idempotencyConflict() {
        return new BadRequestException(HttpStatus.CONFLICT, "Idempotency key conflict", "The supplied idempotency key was already used for a different checkout");
    }

    private record CheckoutSummary(List<LineSnapshot> lines, Money total) {
    }

    private record LineSnapshot(ProductDetails product, int quantity, Money lineTotal) {
    }
}
