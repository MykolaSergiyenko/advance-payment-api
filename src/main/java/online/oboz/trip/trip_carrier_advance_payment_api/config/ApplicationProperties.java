package online.oboz.trip.trip_carrier_advance_payment_api.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.OffsetDateTime;

@ConfigurationProperties(prefix = "application", ignoreInvalidFields = false)
@Data
public class ApplicationProperties {

    @Value("${sms-sender.url:http://sms-sender.r14.k.preprod.oboz:30080}")
    private String smsSenderUrl;
    @Value("${auto-advance.min-count-trip:3}")
    private Integer minCountTrip;
    @Value("${auto-advance.min-date-trip:2020-01-01T00:00:00+00:00}")
    private String strMinDateTrip;
    private OffsetDateTime minDateTrip;
    @Value("${required-download-docs:true}")
    private Boolean requiredDownloadDocs;
    private String mailHost;
    @Value("${spring.mail.port}")
    private int mailPort = 587;
    @Value("${spring.mail.username}")
    private String mailUsername;
    @Value("${spring.mail.password}")
    private String mailPassword;
    @Value("${spring.mail.properties.mail.debug:false}")
    private String propertiesMailDebug;
    @Value("${spring.mail.properties.mail.smtp.starttls.enable:true}")
    private String mailStarttls;
    @Value("${spring.mail.properties.mail.smtp.auth:true}")
    private String mailAuth;
    @Value("${notification.sms-enable:true}")
    private Boolean smsEnable;
    @Value("${email-enable:true}")
    private Boolean mailEnable;

    public OffsetDateTime getMinDateTrip() {
        return OffsetDateTime.parse(strMinDateTrip);
    }

}
