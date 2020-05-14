package online.oboz.trip.trip_carrier_advance_payment_api.service.messages.sms;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.Error;

import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;



@Service
public class SmsSenderService {
    Logger log = LoggerFactory.getLogger(SmsSenderService.class);
    private final RestTemplate restTemplate;
    private final ApplicationProperties applicationProperties;



    @Autowired
    public  SmsSenderService(RestTemplate restTemplate, ApplicationProperties applicationProperties) {
        this.restTemplate = restTemplate;
        this.applicationProperties = applicationProperties;
    }

    public void sendSms(SmsContainer sms){
        try{
            makeRequest(sms);
            //
        } catch (SmsSendingException e){
            log.error("Error while sms-sending to " + sms.getPhone() +
                ". Messages: "+e.getErrors());
            // throws higher ?
        }
    }


    private void makeRequest(SmsContainer sms) throws SmsSendingException {
        String errMessage = "Ошибка отправки СМС: ";
        try {
            String smsSenderUrl = applicationProperties.getSmsSenderUrl().toString();
            ResponseEntity<String> response = restTemplate.postForEntity(smsSenderUrl, sms, String.class);
            if (response.getStatusCode() != HttpStatus.OK) {
                throw getSmsException(errMessage, response.getStatusCode());
            } else {
                // TODO: set advance is sms-sent
            }
            log.info("Success send notification sms to " + sms.getPhone());
        } catch (HttpServerErrorException | SmsSendingException e) {
            throw getSmsException(errMessage + ". Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private SmsSendingException getSmsException(String s, HttpStatus status) {
        Error error = new Error();
        error.setErrorMessage(s);
        error.setErrorCode(Integer.toString(status.value()));
        error.setStatus(status.toString());
        return new SmsSendingException(status, error);
    }


}
