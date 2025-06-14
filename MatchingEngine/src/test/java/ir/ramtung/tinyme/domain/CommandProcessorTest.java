package ir.ramtung.tinyme.domain;

import ir.ramtung.tinyme.config.MockedJMSTestConfig;
import ir.ramtung.tinyme.config.StubbedCreditServiceTestConfig;
import ir.ramtung.tinyme.domain.entity.*;
import ir.ramtung.tinyme.domain.service.*;
import ir.ramtung.tinyme.messaging.creditservice.CreditServiceStub;
import ir.ramtung.tinyme.messaging.exception.InvalidRequestException;
import ir.ramtung.tinyme.messaging.request.DeleteOrderRq;
import ir.ramtung.tinyme.messaging.request.EnterOrderRq;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static ir.ramtung.tinyme.domain.entity.Side.BUY;
import static ir.ramtung.tinyme.domain.entity.Side.SELL;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Import({MockedJMSTestConfig.class, StubbedCreditServiceTestConfig.class})
@DirtiesContext
public class CommandProcessorTest {
    private Security security;
    private Broker broker;
    private Shareholder shareholder;
    private List<Order> orders;
    @Autowired
    Matcher matcher;
    @Autowired
    MatchingControlList controls;
    @Autowired
    CreditServiceStub creditServiceStub;

    @BeforeEach
    void setupOrderBook() {
        creditServiceStub.clear();
        security = Security.builder().build();
        broker = Broker.builder().brokerId(0).build();
        creditServiceStub.addBroker(broker.getBrokerId(), 1_000_000L);
        shareholder = Shareholder.builder().shareholderId(0).build();
        shareholder.incPosition(security, 100_000);
        orders = Arrays.asList(
                new Order(1, security, BUY, 304, 15700, broker, shareholder),
                new Order(2, security, BUY, 43, 15500, broker, shareholder),
                new Order(3, security, BUY, 445, 15450, broker, shareholder),
                new Order(4, security, BUY, 526, 15450, broker, shareholder),
                new Order(5, security, BUY, 1000, 15400, broker, shareholder),
                new Order(6, security, Side.SELL, 350, 15800, broker, shareholder),
                new Order(7, security, Side.SELL, 285, 15810, broker, shareholder),
                new Order(8, security, Side.SELL, 800, 15810, broker, shareholder),
                new Order(9, security, Side.SELL, 340, 15820, broker, shareholder),
                new Order(10, security, Side.SELL, 65, 15820, broker, shareholder)
        );
        orders.forEach(order -> security.getOrderBook().enqueue(order));
    }

    @Test
    void delete_order_works() {
        DeleteOrderRq deleteOrderRq = new DeleteOrderRq(1, security.getIsin(), Side.SELL, 6);
        assertThatNoException().isThrownBy(() -> new DeleteOrderProcessor(controls, deleteOrderRq, security).processCommand());
        assertThat(security.getOrderBook().getBuyQueue()).isEqualTo(orders.subList(0, 5));
        assertThat(security.getOrderBook().getSellQueue()).isEqualTo(orders.subList(6, 10));
    }

    @Test
    void deleting_non_existing_order_fails() {
        DeleteOrderRq deleteOrderRq = new DeleteOrderRq(1, security.getIsin(), Side.SELL, 1);
        assertThatExceptionOfType(InvalidRequestException.class).isThrownBy(() -> new DeleteOrderProcessor(controls, deleteOrderRq, security).processCommand());
    }

    @Test
    void reducing_quantity_does_not_change_priority() {
        EnterOrderRq updateOrderRq = EnterOrderRq.createUpdateOrderRq(1, security.getIsin(), 3, LocalDateTime.now(), BUY, 440, 15450, 0, 0, 0);
        assertThatNoException().isThrownBy(() -> new UpdateOrderProcessor(controls, matcher, updateOrderRq, security).processCommand());
        assertThat(security.getOrderBook().getBuyQueue().get(2).getQuantity()).isEqualTo(440);
        assertThat(security.getOrderBook().getBuyQueue().get(2).getOrderId()).isEqualTo(3);
    }

    @Test
    void increasing_quantity_changes_priority() {
        EnterOrderRq updateOrderRq = EnterOrderRq.createUpdateOrderRq(1, security.getIsin(), 3, LocalDateTime.now(), BUY, 450, 15450, 0, 0, 0);
        assertThatNoException().isThrownBy(() -> new UpdateOrderProcessor(controls, matcher, updateOrderRq, security).processCommand());
        assertThat(security.getOrderBook().getBuyQueue().get(3).getQuantity()).isEqualTo(450);
        assertThat(security.getOrderBook().getBuyQueue().get(3).getOrderId()).isEqualTo(3);
    }

    @Test
    void changing_price_changes_priority() {
        EnterOrderRq updateOrderRq = EnterOrderRq.createUpdateOrderRq(1, security.getIsin(), 1, LocalDateTime.now(), BUY, 300, 15450, 0, 0, 0);
        assertThatNoException().isThrownBy(() -> new UpdateOrderProcessor(controls, matcher, updateOrderRq, security).processCommand());
        assertThat(security.getOrderBook().getBuyQueue().get(3).getQuantity()).isEqualTo(300);
        assertThat(security.getOrderBook().getBuyQueue().get(3).getPrice()).isEqualTo(15450);
        assertThat(security.getOrderBook().getBuyQueue().get(3).getOrderId()).isEqualTo(1);
        assertThat(security.getOrderBook().getBuyQueue().get(0).getOrderId()).isEqualTo(2);
    }

    @Test
    void changing_price_causes_trades_to_happen() {
        EnterOrderRq updateOrderRq = EnterOrderRq.createUpdateOrderRq(1, security.getIsin(), 6, LocalDateTime.now(), Side.SELL, 350, 15700, 0, 0, 0);
        assertThatNoException().isThrownBy(() ->
                assertThat(new UpdateOrderProcessor(controls, matcher, updateOrderRq, security).processCommand().trades()).isNotEmpty()
        );
    }

    @Test
    void updating_non_existing_order_fails() {
        EnterOrderRq updateOrderRq = EnterOrderRq.createUpdateOrderRq(1, security.getIsin(), 6, LocalDateTime.now(), BUY, 350, 15700, 0, 0, 0);
        assertThatExceptionOfType(InvalidRequestException.class).isThrownBy(() -> new UpdateOrderProcessor(controls, matcher, updateOrderRq, security).processCommand());
    }

    @Test
    void increasing_iceberg_peak_size_changes_priority() {
        security = Security.builder().build();
        broker = Broker.builder().brokerId(10).build();
        creditServiceStub.addBroker(broker.getBrokerId(), 1_000_000L);
        orders = Arrays.asList(
                new Order(1, security, BUY, 304, 15700, broker, shareholder),
                new Order(2, security, BUY, 43, 15500, broker, shareholder),
                new IcebergOrder(3, security, BUY, 445, 15450, broker, shareholder, 100),
                new Order(4, security, BUY, 526, 15450, broker, shareholder),
                new Order(5, security, BUY, 1000, 15400, broker, shareholder)
        );
        orders.forEach(order -> security.getOrderBook().enqueue(order));
        EnterOrderRq updateOrderRq = EnterOrderRq.createUpdateOrderRq(1, security.getIsin(), 3, LocalDateTime.now(), BUY, 445, 15450, broker.getBrokerId(), 0, 150);
        assertThatNoException().isThrownBy(() -> new UpdateOrderProcessor(controls, matcher, updateOrderRq, security).processCommand());
        assertThat(security.getOrderBook().getBuyQueue().get(3).getQuantity()).isEqualTo(150);
        assertThat(security.getOrderBook().getBuyQueue().get(3).getOrderId()).isEqualTo(3);
    }

    @Test
    void decreasing_iceberg_quantity_to_amount_larger_than_peak_size_does_not_changes_priority() {
        security = Security.builder().build();
        broker = Broker.builder().build();
        orders = Arrays.asList(
                new Order(1, security, BUY, 304, 15700, broker, shareholder),
                new Order(2, security, BUY, 43, 15500, broker, shareholder),
                new IcebergOrder(3, security, BUY, 445, 15450, broker, shareholder, 100),
                new Order(4, security, BUY, 526, 15450, broker, shareholder),
                new Order(5, security, BUY, 1000, 15400, broker, shareholder)
        );
        orders.forEach(order -> security.getOrderBook().enqueue(order));
        EnterOrderRq updateOrderRq = EnterOrderRq.createUpdateOrderRq(1, security.getIsin(), 3, LocalDateTime.now(), BUY, 300, 15450, 0, 0, 100);
        assertThatNoException().isThrownBy(() -> new UpdateOrderProcessor(controls, matcher, updateOrderRq, security).processCommand());
        assertThat(security.getOrderBook().getBuyQueue().get(2).getOrderId()).isEqualTo(3);
    }

    @Test
    void update_iceberg_that_loses_priority_with_no_trade_works() {
        security = Security.builder().isin("TEST").build();
        broker = Broker.builder().brokerId(1).build();
        creditServiceStub.addBroker(broker.getBrokerId(), 100);

        security.getOrderBook().enqueue(
                new IcebergOrder(1, security, BUY, 100, 9, broker, shareholder, 10)
        );

        EnterOrderRq updateReq = EnterOrderRq.createUpdateOrderRq(2, security.getIsin(), 1, LocalDateTime.now(), BUY, 100, 10, 0, 0, 10);
        assertThatNoException().isThrownBy(() -> new UpdateOrderProcessor(controls, matcher, updateReq, security).processCommand());

        assertThat(creditServiceStub.getCredit(broker.getBrokerId())).isEqualTo(0);
        assertThat(security.getOrderBook().getBuyQueue().get(0).getOrderId()).isEqualTo(1);
    }

    @Test
    void update_iceberg_order_decrease_peak_size() {
        security = Security.builder().isin("TEST").build();
        security.getOrderBook().enqueue(
                new IcebergOrder(1, security, BUY, 20, 10, broker, shareholder, 10)
        );

        EnterOrderRq updateReq = EnterOrderRq.createUpdateOrderRq(2, security.getIsin(), 1, LocalDateTime.now(), BUY, 20, 10, 0, 0, 5);
        assertThatNoException().isThrownBy(() -> new UpdateOrderProcessor(controls, matcher, updateReq, security).processCommand());

        assertThat(security.getOrderBook().getBuyQueue().get(0).getQuantity()).isEqualTo(5);
    }

    @Test
    void update_iceberg_order_price_leads_to_match_as_new_order() throws InvalidRequestException {
        security = Security.builder().isin("TEST").build();
        shareholder.incPosition(security, 1_000);
        orders = List.of(
                new Order(1, security, BUY, 15, 10, broker, shareholder),
                new Order(2, security, BUY, 20, 10, broker, shareholder),
                new Order(3, security, BUY, 40, 10, broker, shareholder),
                new IcebergOrder(4, security, SELL, 30, 12, broker, shareholder, 10)
        );
        orders.forEach(order -> security.getOrderBook().enqueue(order));

        EnterOrderRq updateReq = EnterOrderRq.createUpdateOrderRq(5, security.getIsin(), 4, LocalDateTime.now(), SELL, 30, 10, 0, 0, 10);

        MatchResult result = new UpdateOrderProcessor(controls, matcher, updateReq, security).processCommand();

        assertThat(result.outcome()).isEqualTo(MatchingOutcome.OK);
        assertThat(result.trades()).hasSize(2);
        assertThat(result.remainder().getQuantity()).isZero();
    }


    @Test
    void updating_order_will_not_cause_MEQ_failure() throws InvalidRequestException {
        Security security = Security.builder().isin("NEW_SE").build();
        Order queuedOrder = new Order(1,
                security,
                Side.BUY,
                500,
                200,
                100,
                broker,
                shareholder,
                LocalDateTime.now(),
                200,
                OrderStatus.QUEUED);
        security.getOrderBook().enqueue(queuedOrder);

        EnterOrderRq updateReq = EnterOrderRq.createUpdateOrderRq(
                2,
                "ABC",
                1,
                LocalDateTime.now(),
                Side.BUY,
                400,
                100,
                broker.getBrokerId(),
                shareholder.getShareholderId(),
                0
        );

        MatchResult result = new UpdateOrderProcessor(controls, matcher, updateReq, security).processCommand();

        assertThat(result.outcome()).isEqualTo(MatchingOutcome.OK);

    }
}
