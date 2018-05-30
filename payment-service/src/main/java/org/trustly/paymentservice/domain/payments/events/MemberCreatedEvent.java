package org.trustly.paymentservice.domain.payments.events;

import lombok.Value;

@Value
public class MemberCreatedEvent {
  String memberId;
}
