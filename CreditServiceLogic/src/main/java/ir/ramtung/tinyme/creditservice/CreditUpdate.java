package ir.ramtung.tinyme.creditservice;

public record CreditUpdate(long brokerId, long amountIncreased) {
    public CreditUpdate reverse() {
        return new CreditUpdate(brokerId, -amountIncreased);
    }
}
