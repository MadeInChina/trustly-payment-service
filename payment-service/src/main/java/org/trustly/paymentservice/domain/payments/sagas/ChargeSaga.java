package org.trustly.paymentservice.domain.payments.sagas;

import java.util.UUID;
import lombok.val;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.saga.EndSaga;
import org.axonframework.eventhandling.saga.SagaEventHandler;
import org.axonframework.eventhandling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;
import org.trustly.paymentservice.common.UUIDGenerator;
import org.trustly.paymentservice.domain.payments.events.ChargeCreatedEvent;
import org.trustly.paymentservice.domain.trustlyOrder.commands.CreatePaymentOrderCommand;
import org.trustly.paymentservice.services.trustly.TrustlyService;
import org.trustly.paymentservice.services.trustly.dto.PaymentRequest;

@Saga
public class ChargeSaga {
  @Autowired transient CommandGateway commandGateway;
  @Autowired transient TrustlyService trustlyService;
  @Autowired transient UUIDGenerator uuidGenerator;

  @StartSaga
  @SagaEventHandler(associationProperty = "memberId")
  @EndSaga
  public void on(ChargeCreatedEvent e) {
    val hedvigOrderId =
        (UUID)
            commandGateway.sendAndWait(
                new CreatePaymentOrderCommand(
                    uuidGenerator.generateRandom(),
                    e.getTransactionId(),
                    e.getMemberId(),
                    e.getAmount(),
                    e.getAccountId()));
    trustlyService.startPaymentOrder(
        new PaymentRequest(e.getMemberId(), e.getAmount(), e.getAccountId(), e.getEmail()),
        hedvigOrderId);
  }
}
