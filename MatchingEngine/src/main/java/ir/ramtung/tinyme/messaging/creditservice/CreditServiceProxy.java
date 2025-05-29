package ir.ramtung.tinyme.messaging.creditservice;

import ir.ramtung.tinyme.creditservice.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;

@Service
@EnableJms
@Profile("!test")
public class CreditServiceProxy implements CreditService {
    private final Logger log = Logger.getLogger(this.getClass().getName());

    @Autowired
    private JmsTemplate jmsTemplate;
    @Value("${creditServiceRequestQueue}")
    private String requestQueue;
    @Value("${creditServiceResponseQueue}")
    private String responseQueue;

    @Override
    public boolean processTransaction(List<CreditUpdate> transaction) {
        CommitTransactionRq request = new CommitTransactionRq(transaction);
        jmsTemplate.convertAndSend(requestQueue, request);
        log.info("Sent processTransaction Request: " + request);
        jmsTemplate.setReceiveTimeout(1000);
        Boolean response = (Boolean) jmsTemplate.receiveAndConvert(responseQueue);
        log.info("Received processTransaction response: " + response);
        return response;
    }

    @Override
    public void rollbackTransaction(List<CreditUpdate> transaction) {
        RollbackTransactionRq request = new RollbackTransactionRq(transaction);
        jmsTemplate.convertAndSend(requestQueue, request);
        log.info("Sent rollbackTransaction Request: " + request);
    }
}
