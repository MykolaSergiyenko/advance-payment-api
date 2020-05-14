package online.oboz.trip.trip_carrier_advance_payment_api.service.messages.common.format;

import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.TripAdvance;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.TripAdvanceRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.common.format.urlshorter.UrlShortenerService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.common.format.urlshorter.UrlService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.email.EmailContainer;
import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.sms.SmsContainer;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.Error;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.IllegalFormatFlagsException;

@Service
public class MessageTextService {
    private static final Logger log = LoggerFactory.getLogger(UrlShortenerService.class);

    private ApplicationProperties appProperties;
    private UrlService urlCutter;
    private final TripAdvanceRepository tripAdvanceRepository;


    @Autowired
    public MessageTextService(ApplicationProperties appProperties, UrlService urlCutter, TripAdvanceRepository tripAdvanceRepository) {
        this.appProperties = appProperties;
        this.urlCutter = urlCutter;
        this.tripAdvanceRepository = tripAdvanceRepository;
    }


    public SmsContainer createSms(TripAdvance advance){
        String text = getSmsText(advance);
        String phone = advance.getTrip().getContractor().getPhone(); //...getSmsPhone by template
        String tripNum = advance.getTrip().getNum();//advance.tripNum;
        SmsContainer container = new SmsContainer(text, phone, tripNum);
        return container;
    }

    public EmailContainer createEmail(TripAdvance advance){
        String from = appProperties.getMailUsername();
        String to = advance.getTrip().getContractor().getEmail(); //advance.contact.getEmail. validate?
        String subject = getEmailHeader(advance);
        String text = getEmailText(advance);
        EmailContainer container = new EmailContainer(from, to, subject, text);
        return container;
    }


    private String getSmsText(TripAdvance advance){
        String result = null;
        String template = appProperties.getSmsMessageTemplate();
        String link = appProperties.getLkUrl().toString();
        link = link + advance.getAdvanceUuid();
        if (appProperties.isSmsCutLinks()) {
            link = urlCutter.editUrl(link);
        }
        try{
            result = formatMessageWithUrl(template, advance, link);
        } catch (IllegalFormatFlagsException e){
            log.info("Format message error: "+e.getMessage());
        }
        return result;
    }

    private String getEmailText(TripAdvance advance){
        String result = null;
        String template = appProperties.getEmailMessageTemplate();
        String link = appProperties.getLkUrl().toString();

        link = link + advance.getAdvanceUuid();
        if (appProperties.isEmailCutLinks()) {
            link = urlCutter.editUrl(link);
        }
        try{
            result = formatMessageWithUrl(template, advance, link);
        } catch (IllegalFormatFlagsException e){
            log.info("Format message error: "+e.getMessage());
        }
        return result;
    }

    private String getEmailHeader(TripAdvance advance){
        String result = null;
        String template = appProperties.getEmailHeaderTemplate();

        try{
            result = formatMessageHeader(template, advance);
        } catch (IllegalFormatFlagsException e){
            log.info("Format message error: "+e.getMessage());
        }
        return result;
    }



    private String getMessageTemplate(String type){
        if(type.equals("SMS")){
            return appProperties.getSmsMessageTemplate();
        } else if (type.equals("EMAIL")){
            return appProperties.getEmailMessageTemplate();
        }
        else return null;
    }

    private String formatMessageHeader(String headerTemplate, TripAdvance advance) {
        return String.format(headerTemplate,advance.getTrip().getNum());
    }

    private String formatMessageWithUrl(String textTemplate, TripAdvance advance, String lkLink) {
        return String.format(textTemplate,
            advance.getTripId(), //tripNUm
            advance.getAdvancePaymentSum(), //paymentSum
            lkLink
        );
    }

    private BusinessLogicException getBusinessLogicException(String s) {
        Error error = new Error();
        error.setErrorMessage(s);
        return new BusinessLogicException(HttpStatus.UNPROCESSABLE_ENTITY, error);
    }
}
