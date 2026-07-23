package com.josue.ecommerce.cart.service.checkout;

import com.josue.ecommerce.cart.service.cmd.CheckoutCartItem;
import com.josue.ecommerce.product.service.cmd.ProductDetails;
import com.josue.ecommerce.shared.ValueObjects.Money;
import com.josue.ecommerce.shared.error.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class CheckoutSummaryFactory {

    public CheckoutSummary create(
            List<CheckoutCartItem> cartItems,
            Map<UUID, ProductDetails> products) {

        List<CheckoutLineSnapshot> lines =
                new ArrayList<>(cartItems.size());

        Money total = null;

        for (CheckoutCartItem cartItem : cartItems) {
            ProductDetails product = getAvailableProduct(
                    cartItem.productId(),
                    products
            );

            Money lineTotal = product.price()
                    .multiply(cartItem.quantity());

            CheckoutLineSnapshot line =
                    new CheckoutLineSnapshot(
                            product.id(),
                            product.sku(),
                            product.name(),
                            product.price(),
                            cartItem.quantity(),
                            lineTotal
                    );

            lines.add(line);
            total = total == null
                    ? lineTotal
                    : addMoney(total, lineTotal);
        }

        if (total == null) {
            throw new IllegalStateException(
                    "Cannot calculate the total of an empty checkout"
            );
        }

        return new CheckoutSummary(lines, total);
    }

    private ProductDetails getAvailableProduct(
            UUID productId,
            Map<UUID, ProductDetails> products
    ) {
        return products.get(productId);
    }

    private Money addMoney(
            Money currentTotal,
            Money lineTotal
    ) {
        if (!currentTotal.getCurrency()
                .equals(lineTotal.getCurrency())) {
            throw new BadRequestException(
                    HttpStatus.CONFLICT,
                    "Invalid cart state",
                    "Products with different currencies cannot be checked out together"
            );
        }

        return currentTotal.add(lineTotal);
    }
}

