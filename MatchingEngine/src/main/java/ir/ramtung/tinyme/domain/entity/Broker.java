package ir.ramtung.tinyme.domain.entity;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
public class Broker {
    @Getter
    @EqualsAndHashCode.Include
    private long brokerId;
    @Getter
    private String name;
}
