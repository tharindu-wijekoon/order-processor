package com.tharindu.orderservice.service;

import com.tharindu.orderservice.dto.CreateOrderRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

@Service
public class OrderEventPublisher {
    private final SnsClient snsClient;
    @Value("${sns.topicArn}")
    private String topicArn;

    public OrderEventPublisher(SnsClient snsClient) {
        this.snsClient = snsClient;
    }

    public void publishOrderCreated(CreateOrderRequest order) {
        String payload = """
                {
                    "eventType": "OrderCreated",
                    "OrderId": "%s",
                    "amount": %d,
                    "email": "%s"
                }
                """.formatted(
                    order.getOrderId(),
                    order.getAmount(),
                    order.getEmail()
                );

        PublishRequest request = PublishRequest.builder()
                .topicArn(topicArn)
                .message(payload)
                .build();

        snsClient.publish(request);
    }
}
