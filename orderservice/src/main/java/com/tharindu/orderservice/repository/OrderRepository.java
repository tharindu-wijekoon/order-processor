package com.tharindu.orderservice.repository;

import com.tharindu.orderservice.dto.CreateOrderRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.Map;

@Repository
public class OrderRepository {
    private final DynamoDbClient dynamoDbClient;
    @Value("${dynamodb.ordersTable}")
    private String tableName;

    public OrderRepository(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    public void save(CreateOrderRequest order) {
        Map<String, AttributeValue> item = Map.of(
                "orderId", AttributeValue.fromS(order.getOrderId()),
                "amount", AttributeValue.fromN(order.getAmount().toString()),
                "email", AttributeValue.fromS(order.getEmail())
        );

        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build();

       dynamoDbClient.putItem(request);
    }
}
