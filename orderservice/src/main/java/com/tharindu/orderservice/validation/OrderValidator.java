package com.tharindu.orderservice.validation;

import com.tharindu.orderservice.dto.CreateOrderRequest;

public class OrderValidator {
    public static void validate(CreateOrderRequest request) {
        if (request.getOrderId() == null || request.getOrderId().isEmpty()) {
            throw new IllegalArgumentException("Order ID is required");
        }
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
    }
}
