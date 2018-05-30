package org.trustly.paymentservice.domain.trustlyOrder.events;

import lombok.Value;

@Value
public class TrustlyAccountCreatedEvent {
    String accountId;
}
