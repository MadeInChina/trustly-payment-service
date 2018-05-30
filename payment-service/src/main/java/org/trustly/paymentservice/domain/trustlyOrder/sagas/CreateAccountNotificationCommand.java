package org.trustly.paymentservice.domain.trustlyOrder.sagas;

import lombok.Value;

@Value
public class CreateAccountNotificationCommand {
    String accountId;
}
