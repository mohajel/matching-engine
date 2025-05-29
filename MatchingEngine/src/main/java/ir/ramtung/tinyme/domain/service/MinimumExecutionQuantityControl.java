package ir.ramtung.tinyme.domain.service;

import ir.ramtung.tinyme.domain.entity.Order;
import ir.ramtung.tinyme.messaging.request.EnterOrderRq;
import org.springframework.stereotype.Component;

@Component
public class MinimumExecutionQuantityControl implements MatchingControl {
    private Order order;

    @Override
    public void startNewOrder(Order order) {
        this.order = order;
    }

    @Override
    public void startDeleteOrder(Order order) {
        this.order = order;
    }

    @Override
    public void startUpdateOrder(Order order, EnterOrderRq updateOrderRq) {
        this.order = order;
    }

    @Override
    public MatchingOutcome commit(MatchResult result) {
        if (order.minimumExecutionQuantitySatisfied())
            return MatchingOutcome.OK;
        else return MatchingOutcome.MINIMUM_QUANTITY_NOT_SATISFIED;
    }
}
