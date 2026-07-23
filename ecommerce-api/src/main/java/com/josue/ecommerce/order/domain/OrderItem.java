package com.josue.ecommerce.order.domain;

import com.josue.ecommerce.shared.ValueObjects.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
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
@Table(name = "order_items")
@Getter
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private CustomerOrder order;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(nullable = false, length = 64)
    private String sku;

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "unit_price_amount", nullable = false,
                    precision = 19, scale = 2)),
            @AttributeOverride(name = "currency", column = @Column(name = "unit_price_currency", nullable = false,
                    length = 3, columnDefinition = "CHAR(3)"))
    })
    private Money unitPrice;

    @Column(nullable = false)
    private int quantity;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "line_total_amount", nullable = false,
                    precision = 19, scale = 2)),
            @AttributeOverride(name = "currency", column = @Column(name = "line_total_currency", nullable = false,
                    length = 3, columnDefinition = "CHAR(3)"))
    })
    private Money lineTotal;

    protected OrderItem() {
    }

    OrderItem(CustomerOrder order, UUID productId, String sku, String productName, Money unitPrice,
              int quantity, Money lineTotal) {
        if (order == null) {
            throw new IllegalArgumentException("Order is required");
        }
        this.order = order;
        this.productId = productId;
        this.sku = sku;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.lineTotal = lineTotal;
    }

}
