package org.trustly.paymentservice.query.member;

import org.trustly.paymentservice.domain.payments.TransactionStatus;
import org.trustly.paymentservice.domain.payments.TransactionType;
import org.trustly.paymentservice.domain.payments.events.ChargeCompletedEvent;
import org.trustly.paymentservice.domain.payments.events.ChargeCreatedEvent;
import org.trustly.paymentservice.domain.payments.events.MemberCreatedEvent;
import org.trustly.paymentservice.query.member.entities.Member;
import org.trustly.paymentservice.query.member.entities.MemberRepository;
import org.trustly.paymentservice.query.member.entities.Transaction;
import java.math.BigDecimal;
import java.util.stream.Collectors;
import lombok.val;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;
import org.trustly.paymentservice.domain.payments.TransactionType;
import org.trustly.paymentservice.domain.payments.events.ChargeCompletedEvent;
import org.trustly.paymentservice.domain.payments.events.ChargeCreatedEvent;
import org.trustly.paymentservice.domain.payments.events.MemberCreatedEvent;

@Component
public class MemberEventListener {
    private final MemberRepository memberRepository;

    public MemberEventListener(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @EventHandler
    public void on(MemberCreatedEvent e) {
        val member = new Member();
        member.setId(e.getMemberId());
        memberRepository.save(member);
    }

    @EventHandler
    public void on(ChargeCreatedEvent e) {
        val member = memberRepository
            .findById(e.getMemberId())
            .orElseThrow(() -> new RuntimeException("Could not find member"));
        val transaction = new Transaction();
        transaction.setId(e.getTransactionId());
        transaction.setAmount(e.getAmount().getNumber().numberValueExact(BigDecimal.class));
        transaction.setCurrency(e.getAmount().getCurrency().getCurrencyCode());
        transaction.setTimestamp(e.getTimestamp());
        transaction.setTransactionType(TransactionType.CHARGE);
        transaction.setTransactionStatus(TransactionStatus.INITIATED);

        val transactions = member.getTransactions();
        transactions.add(transaction);
        memberRepository.save(member);
    }

    @EventHandler
    public void on(ChargeCompletedEvent e) {
        val member = memberRepository
            .findById(e.getMemberId())
            .orElseThrow(() -> new RuntimeException("Could not find member"));
        val matchingTransactions = member
            .getTransactions()
            .stream()
            .filter(t -> t.getId().equals(e.getTransactionId()))
            .collect(Collectors.toList());
        if (matchingTransactions.size() != 1) {
            throw new RuntimeException("Unexpected number of transactions");
        }

        val transaction = matchingTransactions.get(0);
        transaction.setTransactionStatus(TransactionStatus.COMPLETED);
        memberRepository.save(member);
    }
}
