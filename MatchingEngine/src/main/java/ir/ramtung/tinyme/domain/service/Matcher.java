package ir.ramtung.tinyme.domain.service;

import ir.ramtung.tinyme.domain.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.ListIterator;

@Service
public class Matcher {
    @Autowired
    private MatchingControlList controls;

    public MatchResult match(Order newOrder) {
        OrderBook orderBook = newOrder.getSecurity().getOrderBook();
        LinkedList<Trade> trades = new LinkedList<>();

        while (orderBook.hasOrderOfType(newOrder.getSide().opposite()) && newOrder.getQuantity() > 0) {
            Order matchingOrder = orderBook.matchWithFirst(newOrder);
            if (matchingOrder == null)
                break;

            Trade trade = new Trade(newOrder.getSecurity(), matchingOrder.getPrice(), Math.min(newOrder.getQuantity(), matchingOrder.getQuantity()), newOrder, matchingOrder);
            trades.add(trade);
            controls.tradeAccepted(newOrder, trade);

            if (newOrder.getQuantity() >= matchingOrder.getQuantity()) {
                newOrder.decreaseQuantity(matchingOrder.getQuantity());
                orderBook.removeFirst(matchingOrder.getSide());
                if (matchingOrder instanceof IcebergOrder icebergOrder) {
                    icebergOrder.decreaseQuantity(matchingOrder.getQuantity());
                    icebergOrder.replenish();
                    if (icebergOrder.getQuantity() > 0)
                        orderBook.enqueue(icebergOrder);
                }
            } else {
                matchingOrder.decreaseQuantity(newOrder.getQuantity());
                newOrder.makeQuantityZero();
            }
        }
        return MatchResult.executed(newOrder, trades);
    }

    public void rollbackTrades(Order newOrder, LinkedList<Trade> trades) {
        ListIterator<Trade> it = trades.listIterator(trades.size());
        while (it.hasPrevious()) {
            if (newOrder.getSide() == Side.BUY)
                newOrder.getSecurity().getOrderBook().restoreOrder(it.previous().getSell());
            else
                newOrder.getSecurity().getOrderBook().restoreOrder(it.previous().getBuy());
        }
    }

    public MatchResult execute(Order order) {
        MatchResult result = match(order);
        if (result.remainder().getQuantity() > 0) {
            order.getSecurity().getOrderBook().enqueue(result.remainder());
            controls.orderEnqueued(order);
        }
        return result;

    }
}
