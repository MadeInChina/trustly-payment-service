package org.trustly.paymentservice.domain.trustlyOrder.events;

import java.util.UUID;

import org.trustly.paymentService.trustly.data.response.Error;

import lombok.Value;

@Value
public class PaymentErrorReceivedEvent {
    UUID hedvigOrderId;

    Error error;
}
