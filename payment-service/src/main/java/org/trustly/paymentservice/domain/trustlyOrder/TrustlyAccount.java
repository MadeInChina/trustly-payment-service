package org.trustly.paymentservice.domain.trustlyOrder;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trustly.paymentservice.domain.trustlyOrder.commands.CreateAccountCommand;
import org.trustly.paymentservice.domain.trustlyOrder.commands.UpdateAccountCommand;
import org.trustly.paymentservice.domain.trustlyOrder.events.TrustlyAccountCreatedEvent;
import org.trustly.paymentservice.domain.trustlyOrder.events.TrustlyAccountUpdatedEvent;

@Aggregate
public class TrustlyAccount {

  private final Logger log = LoggerFactory.getLogger(TrustlyAccount.class);

  @AggregateIdentifier private String id;

  public TrustlyAccount() {}

  @CommandHandler
  public TrustlyAccount(CreateAccountCommand cmd) {
    apply(new TrustlyAccountCreatedEvent(cmd.getAccountId()));
  }

  @CommandHandler
  public void on(UpdateAccountCommand cmd) {
    log.debug("Got UpdateAccountCommand");

    // TODO: Actually handle this event
    apply(
        new TrustlyAccountUpdatedEvent(
            cmd.getAccountId(),
            cmd.getAddress(),
            cmd.getBank(),
            cmd.getCity(),
            cmd.getClearingHouse(),
            cmd.getDescriptor(),
            cmd.getDirectDebitMandate(),
            cmd.getLastDigits(),
            cmd.getName(),
            cmd.getPersonId(),
            cmd.getZipCode()));
  }

  @EventSourcingHandler
  public void on(TrustlyAccountCreatedEvent event) {
    this.id = event.getAccountId();
  }
}
