package org.trustly.paymentservice.domain.trustlyOrder.commands;

import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

import java.util.UUID;

@Value
public class SelectAccountResponseReceivedCommand {
    @TargetAggregateIdentifier
    private final UUID hedvigOrderId;

    private final String iframeUrl;

    private final String trustlyOrderId;

}
