package ir.ramtung.tinyme.domain;

import ir.ramtung.tinyme.config.MockedJMSTestConfig;
import ir.ramtung.tinyme.config.StubbedCreditServiceTestConfig;
import ir.ramtung.tinyme.domain.entity.*;
import ir.ramtung.tinyme.domain.service.*;
import ir.ramtung.tinyme.messaging.creditservice.CreditServiceStub;
import ir.ramtung.tinyme.messaging.request.EnterOrderRq;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDateTime;

import static ir.ramtung.tinyme.domain.entity.Side.BUY;
import static ir.ramtung.tinyme.domain.entity.Side.SELL;
import static ir.ramtung.tinyme.domain.service.MatchingOutcome.MINIMUM_QUANTITY_NOT_SATISFIED;
import static ir.ramtung.tinyme.domain.service.MatchingOutcome.OK;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Import({MockedJMSTestConfig.class, StubbedCreditServiceTestConfig.class})
@DirtiesContext
public class MinimumExecutionQuantityTest {
    private Security security;
    private Broker broker;
    private Shareholder shareholder;
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
        security.getOrderBook().enqueue(new Order(1, security, BUY, 70, 101, broker, shareholder));
    }

    @Test
    void order_successfully_match_when_MEQ_is_satisfied() {
        EnterOrderRq newOrderRq = EnterOrderRq.createNewOrderRq(1, security.getIsin(), 10, LocalDateTime.now(),
                SELL, 100, 100, broker.getBrokerId(), shareholder.getShareholderId(), 0, 50);
        MatchResult result = new NewOrderProcessor(controls, matcher, newOrderRq, security, broker, shareholder).processCommand();
        assertThat(result.outcome()).isEqualTo(MatchingOutcome.OK);
        assertThat(result.trades()).hasSize(1);
        assertThat(result.remainder().getQuantity()).isEqualTo(30);
    }

    @Test
    void order_not_matched_when_MEQ_is_not_satisfied() {
        EnterOrderRq newOrderRq = EnterOrderRq.createNewOrderRq(1, security.getIsin(), 10, LocalDateTime.now(),
                SELL, 200, 100, broker.getBrokerId(), shareholder.getShareholderId(), 0, 100);
        MatchResult result = new NewOrderProcessor(controls, matcher, newOrderRq, security, broker, shareholder).processCommand();
        assertThat(result.outcome()).isEqualTo(MINIMUM_QUANTITY_NOT_SATISFIED);
        assertThat(result.trades()).hasSize(0);
    }

    @Test
    void MEQ_works_with_iceberg_order() {
        EnterOrderRq newOrderRq = EnterOrderRq.createNewOrderRq(1, security.getIsin(), 10, LocalDateTime.now(),
                SELL, 100, 100, broker.getBrokerId(), shareholder.getShareholderId(), 20, 50);
        MatchResult result = new NewOrderProcessor(controls, matcher, newOrderRq, security, broker, shareholder).processCommand();
        assertThat(result.outcome()).isEqualTo(OK);
        assertThat(result.trades()).hasSize(1);
        assertThat(result.remainder().getQuantity()).isEqualTo(20);
    }
}
