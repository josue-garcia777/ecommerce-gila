package com.josue.ecommerce.order.controller;

import com.josue.ecommerce.order.dto.OrderResponse;
import com.josue.ecommerce.order.dto.OrderSummaryResponse;
import com.josue.ecommerce.order.service.OrderQueryService;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderQueryService orderQueryService;

    public OrderController(OrderQueryService orderQueryService) {
        this.orderQueryService = orderQueryService;
    }

    @GetMapping("/{orderId}")
    OrderResponse get(@PathVariable UUID orderId) {
        return orderQueryService.getOrderById(orderId);
    }

    @GetMapping
    List<OrderSummaryResponse> list() {
        return orderQueryService.getOrders();
    }
}
