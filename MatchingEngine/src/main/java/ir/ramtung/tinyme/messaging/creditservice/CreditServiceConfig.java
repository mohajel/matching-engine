package ir.ramtung.tinyme.messaging.creditservice;

import ir.ramtung.tinyme.creditservice.CreditService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class CreditServiceConfig {
    @Bean
    public CreditService creditService() {
        return new CreditServiceProxy();
    }

}
