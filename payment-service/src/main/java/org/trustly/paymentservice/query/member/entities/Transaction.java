package org.trustly.paymentservice.query.member.entities;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import org.trustly.paymentservice.domain.payments.TransactionStatus;
import org.trustly.paymentservice.domain.payments.TransactionType;

import lombok.Getter;
import lombok.Setter;
import org.trustly.paymentservice.domain.payments.TransactionStatus;
import org.trustly.paymentservice.domain.payments.TransactionType;

@Entity
@Getter
@Setter
public class Transaction {
    @Id
    UUID id;

    BigDecimal amount;
    String currency;
    Instant timestamp;
    @Enumerated(EnumType.STRING)
    TransactionType transactionType;
    @Enumerated(EnumType.STRING)
    TransactionStatus transactionStatus;
}
