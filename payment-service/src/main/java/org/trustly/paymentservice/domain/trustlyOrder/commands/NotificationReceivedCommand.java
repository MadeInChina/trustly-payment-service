package org.trustly.paymentservice.domain.trustlyOrder.commands;

import org.trustly.paymentService.trustly.data.notification.Notification;
import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

import java.util.UUID;

@Value
public class NotificationReceivedCommand {
    @TargetAggregateIdentifier UUID hedvigOrderId;

    Notification notification;

}
