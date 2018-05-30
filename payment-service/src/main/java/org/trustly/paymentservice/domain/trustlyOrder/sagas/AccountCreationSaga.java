package org.trustly.paymentservice.domain.trustlyOrder.sagas;

import org.trustly.paymentservice.domain.payments.commands.UpdateTrustlyAccountCommand;
import org.trustly.paymentservice.domain.trustlyOrder.events.AccountNotificationReceivedEvent;
import lombok.val;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.saga.EndSaga;
import org.axonframework.eventhandling.saga.SagaEventHandler;
import org.axonframework.eventhandling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;
import org.trustly.paymentservice.domain.payments.commands.UpdateTrustlyAccountCommand;
import org.trustly.paymentservice.domain.trustlyOrder.events.AccountNotificationReceivedEvent;


@Saga
public class AccountCreationSaga {

    @Autowired
    transient CommandGateway commandGateway;

    @StartSaga
    @SagaEventHandler(associationProperty = "accountId")
    @EndSaga
    public void on(AccountNotificationReceivedEvent event) {

        val command = new UpdateTrustlyAccountCommand(
                event.getMemberId(),
                event.getHedvigOrderId(),
                event.getAccountId(),
                event.getAddress(),
                event.getBank(),
                event.getCity(),
                event.getClearingHouse(),
                event.getDescriptor(),
                event.getDirectDebitMandate(),
                event.getLastDigits(),
                event.getName(),
                event.getPersonId(),
                event.getZipCode());

        commandGateway.sendAndWait(command);
    }

}
