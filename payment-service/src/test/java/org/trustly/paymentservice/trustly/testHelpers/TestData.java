package org.trustly.paymentservice.trustly.testHelpers;

import org.trustly.paymentService.trustly.commons.Currency;
import org.trustly.paymentService.trustly.commons.Method;
import org.trustly.paymentService.trustly.data.notification.Notification;
import org.trustly.paymentService.trustly.data.notification.NotificationParameters;
import org.trustly.paymentService.trustly.data.notification.notificationdata.CreditData;
import org.trustly.paymentservice.domain.payments.events.TrustlyAccountCreatedEvent;
import org.trustly.paymentservice.services.trustly.dto.DirectDebitRequest;

import org.javamoney.moneta.Money;

import java.time.Instant;
import java.util.UUID;
import javax.money.MonetaryAmount;
import lombok.val;
import org.trustly.paymentService.trustly.commons.Currency;
import org.trustly.paymentService.trustly.commons.Method;
import org.trustly.paymentService.trustly.data.notification.Notification;
import org.trustly.paymentService.trustly.data.notification.NotificationParameters;
import org.trustly.paymentService.trustly.data.notification.notificationdata.CreditData;
import org.trustly.paymentservice.domain.payments.events.TrustlyAccountCreatedEvent;
import org.trustly.paymentservice.services.trustly.dto.DirectDebitRequest;

public class TestData {
    public static final String BOT_SERVICE_TRIGGER_ID = "7fece3ca-17d9-11e8-8c15-f36f3d1de091";
    public static final String TRUSTLY_ORDER_ID = "12313213";
    public static final String TRUSTLY_ACCOUNT_ID = "456456";
    public static final String TRUSTLY_IFRAME_URL = "https://trustly.com/iframeurl...";
    public static final String TRUSTLY_NOTIFICATION_ID = "1381313";

    public static final String TRUSTLY_ACCOUNT_BANK = "Swedbank";
    public static final String TRUSTLY_ACCOUNT_DESCRIPTOR = "**145678";
    public static final String TRUSTLY_ACCOUNT_CLEARING_HOUSE = "SWEDEN";
    public static final String TRUSTLY_ACCOUNT_LAST_DIGITS = "145678";

    public static final String MEMBER_ID = "1337";

    public static final UUID HEDVIG_ORDER_ID = UUID.fromString("f1dd38f2-237f-11e8-8fc1-e74ced44b3e1");
    public static final String TOLVAN_FIRST_NAME = "Tolvan";
    public static final String TOLVANSSON_LAST_NAME = "Tolvansson";
    public static final String TOLVANSSON_SSN = "19121212-1212";
    public static final String TOLVAN_EMAIL = "tolvan@somewhere.com";
    public static final String TOLVANSSON_ZIP = "12121";
    public static final String TOLVANSSON_STREET = "Testgatan 1";
    public static final String TOLVANSSON_CITY = "Teststaden";

    public static final boolean TRUSTLY_ACCOUNT_DIRECTDEBIT_TRUE = true;
    public static final boolean TRUSTLY_ACCOUNT_DIRECTDEBIT_FALSE = true;

    public static final String TRANSACTION_ID = "0788882e-22da-11e8-b209-0f7ece059a6d";
    public static final Instant TRANSACTION_TIMESTAMP = Instant.ofEpochMilli(1482710400);
    public static final MonetaryAmount TRANSACTION_AMOUNT = Money.of(100, "SEK");
    public static final String TRANSACTION_URL = "http://www.example.com";

    public static DirectDebitRequest createDirectDebitRequest() {
        return new DirectDebitRequest(
                TOLVAN_FIRST_NAME,
                TOLVANSSON_LAST_NAME,
                TOLVANSSON_SSN,
                TOLVAN_EMAIL,
                MEMBER_ID,
                BOT_SERVICE_TRIGGER_ID);
    }

    public static TrustlyAccountCreatedEvent createTrustlyAccountCreatedEvent() {
        return new TrustlyAccountCreatedEvent(
            MEMBER_ID,
            HEDVIG_ORDER_ID,
            TRUSTLY_ACCOUNT_ID,
            TOLVANSSON_STREET,
            TRUSTLY_ACCOUNT_BANK,
            TOLVANSSON_CITY,
            TRUSTLY_ACCOUNT_CLEARING_HOUSE,
            TRUSTLY_ACCOUNT_DESCRIPTOR,
            TRUSTLY_ACCOUNT_DIRECTDEBIT_TRUE,
            TRUSTLY_ACCOUNT_LAST_DIGITS,
            TOLVAN_FIRST_NAME,
            TOLVANSSON_SSN,
            TOLVANSSON_ZIP
        );
    }

    public static Notification createTrustlyCreditNotificationRequest() {

        val data = new CreditData();
        data.setAmount("100.00");
        data.setCurrency(Currency.SEK);
        data.setEndUserId(MEMBER_ID);
        data.setTimestamp("2010-01-20 14:42:04.675645+01");
        data.setNotificationId(TRUSTLY_NOTIFICATION_ID);
        data.setMessageId(HEDVIG_ORDER_ID.toString());
        data.setOrderId(TRUSTLY_ORDER_ID);
        val params = new NotificationParameters();
        params.setData(data);
        params.setSignature("");
        params.setUUID(TRUSTLY_NOTIFICATION_ID);
        val request = new Notification();
        request.setMethod(Method.CREDIT);
        request.setParams(params);
        request.setVersion(1.1);

        return request;
    }
}
