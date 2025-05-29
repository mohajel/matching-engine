package ir.ramtung.tinyme.config;

import ir.ramtung.tinyme.messaging.creditservice.CreditServiceStub;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("test")
public class StubbedCreditServiceTestConfig {
    @Bean
    @Primary
    public CreditServiceStub creditService() {
        return new CreditServiceStub();
    }
}
