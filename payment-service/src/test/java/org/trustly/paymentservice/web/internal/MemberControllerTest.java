package org.trustly.paymentservice.web.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.trustly.paymentService.trustly.SignedAPI;
import org.trustly.paymentService.trustly.data.response.Error;
import org.trustly.paymentService.trustly.data.response.Response;
import org.trustly.paymentService.trustly.data.response.Result;
import org.trustly.paymentservice.PaymentServiceTestConfiguration;
import org.trustly.paymentservice.common.UUIDGenerator;
import org.trustly.paymentservice.domain.payments.commands.CreateMemberCommand;
import org.trustly.paymentservice.domain.payments.commands.UpdateTrustlyAccountCommand;
import org.trustly.paymentservice.domain.payments.events.ChargeCreatedEvent;
import org.trustly.paymentservice.domain.payments.events.ChargeCreationFailedEvent;
import org.trustly.paymentservice.domain.trustlyOrder.events.PaymentErrorReceivedEvent;
import org.trustly.paymentservice.domain.trustlyOrder.events.PaymentResponseReceivedEvent;
import org.trustly.paymentservice.web.dtos.ChargeRequest;
import lombok.val;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.javamoney.moneta.Money;
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
import org.springframework.transaction.annotation.Transactional;

import javax.money.MonetaryAmount;
import java.util.HashMap;
import java.util.stream.Collectors;
import org.trustly.paymentService.trustly.SignedAPI;
import org.trustly.paymentService.trustly.data.response.Error;
import org.trustly.paymentService.trustly.data.response.Response;
import org.trustly.paymentService.trustly.data.response.Result;
import org.trustly.paymentservice.common.UUIDGenerator;
import org.trustly.paymentservice.domain.payments.commands.CreateMemberCommand;
import org.trustly.paymentservice.domain.payments.commands.UpdateTrustlyAccountCommand;
import org.trustly.paymentservice.domain.payments.events.ChargeCreatedEvent;
import org.trustly.paymentservice.domain.payments.events.ChargeCreationFailedEvent;
import org.trustly.paymentservice.domain.trustlyOrder.events.PaymentErrorReceivedEvent;
import org.trustly.paymentservice.domain.trustlyOrder.events.PaymentResponseReceivedEvent;
import org.trustly.paymentservice.trustly.testHelpers.TestData;
import org.trustly.paymentservice.web.dtos.ChargeRequest;

import static org.trustly.paymentservice.trustly.testHelpers.TestData.*;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@ContextConfiguration(classes = PaymentServiceTestConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class MemberControllerTest {
    @Autowired
    private
    MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CommandGateway commandGateway;

    @Autowired
    private EventStore eventStore;

    @MockBean
    private SignedAPI signedApi;

    @MockBean
    private UUIDGenerator uuidGenerator;

    private static final String EMAIL = "test@hedvig.com";
    private static final MonetaryAmount MONETARY_AMOUNT = Money.of(100, "SEK");
    private static final String ORDER_ID = "123";
    private static final String PAYMENT_URL = "testurl";

    @Test
    public void givenMemberWithoutDirectDebitMandate_WhenCreatingCharge_ThenShouldReturnForbidden() throws Exception {
        commandGateway.sendAndWait(new CreateMemberCommand(TestData.MEMBER_ID));

        val chargeRequest = new ChargeRequest(MONETARY_AMOUNT, EMAIL);

        mockMvc
            .perform(
                post(
                    String.format(
                        "/_/members/%s/charge",
                        TestData.MEMBER_ID
                    )
                )
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(chargeRequest))
            )
            .andExpect(status().is(403));

        val memberEvents = eventStore
            .readEvents(TestData.MEMBER_ID)
            .asStream()
            .collect(Collectors.toList());

        assertTrue(memberEvents.get(1).getPayload() instanceof ChargeCreationFailedEvent);
    }

    @Test
    public void givenMemberWithDirectDebitMandate_WhenCreatingChargeAndTrustlyReturnsSuccess_ThenShouldReturnAccepted() throws Exception {
        commandGateway.sendAndWait(new CreateMemberCommand(TestData.MEMBER_ID));
        commandGateway.sendAndWait(new UpdateTrustlyAccountCommand(
                TestData.MEMBER_ID,
                TestData.HEDVIG_ORDER_ID,
                TestData.TRUSTLY_ACCOUNT_ID,
                TestData.TOLVANSSON_STREET,
                TestData.TRUSTLY_ACCOUNT_BANK,
                TestData.TOLVANSSON_CITY,
                TestData.TRUSTLY_ACCOUNT_CLEARING_HOUSE,
                TestData.TRUSTLY_ACCOUNT_DESCRIPTOR,
                TestData.TRUSTLY_ACCOUNT_DIRECTDEBIT_TRUE,
                TestData.TRUSTLY_ACCOUNT_LAST_DIGITS,
                TestData.TOLVAN_FIRST_NAME + " " + TestData.TOLVANSSON_LAST_NAME,
                TestData.TOLVANSSON_SSN,
                TestData.TOLVANSSON_ZIP
                ));

        mockTrustlyApiResponse(true);
        given(uuidGenerator.generateRandom())
            .willReturn(TestData.HEDVIG_ORDER_ID);

        val chargeRequest = new ChargeRequest(MONETARY_AMOUNT, EMAIL);

        mockMvc
            .perform(
                post(
                    String.format(
                        "/_/members/%s/charge",
                        TestData.MEMBER_ID
                    )
                )
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(chargeRequest))
            )
            .andExpect(status().is(202));

        val memberEvents = eventStore
            .readEvents(TestData.MEMBER_ID)
            .asStream()
            .collect(Collectors.toList());
        assertTrue(memberEvents.get(2).getPayload() instanceof ChargeCreatedEvent);

        val trustlyOrderEvents = eventStore
            .readEvents(TestData.HEDVIG_ORDER_ID.toString())
            .asStream()
            .collect(Collectors.toList());
        assertTrue(trustlyOrderEvents.get(3).getPayload() instanceof PaymentResponseReceivedEvent);
    }

    @Test
    public void givenMemberWithDirectDebitMandate_WhenCreatingChargeAndTrustlyReturnsError_ThenShouldReturnAccepted() throws Exception {
        commandGateway.sendAndWait(new CreateMemberCommand(TestData.MEMBER_ID));
        commandGateway.sendAndWait(new UpdateTrustlyAccountCommand(
                TestData.MEMBER_ID,
                TestData.HEDVIG_ORDER_ID,
                TestData.TRUSTLY_ACCOUNT_ID,
                TestData.TOLVANSSON_STREET,
                TestData.TRUSTLY_ACCOUNT_BANK,
                TestData.TOLVANSSON_CITY,
                TestData.TRUSTLY_ACCOUNT_CLEARING_HOUSE,
                TestData.TRUSTLY_ACCOUNT_DESCRIPTOR,
                TestData.TRUSTLY_ACCOUNT_DIRECTDEBIT_TRUE,
                TestData.TRUSTLY_ACCOUNT_LAST_DIGITS,
                TestData.TOLVAN_FIRST_NAME + " " + TestData.TOLVANSSON_LAST_NAME,
                TestData.TOLVANSSON_SSN,
                TestData.TOLVANSSON_ZIP
                ));

        mockTrustlyApiResponse(false);
        given(uuidGenerator.generateRandom())
            .willReturn(TestData.HEDVIG_ORDER_ID);

        val chargeRequest = new ChargeRequest(MONETARY_AMOUNT, EMAIL);

        mockMvc
            .perform(
                post(
                    String.format(
                        "/_/members/%s/charge",
                        TestData.MEMBER_ID
                    )
                )
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(chargeRequest))
            )
            .andExpect(status().is(202));

        val memberEvents = eventStore
            .readEvents(TestData.MEMBER_ID)
            .asStream()
            .collect(Collectors.toList());
        assertTrue(memberEvents.get(2).getPayload() instanceof ChargeCreatedEvent);

        val trustlyOrderEvents = eventStore
            .readEvents(TestData.HEDVIG_ORDER_ID.toString())
            .asStream()
            .collect(Collectors.toList());
        assertTrue(trustlyOrderEvents.get(2).getPayload() instanceof PaymentErrorReceivedEvent);
    }

    private void mockTrustlyApiResponse(boolean shouldSucceed) {
        val trustlyResultData = new HashMap<String, Object>();
        trustlyResultData.put("orderid", ORDER_ID);
        trustlyResultData.put("url", PAYMENT_URL);
        val trustlyResult = new Result();
        trustlyResult.setData(trustlyResultData);
        val trustlyApiResponse = new Response();
        if (shouldSucceed) {
            trustlyApiResponse.setResult(trustlyResult);
        } else {
            val error = new Error();
            trustlyApiResponse.setError(error);
        }

        given(
            signedApi.sendRequest(
                any()
            )
        )
        .willReturn(trustlyApiResponse);
    }
}
