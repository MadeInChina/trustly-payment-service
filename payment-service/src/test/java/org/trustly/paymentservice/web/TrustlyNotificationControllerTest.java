package org.trustly.paymentservice.web;

import com.google.gson.Gson;
import org.trustly.paymentService.trustly.NotificationHandler;
import org.trustly.paymentservice.PaymentServiceTestConfiguration;
import org.trustly.paymentservice.domain.trustlyOrder.commands.CreatePaymentOrderCommand;
import org.trustly.paymentservice.domain.trustlyOrder.commands.PaymentResponseReceivedCommand;
import javax.transaction.Transactional;
import lombok.val;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.trustly.paymentservice.trustly.testHelpers.TestData.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;

import java.util.UUID;
import org.trustly.paymentService.trustly.NotificationHandler;
import org.trustly.paymentservice.domain.trustlyOrder.commands.CreatePaymentOrderCommand;
import org.trustly.paymentservice.domain.trustlyOrder.commands.PaymentResponseReceivedCommand;
import org.trustly.paymentservice.trustly.testHelpers.TestData;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@ContextConfiguration(classes = PaymentServiceTestConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class TrustlyNotificationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Gson gson;

    @Autowired
    private EventStore eventStore;

    @Autowired
    private CommandGateway commandGateway;

    @MockBean
    private NotificationHandler notificationHandler;

    @Test
    public void givenAConfirmedTrustlyChargeOrder_whenReceivingNotification_thenShouldReturnOk() throws Exception {
        commandGateway.sendAndWait(new CreatePaymentOrderCommand(
            TestData.HEDVIG_ORDER_ID,
            UUID.fromString(TestData.TRANSACTION_ID),
            TestData.MEMBER_ID,
            TestData.TRANSACTION_AMOUNT,
            TestData.TRUSTLY_ACCOUNT_ID));
        commandGateway.sendAndWait(new PaymentResponseReceivedCommand(
            TestData.HEDVIG_ORDER_ID,
            TestData.TRANSACTION_URL,
            TestData.TRUSTLY_ORDER_ID
        ));

        val request = TestData.createTrustlyCreditNotificationRequest();
        given(notificationHandler.handleNotification(any()))
            .willReturn(request);

        mockMvc
            .perform(
                post("/hooks/trustly/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(gson.toJson(request))
            )
            .andExpect(status().isOk());
    }
}
