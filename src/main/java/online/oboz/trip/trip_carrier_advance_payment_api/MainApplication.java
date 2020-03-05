package online.oboz.trip.trip_carrier_advance_payment_api;

import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableConfigurationProperties({ApplicationProperties.class})
@EnableTransactionManagement
public class MainApplication {

    public static void main(String ...args) {
        SpringApplication.run(MainApplication.class, args);
    }

}
