# Order Processor

A microservices-based order processing system built with event-driven architecture using AWS services. The system handles order creation, payment processing, and customer notifications through loosely coupled services communicating via Amazon SNS.

## Architecture

The system consists of three main services that communicate asynchronously through Amazon SNS topics:

```
Order Service (Java/Spring Boot)
    ↓ Creates order & publishes to SNS
Payment Service (Go Lambda)
    ↓ Processes payment & updates status
Notification Service (TypeScript Lambda)
    ↓ Sends customer email notifications
```

**Data Flow:**
1. Customer places order via Order Service REST API
2. Order Service saves to DynamoDB and publishes `OrderCreated` event
3. Payment Service consumes event, simulates payment (70% success rate), updates order status
4. Payment Service publishes `PaymentSuccessful` or `PaymentFailed` event
5. Notification Service consumes result and sends email to customer

## Services

### Order Service
- **Technology**: Java 21, Spring Boot
- **Purpose**: REST API for order creation, DynamoDB persistence, SNS event publishing
- **Deployment**: Docker + Kubernetes

### Payment Service
- **Technology**: Go 1.25.7, AWS Lambda
- **Purpose**: Payment processing simulation, order status updates
- **Deployment**: Serverless Lambda function

### Notification Service
- **Technology**: TypeScript, Node.js, AWS Lambda
- **Purpose**: Customer email notifications for payment outcomes
- **Deployment**: Serverless Lambda function

## Prerequisites

- Java 21 (for Order Service)
- Go 1.25.7 (for Payment Service)
- Node.js 18+ (for Notification Service)
- AWS Account with appropriate permissions
- Docker & Kubernetes (for Order Service deployment)

## Getting Started

1. Clone the repository:
   ```bash
   git clone https://github.com/tharindu-wijekoon/order-processor.git
   cd order-processor
   ```

2. Set up AWS resources:
   - Create DynamoDB table: `Orders`
   - Create SNS topic: `order-events`
   - Configure IAM roles for Lambda functions

3. Build and deploy each service according to their individual README files

## Deployment

- **Order Service**: Use Kubernetes manifests in `orderservice/k8/`
- **Payment Service**: Deploy as Lambda function with SNS trigger
- **Notification Service**: Deploy as Lambda function with SNS trigger

See individual service READMEs for detailed deployment instructions.

## Development

Each service has its own directory with complete setup and deployment instructions. The services are designed to be developed and deployed independently while maintaining event-driven communication.

## Contributing

1. Follow the architecture patterns established in each service
2. Ensure event contracts are maintained across services
3. Test integrations end-to-end
4. Update documentation as needed