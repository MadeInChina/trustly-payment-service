package org.trustly.paymentservice.domain.trustlyOrder.commands;

import java.util.UUID;

import org.trustly.paymentService.trustly.data.response.Error;

import org.axonframework.commandhandling.TargetAggregateIdentifier;

import lombok.Value;

@Value
public class PaymentErrorReceivedCommand {
    @TargetAggregateIdentifier
    UUID hedvigOrderId;

    Error error;
}
