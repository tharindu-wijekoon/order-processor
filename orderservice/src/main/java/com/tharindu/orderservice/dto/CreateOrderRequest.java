package com.tharindu.orderservice.dto;

import lombok.Value;

@Value
public class CreateOrderRequest {
    String orderId;
    Integer amount;
    String email;
}
