package com.josue.ecommerce.cart.mapper;

import com.josue.ecommerce.cart.domain.Cart;
import com.josue.ecommerce.cart.domain.CartItem;
import com.josue.ecommerce.cart.dto.CartItemResponse;
import com.josue.ecommerce.cart.dto.CartMoneyResponse;
import com.josue.ecommerce.cart.dto.CartResponse;
import com.josue.ecommerce.product.service.cmd.ProductDetails;
import com.josue.ecommerce.shared.ValueObjects.Money;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class CartMapper {

    public CartResponse toResponse(Cart cart, Map<UUID, ProductDetails> products) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(item -> item(item, products.get(item.getProductId())))
                .toList();

        return new CartResponse(
                cart.getId(), cart.getStatus(), items, subtotal(items), cart.getVersion(),
                cart.getCreatedAt(), cart.getUpdatedAt()
        );
    }


    private CartItemResponse item(CartItem item, ProductDetails product) {
        if (product == null) {
            return new CartItemResponse(item.getProductId(), null, "Unavailable product", null,
                    item.getQuantity(), null, 0, false, null);
        }
        Money lineTotal = product.price().multiply(item.getQuantity());
        return new CartItemResponse(
                product.id(), product.sku(), product.name(), money(product.price()), item.getQuantity(),
                money(lineTotal), product.stock(), product.active() && product.stock() >= item.getQuantity(),
                product.imageUrl()
        );
    }


    private CartMoneyResponse subtotal(List<CartItemResponse> items) {
        Money total = null;
        for (CartItemResponse item : items) {
            if (item.lineTotal() == null) {
                continue;
            }
            Money line = new Money(item.lineTotal().amount(), item.lineTotal().currency());
            if (total != null && !total.getCurrency().equals(line.getCurrency())) {
                return null;
            }
            total = total == null ? line : total.add(line);
        }
        return total == null ? new CartMoneyResponse(java.math.BigDecimal.ZERO.setScale(2), "USD") : money(total);
    }


    private CartMoneyResponse money(Money money) {
        return new CartMoneyResponse(money.getAmount(), money.getCurrency());
    }
}
