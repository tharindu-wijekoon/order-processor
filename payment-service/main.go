package main

import (
	"context"
	"encoding/json"
	"log"
	"math/rand"
	"os"
	"time"

	"github.com/aws/aws-lambda-go/events"
	"github.com/aws/aws-lambda-go/lambda"
	"github.com/aws/aws-sdk-go-v2/aws"
	"github.com/aws/aws-sdk-go-v2/config"
	"github.com/aws/aws-sdk-go-v2/service/dynamodb"
	"github.com/aws/aws-sdk-go-v2/service/dynamodb/types"
	"github.com/aws/aws-sdk-go-v2/service/sns"
	snstypes "github.com/aws/aws-sdk-go-v2/service/sns/types"
)

type OrderCreatedEvent struct {
	EventType string `json:"eventType"`
	OrderId   string `json:"orderId"`
	Amount    int    `json:"amount"`
	Email     string `json:"email"`
}

type PaymentResultEvent struct {
	EventType string `json:"eventType"`
	OrderId   string `json:"orderId"`
	Amount    int    `json:"amount"`
	Email     string `json:"email"`
	TrxId     string `json:"transactionId"`
}

var (
	dynamoClient *dynamodb.Client
	snsClient    *sns.Client
	tableName    string
	topicArn     string
)

func init() {
	conf, err := config.LoadDefaultConfig(context.TODO())
	if err != nil {
		log.Fatalf("unable to load AWS config: %v", err)
	}

	dynamoClient = dynamodb.NewFromConfig(conf)
	snsClient = sns.NewFromConfig(conf)

	tableName = os.Getenv("ORDERS_TABLE")
	topicArn = os.Getenv("ORDER_TOPIC")

	if tableName == "" || topicArn == "" {
		log.Fatal("ORDERS_TABLE or SNS TOPIC env var not set")
	}
}

func UpdateOrderStatus(ctx context.Context, orderId, status string) error {
	_, err := dynamoClient.UpdateItem(ctx, &dynamodb.UpdateItemInput{
		TableName: aws.String(tableName),
		Key: map[string]types.AttributeValue{
			"orderId": &types.AttributeValueMemberS{Value: orderId},
		},
		UpdateExpression: aws.String("SET #s = :status"),
		ExpressionAttributeNames: map[string]string{
			"#s": "status",
		},
		ExpressionAttributeValues: map[string]types.AttributeValue{
			":status": &types.AttributeValueMemberS{Value: status},
		},
	})
	return err
}

func PublishPaymentEvent(
	ctx context.Context,
	order OrderCreatedEvent,
	eventType string,
) error {

	event := PaymentResultEvent{
		EventType: eventType,
		OrderId:   order.OrderId,
		Amount:    order.Amount,
		Email:     order.Email,
		TrxId:     order.OrderId + "_" + time.Now().Format(time.RFC3339),
	}

	body, _ := json.Marshal(event)

	_, err := snsClient.Publish(ctx, &sns.PublishInput{
		TopicArn: aws.String(topicArn),
		Message:  aws.String(string(body)),
		MessageAttributes: map[string]snstypes.MessageAttributeValue{
			"eventType": {
				DataType:    aws.String("String"),
				StringValue: aws.String(eventType),
			},
		},
	})
	return err
}

func handler(ctx context.Context, event events.SNSEvent) error {
	for _, record := range event.Records {
		var orderEvent OrderCreatedEvent

		err := json.Unmarshal([]byte(record.SNS.Message), &orderEvent)
		if err != nil {
			log.Printf("failed to parse SNS message: %v", err)
			continue
		}

		log.Printf("Processing payment for order %s", orderEvent.OrderId)

		// Simulate payment processing
		time.Sleep(1500 * time.Millisecond)

		success := rand.Float64() < 0.7

		var status, eventType string
		if success {
			status = "PAID"
			eventType = "PaymentSuccessful"
		} else {
			status = "FAILED"
			eventType = "PaymentFailed"
		}

		err = UpdateOrderStatus(ctx, orderEvent.OrderId, status)
		if err != nil {
			log.Printf("failed to update order %s: %v", orderEvent.OrderId, err)
			return err
		}

		err = PublishPaymentEvent(ctx, orderEvent, eventType)
		if err != nil {
			log.Printf("failed to publish payment event for order %s: %v", orderEvent.OrderId, err)
			return err
		}

		log.Printf("Order %s marked as PAID", orderEvent.OrderId)
	}

	return nil
}

func main() {
	lambda.Start(handler)
}
