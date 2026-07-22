package com.josue.ecommerce.cart.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.util.UUID;

@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    private UUID id;

    @Getter
    @Column(name = "cart_id", nullable = false)
    private UUID cartId;

    @Getter
    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Getter
    @Column(nullable = false)
    private int quantity;

    protected CartItem() {
    }

    public CartItem(UUID id, UUID cartId, UUID productId, int quantity) {
        this.id = id;
        this.cartId = cartId;
        this.productId = productId;
        setQuantity(quantity);
    }

    public void setQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.quantity = quantity;
    }

}
