import { SNSEvent, SNSHandler } from "aws-lambda";

type EventType = "PaymentSuccessful" | "PaymentFailed";

interface PaymentResultEvent {
    eventType: EventType;
    orderId: string;
    amount: number;
    email: string;
    transactionId: string;
}

export const handler: SNSHandler = async (event: SNSEvent) => {
    for (const record of event.Records) {
        const snsMessage = record.Sns;

        const messageBody: PaymentResultEvent = JSON.parse(snsMessage.Message)

        await mockSendEmail(messageBody)
    }
};

async function mockSendEmail(paymentResult: PaymentResultEvent) {
    const subject = paymentResult.eventType === "PaymentSuccessful" ? "Payment Successful" : "Payment Failed";

    const body = `
Hello,

Your payment has been processed.

Event Type     : ${paymentResult.eventType}
Order ID       : ${paymentResult.orderId}
Amount         : ${paymentResult.amount}
Transaction ID : ${paymentResult.transactionId}

Thank you.
`;

    console.log("=================================");
    console.log("Sending Email...");
    console.log("To:", paymentResult.email);
    console.log("Subject:", subject);
    console.log("Body:", body);
    console.log("=================================");
}