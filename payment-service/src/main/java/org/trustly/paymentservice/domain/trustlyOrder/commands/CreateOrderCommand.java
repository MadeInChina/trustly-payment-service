package org.trustly.paymentservice.domain.trustlyOrder.commands;

import lombok.Value;

import java.util.UUID;

@Value
public class CreateOrderCommand {

    String memberId;

    UUID hedvigOrderId;
}
