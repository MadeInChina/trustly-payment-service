package org.trustly.paymentservice.domain.trustlyOrder.commands;

import java.util.UUID;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

import lombok.Value;

@Value
public class PaymentResponseReceivedCommand {
    @TargetAggregateIdentifier
    UUID hedvigOrderId;

    String url;
    String trustlyOrderId;
}
