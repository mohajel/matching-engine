package ir.ramtung.tinyme.creditservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class RequestHandler {
    private final Logger log = Logger.getLogger(this.getClass().getName());
    private final JmsTemplate jmsTemplate;
    @Value("${creditServiceResponseQueue}")
    private String responseQueue;

    private final BrokersCredit brokersCredit;

    public RequestHandler(JmsTemplate jmsTemplate, BrokersCredit brokersCredit) {
        this.jmsTemplate = jmsTemplate;
        this.brokersCredit = brokersCredit;
    }

    @JmsListener(destination = "${creditServiceRequestQueue}", selector = "_type='ir.ramtung.creditservice.CommitTransactionRq'")
    public void receiveProcessTransaction(CommitTransactionRq commitTransactionRq) {
        log.info("Received message: " + commitTransactionRq);
            boolean result = brokersCredit.processTransaction(commitTransactionRq.getCreditUpdates());
            jmsTemplate.convertAndSend(responseQueue, result);
    }

    @JmsListener(destination = "${creditServiceRequestQueue}", selector = "_type='ir.ramtung.creditservice.RollbackTransactionRq'")
    public void receiveRollbackTransaction(RollbackTransactionRq rollbackTransactionRq) {
        log.info("Received message: " + rollbackTransactionRq);
        brokersCredit.rollbackTransaction(rollbackTransactionRq.getCreditUpdates());
    }

}
