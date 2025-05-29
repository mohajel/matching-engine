package ir.ramtung.tinyme.domain.service;

import ir.ramtung.tinyme.domain.entity.Order;
import ir.ramtung.tinyme.domain.entity.Trade;
import ir.ramtung.tinyme.messaging.request.EnterOrderRq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MatchingControlList {
    @Autowired
    private List<MatchingControl> controlList;

    public void tradeAccepted(Order newOrder, Trade trade) {
        for (MatchingControl control : controlList)
            control.tradeAccepted(newOrder, trade);
    }

    public void orderDequeued(Order order) {
        for (MatchingControl control : controlList)
            control.orderDequeued(order);
    }

    public void orderEnqueued(Order order) {
        for (MatchingControl control : controlList)
            control.orderEnqueued(order);
    }

    public void startNewOrder(Order order) {
        for (MatchingControl control : controlList)
            control.startNewOrder(order);
    }

    public void startDeleteOrder(Order order) {
        for (MatchingControl control : controlList)
            control.startDeleteOrder(order);
    }

    public void startUpdateOrder(Order order, EnterOrderRq updateOrderRq) {
        for (MatchingControl control : controlList)
            control.startUpdateOrder(order, updateOrderRq);
    }

    public MatchingOutcome finish(MatchResult result) {
        for (int i = 0; i < controlList.size(); i++) {
            MatchingOutcome controlOutcome = controlList.get(i).commit(result);
            if (!controlOutcome.equals(MatchingOutcome.OK)) {
                for (int j = 0; j < i; j++)
                    controlList.get(j).rollback(result);
                return controlOutcome;
            }
        }
        return MatchingOutcome.OK;
    }

    public void abort() {
        for (MatchingControl control : controlList)
            control.abort();
    }
}
