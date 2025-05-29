package ir.ramtung.tinyme.domain.service;

import ir.ramtung.tinyme.domain.entity.*;
import ir.ramtung.tinyme.messaging.Message;
import ir.ramtung.tinyme.messaging.exception.InvalidRequestException;
import ir.ramtung.tinyme.messaging.request.EnterOrderRq;

import java.util.List;

public class UpdateOrderProcessor extends CommandProcessor {
    private final Matcher matcher;
    private final Order order;
    private Order originalOrder;
    private final Security security;
    private final EnterOrderRq updateOrderRq;

    public UpdateOrderProcessor(MatchingControlList controls, Matcher matcher, EnterOrderRq updateOrderRq, Security security) throws InvalidRequestException {
        super(controls);
        this.matcher = matcher;

        this.security = security;
        this.updateOrderRq = updateOrderRq;
        order = security.getOrderBook().findByOrderId(updateOrderRq.getSide(), updateOrderRq.getOrderId());
        if (order == null)
            throw new InvalidRequestException(Message.ORDER_ID_NOT_FOUND);
        if ((order instanceof IcebergOrder) && updateOrderRq.getPeakSize() == 0)
            throw new InvalidRequestException(Message.INVALID_PEAK_SIZE);
        if (!(order instanceof IcebergOrder) && updateOrderRq.getPeakSize() != 0)
            throw new InvalidRequestException(Message.CANNOT_SPECIFY_PEAK_SIZE_FOR_A_NON_ICEBERG_ORDER);
    }

    @Override
    protected void start() {
        controls.startUpdateOrder(order, updateOrderRq);
    }

    @Override
    protected MatchResult process() {

        boolean losesPriority = order.isQuantityIncreased(updateOrderRq.getQuantity())
                || updateOrderRq.getPrice() != order.getPrice()
                || ((order instanceof IcebergOrder icebergOrder) && (icebergOrder.getPeakSize() < updateOrderRq.getPeakSize()));

        controls.orderDequeued(order);
        originalOrder = order.snapshot();
        order.updateFromRequest(updateOrderRq);
        if (!losesPriority) {
            controls.orderEnqueued(order);
            return MatchResult.executed(null, List.of());
        }

        order.markAsNew();
        security.getOrderBook().removeByOrderId(updateOrderRq.getSide(), updateOrderRq.getOrderId());

        return matcher.execute(order);
    }

    @Override
    protected void undo(MatchResult result) {
        matcher.rollbackTrades(order, result.trades());
        security.getOrderBook().removeByOrderId(order.getSide(), order.getOrderId());
        controls.orderDequeued(order);
        security.getOrderBook().enqueue(originalOrder);
        controls.orderEnqueued(originalOrder);
    }
}
