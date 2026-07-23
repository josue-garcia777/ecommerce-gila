package com.josue.ecommerce.cart.mapper;

import com.josue.ecommerce.cart.domain.Cart;
import com.josue.ecommerce.cart.service.cmd.CheckoutCart;
import com.josue.ecommerce.cart.service.cmd.CheckoutCartItem;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class CheckoutMapper {

    public CheckoutCart toCheckOutCar(Cart cart) {
        return new CheckoutCart(
                cart.getId(), cart.getUserId(), cart.getStatus(), cart.getItems().stream()
                .map(item -> new CheckoutCartItem(item.getProductId(), item.getQuantity()))
                .toList()
        );
    }
}
