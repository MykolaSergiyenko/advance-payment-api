package online.oboz.trip.trip_carrier_advance_payment_api.service.messages.sms;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.notificatoins.SendSmsRequest;
import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.edit_message.MessagingException;

import online.oboz.trip.trip_carrier_advance_payment_api.service.util.ErrorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
 * @see SendSmsRequest
 */
@Service
public class SmsSenderService implements SmsSender {
    Logger log = LoggerFactory.getLogger(SmsSenderService.class);

    private RestTemplate restTemplate = new RestTemplate();
    private final URL smsSender;


    public SmsSenderService(@Value("${services.notifications.sms.sender-url}") URL url) {
        this.smsSender = url;
    }

    public void sendSms(SendSmsRequest sms) throws MessagingException {
        try {
            URL smsSenderUrl = smsSender;
            if (null == smsSenderUrl)
                throw getSmsSendingException("Укажите url сервиса отправки СМС в конфигурации приложения:", sms);
            ResponseEntity<String> response = restTemplate.postForEntity(smsSenderUrl.toString(),
                sms, String.class);
            if (response.getStatusCode() != HttpStatus.OK) {
                getSmsSendingException("Ошибка отправки СМС." + response.getBody(), sms);
            }
            log.info("СМС успешно отправлена по номеру {}.", sms.getPhone());
        } catch (HttpServerErrorException e) {
            throw getSmsSendingException(e.getMessage(), sms);
        } catch (ResourceAccessException e) {
            throw getSmsSendingException(e.getMessage(), sms);
        }
    }

    private MessagingException getSmsSendingException(String message, SendSmsRequest sms) {
        return ErrorUtils.getMessagingError("Sms-sending error: While sms-sending to " + sms.getPhone() +
            ". Messages: " + message);
    }
}
