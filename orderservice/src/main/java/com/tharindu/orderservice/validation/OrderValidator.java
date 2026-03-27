package com.tharindu.orderservice.validation;

import com.tharindu.orderservice.dto.CreateOrderRequest;

public class OrderValidator {
    // Simple validation logic for the order request
    public static void validate(CreateOrderRequest request) {
        // Make sure the orderId is not null or empty.
        if (request.getOrderId() == null || request.getOrderId().isEmpty()) {
            throw new IllegalArgumentException("Order ID is required");
        }
        // Make sure the amount is greater than 0 and not null.
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
        // Make sure the email is not null or empty.
        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
    }
}
