package com.josue.ecommerce.shared.ValueObjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.util.Objects;

import lombok.Getter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Embeddable
public class Money {
    private final static String DEFAULT_CURR = "USD";

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3, columnDefinition = "CHAR(3)")
    @JdbcTypeCode(SqlTypes.CHAR)
    private String currency;


    protected Money() {
    }

    public Money(BigDecimal amount, String currency) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount is required");
        }

        if (amount.signum() < 0) {
            throw new IllegalArgumentException("Amount must not be negative");
        }

        if (currency == null || currency.isBlank()) {
            currency = DEFAULT_CURR;
        }

        String normalizedCurrency = currency.trim().toUpperCase();

        this.amount = amount.setScale(2);
        this.currency = normalizedCurrency;
    }


    public Money add(Money other) {

        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Currencies must match");
        }

        return new Money(amount.add(other.amount), currency);
    }

    public Money multiply(int quantity) {

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        return new Money(amount.multiply(BigDecimal.valueOf(quantity)), currency);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Money other)) {
            return false;
        }
        return amount.compareTo(other.amount) == 0 && currency.equals(other.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.stripTrailingZeros(), currency);
    }
}
