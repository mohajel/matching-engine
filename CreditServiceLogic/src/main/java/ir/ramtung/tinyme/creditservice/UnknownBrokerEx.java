package ir.ramtung.tinyme.creditservice;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UnknownBrokerEx extends Exception {
    private long brokerId;
}
