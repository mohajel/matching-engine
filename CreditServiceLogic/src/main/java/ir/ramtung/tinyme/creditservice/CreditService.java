package ir.ramtung.tinyme.creditservice;

import java.util.List;

public interface CreditService {
    boolean processTransaction(List<CreditUpdate> transaction);
    void rollbackTransaction(List<CreditUpdate> transaction);
}
