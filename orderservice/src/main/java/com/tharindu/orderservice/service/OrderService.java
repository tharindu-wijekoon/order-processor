package com.tharindu.orderservice.service;

import com.tharindu.orderservice.dto.CreateOrderRequest;
import com.tharindu.orderservice.repository.OrderRepository;
import com.tharindu.orderservice.validation.OrderValidator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderEventPublisher orderPublisher;

    public void createOrder(CreateOrderRequest request) {
//        1. Validate the order
        OrderValidator.validate(request);

//        2. Save order to DynamoDB
        orderRepository.save(request);

//        3. Publish OrderCreated event
        orderPublisher.publishOrderCreated(request);
    }
}
