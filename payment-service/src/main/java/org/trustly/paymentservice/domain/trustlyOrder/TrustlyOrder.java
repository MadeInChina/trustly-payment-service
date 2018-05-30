package org.trustly.paymentservice.domain.trustlyOrder;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trustly.paymentService.trustly.data.response.Error;
import org.trustly.paymentservice.domain.trustlyOrder.commands.AccountNotificationReceivedCommand;
import org.trustly.paymentservice.domain.trustlyOrder.commands.CancelNotificationReceivedCommand;
import org.trustly.paymentservice.domain.trustlyOrder.commands.CreateOrderCommand;
import org.trustly.paymentservice.domain.trustlyOrder.commands.CreatePaymentOrderCommand;
import org.trustly.paymentservice.domain.trustlyOrder.commands.CreditNotificationReceivedCommand;
import org.trustly.paymentservice.domain.trustlyOrder.commands.PaymentErrorReceivedCommand;
import org.trustly.paymentservice.domain.trustlyOrder.commands.PaymentResponseReceivedCommand;
import org.trustly.paymentservice.domain.trustlyOrder.commands.PendingNotificationReceivedCommand;
import org.trustly.paymentservice.domain.trustlyOrder.commands.SelectAccountResponseReceivedCommand;
import org.trustly.paymentservice.domain.trustlyOrder.events.AccountNotificationReceivedEvent;
import org.trustly.paymentservice.domain.trustlyOrder.events.CreditNotificationReceivedEvent;
import org.trustly.paymentservice.domain.trustlyOrder.events.ExternalTransactionIdAssignedEvent;
import org.trustly.paymentservice.domain.trustlyOrder.events.NotificationReceivedEvent;
import org.trustly.paymentservice.domain.trustlyOrder.events.OrderAssignedTrustlyIdEvent;
import org.trustly.paymentservice.domain.trustlyOrder.events.OrderCanceledEvent;
import org.trustly.paymentservice.domain.trustlyOrder.events.OrderCompletedEvent;
import org.trustly.paymentservice.domain.trustlyOrder.events.OrderCreatedEvent;
import org.trustly.paymentservice.domain.trustlyOrder.events.PaymentErrorReceivedEvent;
import org.trustly.paymentservice.domain.trustlyOrder.events.PaymentResponseReceivedEvent;
import org.trustly.paymentservice.domain.trustlyOrder.events.PendingNotificationReceivedEvent;
import org.trustly.paymentservice.domain.trustlyOrder.events.SelectAccountResponseReceivedEvent;

@Aggregate
public class TrustlyOrder {

  Logger log = LoggerFactory.getLogger(TrustlyOrder.class);

  @AggregateIdentifier private UUID id;
  private String trustlyOrderId;
  private OrderType orderType;
  private OrderState orderState;
  private String memberId;
  private UUID externalTransactionId;
  private List<Error> errors =
      new ArrayList<org.trustly.paymentService.trustly.data.response.Error>();
  private TreeSet<String> handledNotifications = new TreeSet<>();

  public TrustlyOrder() {}

  @CommandHandler
  public TrustlyOrder(CreateOrderCommand cmd) {

    apply(new OrderCreatedEvent(cmd.getHedvigOrderId(), cmd.getMemberId()));
  }

  @CommandHandler
  public TrustlyOrder(CreatePaymentOrderCommand cmd) {
    apply(new OrderCreatedEvent(cmd.getHedvigOrderId(), cmd.getMemberId()));
    apply(new ExternalTransactionIdAssignedEvent(cmd.getHedvigOrderId(), cmd.getTransactionId()));
  }

  @CommandHandler
  public void cmd(SelectAccountResponseReceivedCommand cmd) {
    apply(new OrderAssignedTrustlyIdEvent(cmd.getHedvigOrderId(), cmd.getTrustlyOrderId()));
    apply(new SelectAccountResponseReceivedEvent(cmd.getHedvigOrderId(), cmd.getIframeUrl()));
  }

  @CommandHandler
  public void cmd(PaymentResponseReceivedCommand cmd) {
    apply(new OrderAssignedTrustlyIdEvent(cmd.getHedvigOrderId(), cmd.getTrustlyOrderId()));
    apply(new PaymentResponseReceivedEvent(cmd.getHedvigOrderId(), cmd.getUrl()));
  }

  @CommandHandler
  public void cmd(PaymentErrorReceivedCommand cmd) {
    apply(new PaymentErrorReceivedEvent(cmd.getHedvigOrderId(), cmd.getError()));
  }

  @CommandHandler
  public void cmd(AccountNotificationReceivedCommand cmd) {

    if (handledNotifications.contains(cmd.getNotificationId())) {
      return;
    }

    apply(
        new AccountNotificationReceivedEvent(
            this.id,
            this.memberId,
            cmd.getNotificationId(),
            cmd.getTrustlyOrderId(),
            cmd.getAccountId(),
            cmd.getAddress(),
            cmd.getBank(),
            cmd.getCity(),
            cmd.getClearingHouse(),
            cmd.getDescriptor(),
            cmd.isDirectDebitMandateActivated(),
            cmd.getLastDigits(),
            cmd.getName(),
            cmd.getPersonId(),
            cmd.getZipCode()));
    markOrderComplete();
  }

  @CommandHandler
  public void cmd(CancelNotificationReceivedCommand cmd) {
    apply(new OrderCanceledEvent(this.id));
  }

  @CommandHandler
  public void cmd(PendingNotificationReceivedCommand cmd) {
    apply(
        new PendingNotificationReceivedEvent(
            cmd.getHedvigOrderId(),
            cmd.getNotificationId(),
            cmd.getTrustlyOrderId(),
            cmd.getAmount(),
            cmd.getMemberId(),
            cmd.getTimestamp()));
  }

  @CommandHandler
  public void cmd(CreditNotificationReceivedCommand cmd) {
    apply(
        new CreditNotificationReceivedEvent(
            this.id,
            this.externalTransactionId,
            cmd.getNotificationId(),
            cmd.getTrustlyOrderId(),
            cmd.getMemberId(),
            cmd.getAmount(),
            cmd.getTimestamp()));

    markOrderComplete();
  }

  public void markOrderComplete() {
    if (orderState == OrderState.CONFIRMED) {
      apply(new OrderCompletedEvent(this.id));
    }
  }

  @EventSourcingHandler
  public void on(OrderCreatedEvent e) {
    this.id = e.getHedvigOrderId();
    this.memberId = e.getMemberId();
  }

  @EventSourcingHandler
  public void on(OrderAssignedTrustlyIdEvent e) {
    this.trustlyOrderId = e.getTrustlyOrderId();
    this.orderState = OrderState.CONFIRMED;
  }

  @EventSourcingHandler
  public void on(SelectAccountResponseReceivedEvent e) {
    this.orderType = OrderType.SELECT_ACCOUNT;
  }

  @EventSourcingHandler
  public void on(PaymentResponseReceivedEvent e) {
    this.orderType = OrderType.CHARGE;
  }

  @EventSourcingHandler
  public void on(PaymentErrorReceivedEvent e) {
    this.orderType = OrderType.CHARGE;
    this.errors.add(e.getError());
  }

  @EventSourcingHandler
  public void on(OrderCompletedEvent e) {
    this.orderState = OrderState.COMPLETE;
  }

  @EventSourcingHandler
  public void on(OrderCanceledEvent e) {
    this.orderState = OrderState.CANCELED;
  }

  @EventSourcingHandler
  public void on(ExternalTransactionIdAssignedEvent e) {
    this.externalTransactionId = e.getTransactionId();
  }

  @EventSourcingHandler
  public void on(AccountNotificationReceivedEvent e) {
    handledNotifications.add(e.getNotificationId());
  }

  @EventSourcingHandler
  public void on(NotificationReceivedEvent e) {
    handledNotifications.add(e.getNotificationId());
  }
}
