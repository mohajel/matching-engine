package ir.ramtung.tinyme.domain.service;

import ir.ramtung.tinyme.creditservice.CreditService;
import ir.ramtung.tinyme.creditservice.CreditUpdate;
import ir.ramtung.tinyme.domain.entity.Order;
import ir.ramtung.tinyme.domain.entity.Side;
import ir.ramtung.tinyme.domain.entity.Trade;
import ir.ramtung.tinyme.messaging.request.EnterOrderRq;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CreditControl implements MatchingControl {
    private final CreditService creditService;
    private List<CreditUpdate> transaction = new ArrayList<>();

    private void increaseCredit(long brokerId, long amount) {
        transaction.add(new CreditUpdate(brokerId, amount));
    }
    private void decreaseCredit(long brokerId, long amount) {
        transaction.add(new CreditUpdate(brokerId, -amount));
    }

    public CreditControl(CreditService creditService) {
        this.creditService = creditService;
    }

    @Override
    public void startNewOrder(Order order) {
        transaction = new ArrayList<>();
    }

    @Override
    public void startDeleteOrder(Order order) {
        transaction = new ArrayList<>();
    }

    @Override
    public void startUpdateOrder(Order order, EnterOrderRq updateOrderRq) {
        transaction = new ArrayList<>();
    }

    @Override
    public void tradeAccepted(Order newOrder, Trade trade) {
        if (newOrder.getSide() == Side.BUY)
            decreaseCredit(trade.getBuy().getBroker().getBrokerId(), trade.getTradedValue());
        increaseCredit(trade.getSell().getBroker().getBrokerId(), trade.getTradedValue());
    }

    @Override
    public void orderDequeued(Order order) {
        if (order.getSide() == Side.BUY)
            increaseCredit(order.getBroker().getBrokerId(), order.getValue());
    }
    @Override
    public void orderEnqueued(Order order) {
        if (order.getSide() == Side.BUY) {
            decreaseCredit(order.getBroker().getBrokerId(), order.getValue());
        }
    }

    @Override
    public MatchingOutcome commit(MatchResult result) {
        if (creditService.processTransaction(transaction))
            return MatchingOutcome.OK;
        else
            return MatchingOutcome.NOT_ENOUGH_CREDIT;
    }

    @Override
    public void rollback(MatchResult result) {
        creditService.rollbackTransaction(transaction);
    }
}
