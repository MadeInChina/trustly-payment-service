package org.trustly.paymentservice.domain.payments.commands;

import java.time.Instant;
import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;
import lombok.Value;
import org.axonframework.commandhandling.model.AggregateIdentifier;

@Value
public class PayoutCompletedCommand {
  @AggregateIdentifier String memberId;

  MonetaryAmount amount;
  CurrencyUnit currency;
  Instant timestamp;
}
