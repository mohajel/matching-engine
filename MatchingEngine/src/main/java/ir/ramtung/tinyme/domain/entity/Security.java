package ir.ramtung.tinyme.domain.entity;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Security {
    private String isin;
    @Builder.Default
    private final int tickSize = 1;
    @Builder.Default
    private final int lotSize = 1;
    @Builder.Default
    private final OrderBook orderBook = new OrderBook();

}
