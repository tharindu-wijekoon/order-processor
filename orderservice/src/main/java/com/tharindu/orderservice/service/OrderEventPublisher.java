package com.tharindu.orderservice.service;

import com.tharindu.orderservice.dto.CreateOrderRequest;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;

@Service
public class OrderEventPublisher {
    private final SnsClient snsClient;
    // Get the topic arn from application.yaml
    @Value("${sns.topicArn}")
    private String topicArn;

    public OrderEventPublisher(SnsClient snsClient) {
        this.snsClient = snsClient;
    }

    public void publishOrderCreated(CreateOrderRequest order) {
        // Create the message payload as a JSON string
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

        // Create the PublishRequest with message attributes
        PublishRequest request = PublishRequest.builder()
                .topicArn(topicArn)
                .message(payload)
                // Add message attributes to indicate the event type for easy filtering by subscribers
                .messageAttributes(Map.of(
                    "eventType", MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue("OrderCreated")
                        .build()
                ))
                .build();

        // Publish the message to SNS
        snsClient.publish(request);
    }
}
