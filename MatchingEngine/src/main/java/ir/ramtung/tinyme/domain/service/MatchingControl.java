package ir.ramtung.tinyme.domain.service;

import ir.ramtung.tinyme.domain.entity.Order;
import ir.ramtung.tinyme.domain.entity.Trade;
import ir.ramtung.tinyme.messaging.request.EnterOrderRq;

public interface MatchingControl {
    default void startNewOrder(Order order) {}
    default void startDeleteOrder(Order order) {}
    default void startUpdateOrder(Order order, EnterOrderRq updateOrderRq) {}

    default void tradeAccepted(Order newOrder, Trade trade) {}
    default void orderEnqueued(Order order) {}
    default void orderDequeued(Order order) {}

    MatchingOutcome commit(MatchResult result);
    default void rollback(MatchResult result) {}
    default void abort() {}
}
