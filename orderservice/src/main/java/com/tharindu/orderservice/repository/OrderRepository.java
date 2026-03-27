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
    // Get the value from application.yaml
    @Value("${dynamodb.ordersTable}")
    private String tableName;

    public OrderRepository(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    // Save order to DynamoDB
    public void save(CreateOrderRequest order) {
        // Map the order to DynamoDB item format
        Map<String, AttributeValue> item = Map.of(
                // orderId is the partition key in DynamoDB
                "orderId", AttributeValue.fromS(order.getOrderId()),
                "amount", AttributeValue.fromN(order.getAmount().toString()),
                "email", AttributeValue.fromS(order.getEmail())
        );

        // Create the PutItemRequest
        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build();

        // Put the item into DynamoDB
        dynamoDbClient.putItem(request);
    }
}
