package org.trustly.paymentservice.domain.trustlyOrder.events;

import java.util.UUID;

import lombok.Value;

@Value
public class PaymentResponseReceivedEvent {
    UUID hedvigOrderId;

    String url;
}
