# Order Service

The Order Service is a RESTful microservice built with Spring Boot that handles order creation, validation, and event publishing. It integrates with AWS services (DynamoDB for storage and SNS for event-driven communication) and is part of a larger order processing system that includes payment and notification services.

## Features

- **Order Creation**: Accepts and validates order requests via REST API
- **Data Persistence**: Stores orders in AWS DynamoDB
- **Event Publishing**: Publishes order events to AWS SNS for downstream processing
- **Input Validation**: Comprehensive validation of order data
- **Error Handling**: Global exception handling with appropriate HTTP status codes
- **AWS Integration**: Configured for DynamoDB and SNS using AWS SDK v2
- **Containerized**: Docker-ready for easy deployment
- **Kubernetes Support**: Includes K8s manifests for cloud deployment
- **Monitoring**: Spring Boot Actuator for health checks and metrics

## Architecture

The Order Service is part of an event-driven microservices architecture:

```
Order Service (Java/Spring Boot)
├── Receives order requests
├── Validates and stores in DynamoDB
└── Publishes events to SNS

    ↓ SNS Topic (order-events)

Payment Service (Go) ←─────┼─────→ Notification Service (TypeScript)
Processes payments         │         Sends email notifications
Updates order status       │
```

## Prerequisites

- Java 21 or higher
- Maven 3.6+
- AWS Account with appropriate permissions
- Docker (for containerized deployment)
- kubectl (for Kubernetes deployment)

## Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/tharindu-wijekoon/order-processor.git
   cd order-processor/orderservice
   ```

2. Build the project:
   ```bash
   ./mvnw clean compile
   ```

## Configuration

The application uses the following environment variables (configured via Kubernetes ConfigMap and Secrets):

### Required Environment Variables

- `AWS_REGION`: AWS region (e.g., us-east-1)
- `ORDERS_TABLE`: DynamoDB table name for orders
- `SNS_TOPIC_ARN`: ARN of the SNS topic for order events

### AWS Resources Setup

1. Create a DynamoDB table named `Orders` with:
   - Partition key: `orderId` (String)

2. Create an SNS topic named `order-events`

3. Ensure your AWS credentials are configured (IAM role for EC2/ECS or AWS CLI for local development)

## Running the Application

### Local Development

1. Set environment variables:
   ```bash
   export AWS_REGION=us-east-1
   export ORDERS_TABLE=Orders
   export SNS_TOPIC_ARN=arn:aws:sns:us-east-1:YOUR_ACCOUNT_ID:order-events
   ```

2. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

The service will start on port 8080.

### Docker

1. Build the Docker image:
   ```bash
   docker build -t orderservice .
   ```

2. Run the container:
   ```bash
   docker run -p 8080:8080 \
     -e AWS_REGION=us-east-1 \
     -e ORDERS_TABLE=Orders \
     -e SNS_TOPIC_ARN=arn:aws:sns:us-east-1:YOUR_ACCOUNT_ID:order-events \
     orderservice
   ```

### Kubernetes

Apply the Kubernetes manifests in the `k8/` directory:

```bash
kubectl apply -f k8/
```

This will deploy:
- Order Service deployment with 2 replicas
- LoadBalancer service exposing port 80
- ConfigMap with environment variables
- Secrets with sensitive configuration

## API Documentation

### Create Order

**Endpoint:** `POST /orders`

**Request Body:**
```json
{
  "orderId": "string",
  "amount": number,
  "email": "string"
}
```

**Response:**
- **201 Created**: Order created successfully
- **400 Bad Request**: Invalid input data
- **500 Internal Server Error**: Server error

**Example Request:**
```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD-001",
    "amount": 99.99,
    "email": "customer@example.com"
  }'
```

**Validation Rules:**
- `orderId`: Required, non-empty string
- `amount`: Required, must be greater than 0
- `email`: Required, valid email format

## Testing

Run the tests using Maven:

```bash
./mvnw test
```

The project includes unit tests for the main service components.

## Monitoring

The service includes Spring Boot Actuator endpoints:

- Health check: `GET /actuator/health`
- Metrics: `GET /actuator/metrics`
- Info: `GET /actuator/info`

## Development

### Project Structure

```
src/
├── main/
│   ├── java/com/tharindu/orderservice/
│   │   ├── OrderserviceApplication.java    # Main Spring Boot application
│   │   ├── config/                         # AWS configuration
│   │   ├── controller/                     # REST controllers
│   │   ├── dto/                           # Data transfer objects
│   │   ├── exception/                     # Exception handlers
│   │   ├── repository/                    # Data access layer
│   │   ├── service/                       # Business logic
│   │   ├── util/                          # Utility classes
│   │   └── validation/                    # Input validation
│   └── resources/
│       └── application.yaml               # Application configuration
└── test/                                  # Unit tests
```

### Key Components

- **OrderController**: Handles HTTP requests and responses
- **OrderService**: Contains business logic for order processing
- **OrderRepository**: Manages DynamoDB operations
- **OrderEventPublisher**: Publishes events to SNS
- **OrderValidator**: Validates order input data
- **AwsConfig**: Configures AWS SDK clients

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request
