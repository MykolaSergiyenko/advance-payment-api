package online.oboz.trip.trip_carrier_advance_payment_api.service;

import lombok.extern.slf4j.Slf4j;
import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.service.dto.SendSmsRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

//@Service
@Slf4j
public class Notification {

        private static final String RUSSIAN_COUNTRY_CODE = "7";
        private static final String SEND_SMS_METHOD_PATH = "/v1/send-sms";
        private static final String SMS_TEXT_P1 = "";
        private static final String SMS_TEXT_P2 = "";
        // String url = "http://da-checking-service:8080";
        // String url = "http://sms-sender.r14.k.preprod.oboz:30080";


        RestTemplate restTemplate;
        ApplicationProperties applicationProperties;
    @Autowired
        public Notification(           RestTemplate restTemplate,
                                       ApplicationProperties applicationProperties) {
            this.restTemplate = restTemplate;
            this.applicationProperties = applicationProperties;
        }



        private Boolean sendSms(String phoneNumber, String password) {
            String url = applicationProperties.getSmsSenderUrl();
            try {
                ResponseEntity<String> response = restTemplate.postForEntity(
                    url + SEND_SMS_METHOD_PATH,
                    new SendSmsRequest(getSmsText(phoneNumber, password), RUSSIAN_COUNTRY_CODE + phoneNumber),
                    String.class
                );
                if (response.getStatusCode().value() != 200) {
                    log.error("Sms server returned bad response" + response);
                    return false;
                }
            } catch (Exception e) {
                log.error("Some Exeption" + e.getStackTrace().toString());
            }

            return true;
        }

        private String getSmsText(String phoneNumber, String password) {
            return SMS_TEXT_P1 + phoneNumber + SMS_TEXT_P2 + password;
        }

    }

