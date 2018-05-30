package org.trustly.paymentservice.services.trustly.dto;

import java.util.UUID;
import lombok.Value;
import org.trustly.paymentservice.domain.trustlyOrder.OrderState;

@Value
public class OrderInformation {

  UUID id;

  String iframeUrl;

  OrderState state;
}
