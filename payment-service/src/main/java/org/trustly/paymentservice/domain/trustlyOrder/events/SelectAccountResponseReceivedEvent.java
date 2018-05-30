package org.trustly.paymentservice.domain.trustlyOrder.events;

import lombok.Value;

import java.util.UUID;

@Value
public class SelectAccountResponseReceivedEvent {

    UUID hedvigOrderId;
    String iframeUrl;

}
