package ir.ramtung.tinyme.domain.service;

import ir.ramtung.tinyme.domain.entity.Order;
import ir.ramtung.tinyme.domain.entity.Side;
import ir.ramtung.tinyme.domain.entity.Trade;
import ir.ramtung.tinyme.messaging.request.EnterOrderRq;
import org.springframework.stereotype.Component;

@Component
public class OwnershipControl implements MatchingControl {
    private MatchingOutcome matchingCouldBeStarted;

    @Override
    public void startNewOrder(Order order) {
        if (order.getSide() == Side.SELL &&
                !order.getShareholder().hasEnoughPositionsOn(order.getSecurity(),
                        order.getSecurity().getOrderBook().totalSellQuantityByShareholder(order.getShareholder()) + order.getQuantity())) {
            matchingCouldBeStarted = MatchingOutcome.NOT_ENOUGH_POSITIONS;
            return;
        }
        matchingCouldBeStarted = MatchingOutcome.OK;
    }

    @Override
    public void startUpdateOrder(Order order, EnterOrderRq updateOrderRq) {
        if (updateOrderRq.getSide() == Side.SELL &&
                !order.getShareholder().hasEnoughPositionsOn(order.getSecurity(),
                        order.getSecurity().getOrderBook().totalSellQuantityByShareholder(order.getShareholder()) - order.getQuantity() + updateOrderRq.getQuantity())) {
            matchingCouldBeStarted = MatchingOutcome.NOT_ENOUGH_POSITIONS;
            return;
        }
        matchingCouldBeStarted = MatchingOutcome.OK;
    }

    @Override
    public MatchingOutcome commit(MatchResult result) {
        if (!matchingCouldBeStarted.equals(MatchingOutcome.OK))
            return matchingCouldBeStarted;
        for (Trade trade : result.trades()) {
            trade.getBuy().getShareholder().incPosition(trade.getSecurity(), trade.getQuantity());
            trade.getSell().getShareholder().decPosition(trade.getSecurity(), trade.getQuantity());
        }
        return MatchingOutcome.OK;
    }

    @Override
    public void rollback(MatchResult result) {
        for (Trade trade : result.trades()) {
            trade.getBuy().getShareholder().decPosition(trade.getSecurity(), trade.getQuantity());
            trade.getSell().getShareholder().incPosition(trade.getSecurity(), trade.getQuantity());
        }
    }
}
