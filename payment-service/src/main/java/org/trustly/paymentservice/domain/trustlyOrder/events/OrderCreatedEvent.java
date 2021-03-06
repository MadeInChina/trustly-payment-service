package org.trustly.paymentservice.domain.trustlyOrder.events;

import lombok.Value;

import java.util.UUID;

@Value
public class OrderCreatedEvent {
    UUID hedvigOrderId;

    String memberId;
}
