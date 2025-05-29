package ir.ramtung.tinyme.domain.service;

import ir.ramtung.tinyme.domain.entity.*;
import ir.ramtung.tinyme.messaging.request.EnterOrderRq;

public class NewOrderProcessor extends CommandProcessor {
    private final Matcher matcher;
    private final Order order;

    public NewOrderProcessor(MatchingControlList controls, Matcher matcher, EnterOrderRq enterOrderRq, Security security, Broker broker, Shareholder shareholder) {
        super(controls);
        this.matcher = matcher;

        if (enterOrderRq.getPeakSize() == 0)
            order = new Order(enterOrderRq.getOrderId(), security, enterOrderRq.getSide(),
                    enterOrderRq.getQuantity(), enterOrderRq.getPrice(), broker, shareholder, enterOrderRq.getEntryTime(),
                    enterOrderRq.getMinimumExecutionQuantity());
        else
            order = new IcebergOrder(enterOrderRq.getOrderId(), security, enterOrderRq.getSide(),
                    enterOrderRq.getQuantity(), enterOrderRq.getPrice(), broker, shareholder,
                    enterOrderRq.getEntryTime(), enterOrderRq.getPeakSize(), enterOrderRq.getMinimumExecutionQuantity());
    }

    @Override
    protected void start() {
        controls.startNewOrder(order);
    }

    @Override
    protected MatchResult process() {
        return matcher.execute(order);
    }

    @Override
    protected void undo(MatchResult result) {
        order.getSecurity().getOrderBook().removeByOrderId(order.getSide(), order.getOrderId());
        controls.orderDequeued(result.remainder());
        matcher.rollbackTrades(order, result.trades());
    }
}
