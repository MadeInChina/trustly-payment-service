package org.trustly.paymentservice.services.payments;

import org.trustly.paymentservice.common.UUIDGenerator;
import org.trustly.paymentservice.domain.payments.commands.CreateChargeCommand;
import org.trustly.paymentservice.domain.payments.commands.CreateMemberCommand;
import org.trustly.paymentservice.services.payments.dto.ChargeMemberRequest;
import java.time.Instant;
import lombok.val;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private final CommandGateway commandGateway;
    private final UUIDGenerator uuidGenerator;

    public PaymentService(CommandGateway commandGateway, UUIDGenerator uuidGenerator) {
        this.commandGateway = commandGateway;
        this.uuidGenerator = uuidGenerator;
    }

    public void createMember(String memberId) {
        commandGateway.sendAndWait(new CreateMemberCommand(memberId));
    }

    public boolean chargeMember(ChargeMemberRequest request) {
        val transactionId = uuidGenerator.generateRandom();
        return commandGateway.sendAndWait(new CreateChargeCommand(
            request.getMemberId(),
            transactionId,
            request.getAmount(),
            Instant.now(),
            request.getEmail()
        ));
    }
}
