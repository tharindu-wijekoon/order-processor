package com.tharindu.orderservice.controller;

import com.tharindu.orderservice.dto.CreateOrderRequest;
import com.tharindu.orderservice.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@AllArgsConstructor
public class OrderController {
    private final OrderService orderService;

    // Endpoint to create a new order
    // Accepts a CreateOrderRequest in the request body and returns a 201 Created response if successful
    // Example request body:
    // {
    //   "orderId": "12345",
    //   "amount": 100,
    //   "email": "customer@example.com"
    // }
    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody CreateOrderRequest request) {
        orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("Created");
    }
}
