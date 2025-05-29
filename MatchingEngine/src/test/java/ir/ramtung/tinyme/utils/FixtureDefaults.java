package ir.ramtung.tinyme.utils;


import ir.ramtung.tinyme.domain.entity.Broker;
import ir.ramtung.tinyme.domain.entity.Security;
import ir.ramtung.tinyme.domain.entity.Shareholder;
import ir.ramtung.tinyme.messaging.creditservice.CreditServiceStub;
import ir.ramtung.tinyme.repository.BrokerRepository;
import ir.ramtung.tinyme.repository.SecurityRepository;
import ir.ramtung.tinyme.repository.ShareholderRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class FixtureDefaults {
    private final SecurityRepository securityRepository;
    private final BrokerRepository brokerRepository;
    private final ShareholderRepository shareholderRepository;
    @Autowired
    private CreditServiceStub creditServiceStub;

    public void init() {
        Security security = Security.builder().isin(TestDefaults.ISIN).build();
        securityRepository.addSecurity(security);

        Broker broker = Broker.builder()
                .brokerId(TestDefaults.BROKER_ID)
                .build();
        creditServiceStub.clear();
        creditServiceStub.addBroker(broker.getBrokerId(), 10_000_000);
        brokerRepository.addBroker(broker);


        Shareholder shareholder = Shareholder.builder().shareholderId(TestDefaults.SHAREHOLDER_ID).build();
        shareholder.incPosition(security, 1000);
        shareholderRepository.addShareholder(shareholder);
    }

    public void flush(){
        shareholderRepository.clear();
        brokerRepository.clear();
        securityRepository.clear();
    }

    public Security getSecurity(){
        return securityRepository.findSecurityByIsin(TestDefaults.ISIN);
    }

}
