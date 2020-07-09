package online.oboz.trip.trip_carrier_advance_payment_api.service.messages.sms;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.notificatoins.SmsContainer;
import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.common.format.MessagingException;

import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.util.ErrorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.URL;

/**
 * <b>Сервис отправки СМС</b>
 *
 * @author s‡udent
 * @see SmsSender
 * @see RestTemplate
 * @see SmsContainer
 */
@Service
public class SmsSenderService implements SmsSender {
    Logger log = LoggerFactory.getLogger(SmsSenderService.class);
    //private final RestService restService;

    private RestTemplate restTemplate = new RestTemplate();
    private final ApplicationProperties applicationProperties;

    @Autowired
    public SmsSenderService(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }


    public void sendSms(SmsContainer sms) throws MessagingException {
        try {
            URL smsSenderUrl = applicationProperties.getSmsSenderUrl();
            if (null == smsSenderUrl) throw getSmsSendingException("SMS-sender-url is empty for:", sms);
            //ResponseEntity<String> response = restService.postForEntity(smsSenderUrl, sms);
            ResponseEntity<String> response = restTemplate.postForEntity(smsSenderUrl.toString(),
                sms, String.class);
            if (response.getStatusCode() != HttpStatus.OK) {
                getSmsSendingException("Bad SMS-response. " + response.getBody(), sms);
            } else {
                // TODO: set advance is sms-sent
            }
            log.info("Success send notification sms to " + sms.getPhone());
        } catch (HttpServerErrorException e) {
            throw getSmsSendingException(e.getMessage(), sms);
        } catch (ResourceAccessException e) {
            throw getSmsSendingException(e.getMessage(), sms);
        }
    }

    private MessagingException getSmsSendingException(String message, SmsContainer sms) {
        return ErrorUtils.getMessagingError("Sms-sending error: While sms-sending to " + sms.getPhone() +
            ". Messages: " + message);
    }
}
