package ir.ramtung.tinyme.creditservice;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Map;
import java.util.StringJoiner;
import java.util.logging.Logger;

@Component
@Profile("!test")
public class DataLoader {
    private final Logger log = Logger.getLogger(this.getClass().getName());
    private final BrokersCredit brokersCredit;

    public DataLoader(BrokersCredit brokersCredit) {
        this.brokersCredit = brokersCredit;
    }

    @Value("classpath:persistence/broker.csv")
    private Resource brokerCsvResource;

    @PostConstruct
    public void loadAll() throws Exception {
        loadBrokers();
    }

    @PreDestroy
    public void saveAll() throws Exception {
        System.out.print("Saving persistent data ...");
        saveBrokers();
        System.out.println(", done!");
    }

    private void loadBrokers() throws Exception {
        brokersCredit.clear();
      try (Reader reader = new FileReader(brokerCsvResource.getFile())) {
            try (CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(1).build()) {
                String[] line;
                while ((line = csvReader.readNext()) != null)
                    brokersCredit.addBroker(Long.parseLong(line[0]), Long.parseLong(line[1]));
            }
        }
        log.info("Brokers loaded");
    }

    private void saveBrokers() throws Exception {
        try (PrintWriter writer = new PrintWriter(new FileWriter(brokerCsvResource.getFile()))) {
            writer.println("brokerId,credit");
            for (Map.Entry<Long, Long> entry : brokersCredit.allEntries()) {
                StringJoiner joiner = new StringJoiner(",");
                joiner.add(String.valueOf(entry.getKey()))
                        .add(String.valueOf(entry.getValue()));
                writer.println(joiner);
            }
        }
        log.info("Brokers saved");
    }
}
