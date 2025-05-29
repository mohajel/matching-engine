package ir.ramtung.tinyme.messaging.creditservice;

import ir.ramtung.tinyme.creditservice.BrokersCredit;
import ir.ramtung.tinyme.creditservice.CreditService;
import ir.ramtung.tinyme.creditservice.CreditUpdate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CreditServiceStub implements CreditService {
    private final BrokersCredit brokersCredit = new BrokersCredit();

    public void addBroker(long brokerId, long initCredit) {
        brokersCredit.addBroker(brokerId, initCredit);
    }

    public long getCredit(long brokerId) {
        try {
            return brokersCredit.getCredit(brokerId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void clear() {
        brokersCredit.clear();
    }

    @Override
    public boolean processTransaction(List<CreditUpdate> transaction) {
        return brokersCredit.processTransaction(transaction);
    }

    @Override
    public void rollbackTransaction(List<CreditUpdate> transaction) {
        brokersCredit.rollbackTransaction(transaction);
    }
}
