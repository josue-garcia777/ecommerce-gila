package com.josue.ecommerce.cart.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;

import java.util.UUID;

@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @Getter
    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Getter
    @Column(nullable = false)
    private int quantity;

    protected CartItem() {
    }

    CartItem(Cart cart, UUID productId, int quantity) {
        if (cart == null) {
            throw new IllegalArgumentException("Cart is required");
        }
        if (productId == null) {
            throw new IllegalArgumentException("Product ID is required");
        }
        this.cart = cart;
        this.productId = productId;
        setQuantity(quantity);
    }

    boolean hasProduct(UUID productId) {
        return this.productId.equals(productId);
    }

    public boolean isNew() {
        return id == null;
    }

    public void setQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.quantity = quantity;
    }

}
