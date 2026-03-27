# Payment Service

The Payment Service is an AWS Lambda function written in Go that handles payment processing in the order processing microservices architecture. It consumes order creation events from Amazon SNS, simulates payment processing with a configurable success rate, updates order status in DynamoDB, and publishes payment result events back to SNS for downstream notification services.

## Features

- **Event-Driven Processing**: Consumes order events from SNS and publishes payment results
- **Payment Simulation**: Realistic payment processing with configurable success/failure rates
- **DynamoDB Integration**: Updates order status in the shared orders table
- **AWS Lambda Optimized**: Stateless, serverless deployment with Go runtime
- **Idempotent Operations**: Safe retry handling for failed operations
- **Message Filtering**: Uses SNS message attributes for efficient event routing
- **Error Handling**: Comprehensive logging and error management

## Architecture

The Payment Service is a critical component in the event-driven order processing pipeline:

```
Order Service (Java)
    ↓ Creates order
SNS Topic (order-events)
    ↓ OrderCreated event
Payment Service (Go Lambda)
    ↓ Processes payment (70% success)
DynamoDB (Orders table)
    ↓ Updates status
SNS Topic (order-events)
    ↓ PaymentSuccessful/PaymentFailed event
Notification Service (TypeScript)
    ↓ Sends email notification
```

## Prerequisites

- Go 1.25.7 or higher
- AWS CLI configured with appropriate permissions
- AWS Account with Lambda, DynamoDB, and SNS access
- Docker (optional, for local testing)

## Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/tharindu-wijekoon/order-processor.git
   cd order-processor/payment-service
   ```

2. Initialize Go modules:
   ```bash
   go mod download
   ```

## Configuration

The service requires the following environment variables:

### Required Environment Variables

- `ORDERS_TABLE`: Name of the DynamoDB table containing orders
- `ORDER_TOPIC`: ARN of the SNS topic for publishing payment result events

### AWS Resources Setup

1. **DynamoDB Table**: Ensure the `Orders` table exists with:
   - Partition key: `orderId` (String)
   - Additional attributes: `status` (String) for payment status

2. **SNS Topic**: Create or use existing `order-events` topic for event publishing

3. **IAM Permissions**: Lambda execution role needs:
   - `dynamodb:UpdateItem` on the Orders table
   - `sns:Publish` on the order-events topic

## Building and Deployment

### Build the Lambda Package

```bash
# Build for Linux (required for Lambda)
GOOS=linux GOARCH=amd64 go build -o bootstrap main.go

# Create deployment package
zip payment.zip bootstrap
```

### Deploy to AWS Lambda

Using AWS CLI:

```bash
aws lambda create-function \
  --function-name payment-service \
  --runtime provided.al2023 \
  --role arn:aws:iam::YOUR_ACCOUNT_ID:role/lambda-execution-role \
  --handler bootstrap \
  --zip-file fileb://payment.zip \
  --environment Variables="{ORDERS_TABLE=Orders,ORDER_TOPIC=arn:aws:sns:us-east-1:YOUR_ACCOUNT_ID:order-events}"
```

Or update existing function:

```bash
aws lambda update-function-code \
  --function-name payment-service \
  --zip-file fileb://payment.zip
```

### SNS Subscription

Subscribe the Lambda to the order-events SNS topic:

```bash
aws sns subscribe \
  --topic-arn arn:aws:sns:us-east-1:YOUR_ACCOUNT_ID:order-events \
  --protocol lambda \
  --notification-endpoint arn:aws:lambda:us-east-1:YOUR_ACCOUNT_ID:function:payment-service
```

## Functionality

### Event Processing Flow

1. **Receive Order Event**: Lambda triggered by SNS message with `OrderCreated` event
2. **Payment Processing**: Simulate payment with 1.5-second delay and 70% success rate
3. **Status Update**: Update order status in DynamoDB (`PAID` or `FAILED`)
4. **Publish Result**: Send `PaymentSuccessful` or `PaymentFailed` event to SNS

### Event Formats

**Input Event (OrderCreated):**
```json
{
  "eventType": "OrderCreated",
  "orderId": "ORD-001",
  "amount": 99.99,
  "email": "customer@example.com"
}
```

**Output Event (Payment Result):**
```json
{
  "eventType": "PaymentSuccessful",
  "orderId": "ORD-001",
  "amount": 99.99,
  "email": "customer@example.com",
  "transactionId": "ORD-001_2025-03-27T10:30:45Z"
}
```

### Payment Logic

- **Success Rate**: 70% of payments succeed
- **Processing Time**: 1.5 seconds simulation delay
- **Transaction ID**: Format: `{orderId}_{RFC3339_timestamp}`
- **Status Values**: `PAID` (success) or `FAILED` (failure)

## Local Development and Testing

### Run Locally

```bash
# Set environment variables
export ORDERS_TABLE=Orders
export ORDER_TOPIC=arn:aws:sns:us-east-1:YOUR_ACCOUNT_ID:order-events

# Run the function
go run main.go
```

### Testing with AWS SAM

Create a `template.yaml` for SAM:

```yaml
AWSTemplateFormatVersion: '2010-09-09'
Transform: AWS::Serverless-2016-10-31
Description: Payment Service Lambda

Resources:
  PaymentFunction:
    Type: AWS::Serverless::Function
    Properties:
      FunctionName: payment-service
      Runtime: provided.al2023
      Handler: bootstrap
      CodeUri: .
      Environment:
        Variables:
          ORDERS_TABLE: !Ref OrdersTable
          ORDER_TOPIC: !Ref OrderTopic
      Events:
        SNSEvent:
          Type: SNS
          Properties:
            Topic: !Ref OrderTopic
```

Deploy with SAM:

```bash
sam build
sam deploy --guided
```

## Monitoring and Logging

- **CloudWatch Logs**: All Lambda execution logs are available in CloudWatch
- **X-Ray**: Enable X-Ray tracing for performance monitoring
- **Metrics**: Monitor invocation count, duration, and error rates

## Project Structure

```
payment-service/
├── main.go              # Lambda handler and main logic
├── go.mod               # Go module dependencies
├── go.sum               # Dependency checksums
├── bootstrap            # Compiled Lambda binary
└── payment.zip          # Deployment package
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test locally and ensure AWS deployment works
5. Submit a pull request