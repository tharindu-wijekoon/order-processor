# Notification Service

The Notification Service is an AWS Lambda function written in TypeScript that handles customer email notifications in the order processing microservices architecture. It consumes payment result events from Amazon SNS and sends appropriate email notifications to customers based on payment success or failure outcomes.

## Features

- **Event-Driven Notifications**: Consumes payment events from SNS and triggers email notifications
- **Type-Safe Processing**: Full TypeScript implementation with strict type checking
- **Dynamic Email Templates**: Generates personalized email content based on payment status
- **AWS Lambda Optimized**: Serverless deployment with automatic scaling
- **Comprehensive Logging**: Detailed logging for monitoring and debugging
- **Mock Email Integration**: Ready for integration with real email services (SES, SendGrid, etc.)
- **Error Handling**: Robust error handling and logging for failed notifications

## Architecture

The Notification Service completes the event-driven order processing pipeline:

```
Order Service (Java)
    ↓ Creates order
SNS Topic (order-events)
    ↓ OrderCreated event
Payment Service (Go)
    ↓ Processes payment
SNS Topic (order-events)
    ↓ PaymentSuccessful/PaymentFailed event
Notification Service (TypeScript)
    ↓ Sends email notification
Customer Email
```

## Prerequisites

- Node.js 18.x or higher
- npm or yarn
- AWS CLI configured with appropriate permissions
- AWS Account with Lambda and SNS access
- TypeScript 5.9.3 or higher

## Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/tharindu-wijekoon/order-processor.git
   cd order-processor/notification-service
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

## Configuration

The service currently requires no environment variables as it processes events directly from SNS. However, for production deployment with real email services, you may need:

### Optional Environment Variables (for email service integration)

- `EMAIL_API_KEY`: API key for email service provider
- `EMAIL_FROM`: Sender email address
- `EMAIL_SERVICE_URL`: Email service endpoint

### AWS Resources Setup

1. **SNS Topic**: Ensure the `order-events` topic exists and receives payment result events from the Payment Service

2. **IAM Permissions**: Lambda execution role needs:
   - `sns:Subscribe` on the order-events topic (for subscription setup)
   - Permissions for your chosen email service (SES, etc.)

## Building and Deployment

### Build the Project

```bash
# Compile TypeScript to JavaScript
npm run build
```

This creates the `dist/` directory with compiled JavaScript files.

### Deploy to AWS Lambda

Using AWS CLI:

```bash
# Create deployment package
cd dist
zip -r notification.zip .

# Create Lambda function
aws lambda create-function \
  --function-name notification-service \
  --runtime nodejs18.x \
  --role arn:aws:iam::YOUR_ACCOUNT_ID:role/lambda-execution-role \
  --handler index.handler \
  --zip-file fileb://notification.zip
```

Or update existing function:

```bash
aws lambda update-function-code \
  --function-name notification-service \
  --zip-file fileb://notification.zip
```

### SNS Subscription

Subscribe the Lambda to the order-events SNS topic:

```bash
aws sns subscribe \
  --topic-arn arn:aws:sns:us-east-1:YOUR_ACCOUNT_ID:order-events \
  --protocol lambda \
  --notification-endpoint arn:aws:lambda:us-east-1:YOUR_ACCOUNT_ID:function:notification-service
```

## Functionality

### Event Processing Flow

1. **Receive Payment Event**: Lambda triggered by SNS message with payment result
2. **Parse Event Data**: Extract order details, payment status, and customer email
3. **Generate Email Content**: Create personalized subject and body based on payment outcome
4. **Send Notification**: Log email details (mock implementation) or send via email service

### Supported Event Types

**PaymentSuccessful Event:**
```json
{
  "eventType": "PaymentSuccessful",
  "orderId": "ORD-001",
  "amount": 99.99,
  "email": "customer@example.com",
  "transactionId": "ORD-001_2025-03-27T10:30:45Z"
}
```

**PaymentFailed Event:**
```json
{
  "eventType": "PaymentFailed",
  "orderId": "ORD-001",
  "amount": 99.99,
  "email": "customer@example.com",
  "transactionId": "ORD-001_2025-03-27T10:30:45Z"
}
```

### Email Templates

**Success Email:**
- Subject: "Payment Successful - Order ORD-001"
- Body: Confirmation message with order details and transaction ID

**Failure Email:**
- Subject: "Payment Failed - Order ORD-001"
- Body: Failure notification with order details and next steps

## Local Development and Testing

### Run Locally

```bash
# Build the project
npm run build

# Test with sample event (requires AWS SAM CLI or local Lambda runtime)
# Create a test-event.json file with SNS event structure
```

### Testing with AWS SAM

Create a `template.yaml` for SAM:

```yaml
AWSTemplateFormatVersion: '2010-09-09'
Transform: AWS::Serverless-2016-10-31
Description: Notification Service Lambda

Resources:
  NotificationFunction:
    Type: AWS::Serverless::Function
    Properties:
      FunctionName: notification-service
      Runtime: nodejs18.x
      Handler: index.handler
      CodeUri: dist/
      Events:
        SNSEvent:
          Type: SNS
          Properties:
            Topic: !Ref OrderEventsTopic
```

Deploy with SAM:

```bash
sam build
sam deploy --guided
```

## Project Structure

```
notification-service/
├── src/
│   └── handler.ts        # Lambda handler function
├── dist/                 # Compiled JavaScript (after build)
├── package.json          # Node.js dependencies and scripts
├── tsconfig.json         # TypeScript configuration
└── .gitignore           # Git ignore rules
```

## Monitoring and Logging

- **CloudWatch Logs**: All execution logs are available in CloudWatch
- **X-Ray**: Enable X-Ray tracing for performance monitoring
- **Metrics**: Monitor invocation count, duration, and error rates
- **Custom Logs**: Email sending attempts and outcomes are logged

## Extending the Service

### Adding Real Email Integration

Replace the `mockSendEmail` function with actual email service calls:

```typescript
// Example with AWS SES
import { SESClient, SendEmailCommand } from "@aws-sdk/client-ses";

const sesClient = new SESClient({ region: "us-east-1" });

async function sendEmail(to: string, subject: string, body: string) {
  const command = new SendEmailCommand({
    Source: process.env.EMAIL_FROM,
    Destination: { ToAddresses: [to] },
    Message: {
      Subject: { Data: subject },
      Body: { Text: { Data: body } }
    }
  });
  await sesClient.send(command);
}
```

### Adding Retry Logic

Implement exponential backoff for failed email deliveries:

```typescript
// Add retry mechanism with AWS Lambda destinations or DLQ
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Run `npm run build` to ensure TypeScript compilation
5. Test locally and ensure AWS deployment works
6. Submit a pull request

## License

This project is licensed under the MIT License.