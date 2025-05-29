package ir.ramtung.tinyme.creditservice;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class BrokersCredit {
    private final HashMap<Long, Long> creditByBrokerId = new HashMap<>();

    public void clear() {
        creditByBrokerId.clear();
    }

    public void addBroker(long brokerId, long credit) {
        if (creditByBrokerId.containsKey(brokerId))
            throw new IllegalArgumentException("Broker already exists");
        creditByBrokerId.put(brokerId, credit);
    }

    public Set<Map.Entry<Long, Long>> allEntries() {
        return creditByBrokerId.entrySet();
    }

    public long getCredit(long brokerId) throws UnknownBrokerEx {
        if (!creditByBrokerId.containsKey(brokerId))
            throw new UnknownBrokerEx(brokerId);
        return creditByBrokerId.get(brokerId);
    }

    public boolean processTransaction(List<CreditUpdate> transaction) {
        for (int i = 0; i < transaction.size(); i++) {
            if (canUpdate(transaction.get(i)))
                applyUpdate(transaction.get(i));
            else {
                rollbackUpdates(transaction, i);
                return false;
            }
        }
        return true;
    }

    private void rollbackUpdates(List<CreditUpdate> transaction, int i) {
        for (int j = i-1; j >= 0; j--) {
            applyUpdate(transaction.get(j).reverse());
        }
    }

    private void applyUpdate(CreditUpdate creditUpdate) {
        if (!creditByBrokerId.containsKey(creditUpdate.brokerId()))
            return;
        creditByBrokerId.put(creditUpdate.brokerId(), creditByBrokerId.get(creditUpdate.brokerId()) + creditUpdate.amountIncreased());
    }

    private boolean canUpdate(CreditUpdate creditUpdate) {
        if (!creditByBrokerId.containsKey(creditUpdate.brokerId()))
            return false;
        return creditByBrokerId.get(creditUpdate.brokerId()) + creditUpdate.amountIncreased() >= 0;
    }

    public void rollbackTransaction(List<CreditUpdate> transaction) {
        rollbackUpdates(transaction, transaction.size());
    }

}
