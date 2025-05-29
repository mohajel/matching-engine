package ir.ramtung.tinyme.domain.service;

import ir.ramtung.tinyme.domain.entity.*;
import ir.ramtung.tinyme.messaging.Message;
import ir.ramtung.tinyme.messaging.exception.InvalidRequestException;
import ir.ramtung.tinyme.messaging.request.DeleteOrderRq;

public class DeleteOrderProcessor extends CommandProcessor {
    private final Order order;

    public DeleteOrderProcessor(MatchingControlList controls, DeleteOrderRq deleteOrderRq, Security security) throws InvalidRequestException {
        super(controls);
        order = security.getOrderBook().findByOrderId(deleteOrderRq.getSide(), deleteOrderRq.getOrderId());
        if (order == null)
            throw new InvalidRequestException(Message.ORDER_ID_NOT_FOUND);
    }

    @Override
    protected void start() {
        controls.startDeleteOrder(order);
    }

    @Override
    protected MatchResult process() {
        order.getSecurity().getOrderBook().removeByOrderId(order.getSide(), order.getOrderId());
        controls.orderDequeued(order);
        return new MatchResult(MatchingOutcome.OK);
    }

    @Override
    protected void undo(MatchResult result) {
        order.getSecurity().getOrderBook().restoreOrder(order);
    }
}
