package ir.ramtung.tinyme.creditservice;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CommitTransactionRq {
    private List<CreditUpdate> creditUpdates;
}
