package org.trustly.paymentservice.query.trustlyOrder;


import org.trustly.paymentservice.domain.trustlyOrder.OrderState;
import org.trustly.paymentservice.domain.trustlyOrder.OrderType;
import org.trustly.paymentservice.domain.trustlyOrder.events.*;
import org.trustly.paymentservice.query.trustlyOrder.enteties.TrustlyNotification;
import org.trustly.paymentservice.query.trustlyOrder.enteties.TrustlyOrder;
import org.trustly.paymentservice.query.trustlyOrder.enteties.TrustlyOrderRepository;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;
import org.trustly.paymentservice.domain.trustlyOrder.OrderState;
import org.trustly.paymentservice.domain.trustlyOrder.OrderType;
import org.trustly.paymentservice.domain.trustlyOrder.events.AccountNotificationReceivedEvent;
import org.trustly.paymentservice.domain.trustlyOrder.events.OrderAssignedTrustlyIdEvent;
import org.trustly.paymentservice.domain.trustlyOrder.events.OrderCanceledEvent;
import org.trustly.paymentservice.domain.trustlyOrder.events.OrderCompletedEvent;
import org.trustly.paymentservice.domain.trustlyOrder.events.OrderCreatedEvent;
import org.trustly.paymentservice.domain.trustlyOrder.events.SelectAccountResponseReceivedEvent;

@Component
public class TrustlyEventListener {

    private final TrustlyOrderRepository orderRepository;

    public TrustlyEventListener(TrustlyOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @EventHandler
    public void on(OrderCreatedEvent e) {
        TrustlyOrder order = new TrustlyOrder();

        order.setId(e.getHedvigOrderId());
        order.setMemberId(e.getMemberId());

        orderRepository.save(order);
    }

    @EventHandler
    public void on(OrderAssignedTrustlyIdEvent e) {
        TrustlyOrder order = orderRepository.findOne(e.getHedvigOrderId());
        order.setState(OrderState.CONFIRMED);
        order.setTrustlyOrderId(e.getTrustlyOrderId());
        orderRepository.save(order);
    }

    @EventHandler
    public void on(OrderCanceledEvent e) {
        TrustlyOrder order = orderRepository.findOne(e.getHedvigOrderId());
        order.setState(OrderState.CANCELED);
        orderRepository.save(order);
    }

    @EventHandler
    public void on(OrderCompletedEvent e) {
        TrustlyOrder order = orderRepository.findOne(e.getId());
        order.setState(OrderState.COMPLETE);
        orderRepository.save(order);
    }

    @EventHandler
    public void on(SelectAccountResponseReceivedEvent e) {
        TrustlyOrder order = orderRepository.findOne(e.getHedvigOrderId());
        order.setIframeUrl(e.getIframeUrl());
        order.setType(OrderType.SELECT_ACCOUNT);
        orderRepository.save(order);
    }

    @EventHandler
    public void on(AccountNotificationReceivedEvent e) {
        TrustlyOrder order = orderRepository.findByTrustlyOrderId(e.getTrustlyOrderId());
        TrustlyNotification notification = new TrustlyNotification();
        notification.setNotificationId(e.getNotificationId());
        order.addNotification(notification);

        notification.setAccountId(e.getAccountId());
        notification.setAddress(e.getAddress());
        notification.setBank(e.getBank());
        notification.setCity(e.getCity());
        notification.setClearingHouse(e.getClearingHouse());
        notification.setDescriptor(e.getDescriptor());
        notification.setDirectDebitMandate(e.getDirectDebitMandate());
        notification.setLastDigits(e.getLastDigits());
        notification.setName(e.getName());
        notification.setPersonId(e.getPersonId());
        notification.setZipCode(e.getZipCode());

        //orderRepository.save(order);
    }


}
