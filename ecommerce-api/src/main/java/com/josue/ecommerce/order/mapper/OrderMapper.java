package com.josue.ecommerce.order.mapper;

import com.josue.ecommerce.order.domain.CustomerOrder;
import com.josue.ecommerce.order.domain.OrderItem;
import com.josue.ecommerce.order.dto.OrderItemResponse;
import com.josue.ecommerce.order.dto.OrderMoneyResponse;
import com.josue.ecommerce.order.dto.OrderResponse;
import com.josue.ecommerce.order.dto.OrderSummaryResponse;
import com.josue.ecommerce.shared.ValueObjects.Money;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public OrderResponse toResponse(CustomerOrder order, List<OrderItem> items) {
        return new OrderResponse(
                order.getId(), order.getCartId(), order.getStatus(), money(order.getTotal()),
                order.getPaymentReference(), order.getCreatedAt(),
                items.stream().map(this::item).toList()
        );
    }

    public OrderSummaryResponse toSummary(CustomerOrder order) {
        return new OrderSummaryResponse(
                order.getId(), order.getCartId(), order.getStatus(), money(order.getTotal()), order.getCreatedAt()
        );
    }

    private OrderItemResponse item(OrderItem item) {
        return new OrderItemResponse(
                item.getProductId(), item.getSku(), item.getProductName(), money(item.getUnitPrice()),
                item.getQuantity(), money(item.getLineTotal())
        );
    }

    private OrderMoneyResponse money(Money money) {
        return new OrderMoneyResponse(money.getAmount(), money.getCurrency());
    }
}
