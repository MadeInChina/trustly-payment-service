package org.trustly.paymentservice.domain.trustlyOrder.commands;

import java.util.UUID;

import lombok.Value;

@Value
public class CancelNotificationReceivedCommand {
    UUID hedvigOrderId;
    String notificationId;
    String trustlyOrderId;
    String memberId;
}
