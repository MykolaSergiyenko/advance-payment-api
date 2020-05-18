package online.oboz.trip.trip_carrier_advance_payment_api.service.messages.common.format;

import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.TripAdvance;
import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.common.format.urlshorter.UrlShortenerService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.common.format.urlshorter.UrlService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.email.EmailContainer;
import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.sms.SmsContainer;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.IllegalFormatException;

/**
 * <b>Сервис для формирования текстовых параметров уведомлений по шаблонам</b>
 * <p>
 * в параметрах приложения: тексты СМС и и-мейлов, заголовки и т.д.
 * <p>
 * Кроме общих параметров, использует "сокращатель" для ссылок {@link UrlShortenerService} .
 * <p>
 * Используется в {@link online.oboz.trip.trip_carrier_advance_payment_api.service.messages.NewNotificationService}.
 * <p>
 *
 * @author s‡udent
 * @see TextService
 */
@Service
public class MessageCreateService implements TextService {
    private static final Logger log = LoggerFactory.getLogger(MessageCreateService.class);

    private ApplicationProperties appProperties;
    private UrlService urlCutter;

    @Autowired
    public MessageCreateService(ApplicationProperties appProperties, UrlService urlCutter) {
        this.appProperties = appProperties;
        this.urlCutter = urlCutter;
    }


    @Override
    public SmsContainer createSms(TripAdvance advance) throws MessagingException {
        String text = getSmsText(advance);
        String phone = getPhoneNumber(advance);
        String tripNum = advance.getTrip().getNum();

        if (StringUtils.isBlank(text) ||
            StringUtils.isBlank(phone) ||
            StringUtils.isBlank(tripNum)) {
            throw getCreateSmsException("Empty sms-fields for advance ", advance.getId().toString());
        }
        SmsContainer container = new SmsContainer(text, phone, tripNum);
        return container;
    }

    @Override
    public EmailContainer createEmail(TripAdvance advance) throws MessagingException {
        String from = appProperties.getMailUsername();
        String to = advance.getTrip().getContractor().getEmail(); //TODO: validate?
        String subject = getEmailHeader(advance);
        String text = getEmailText(advance);

        if (StringUtils.isBlank(from) ||
            StringUtils.isBlank(to) ||
            StringUtils.isBlank(subject) ||
            StringUtils.isBlank(text)) {
            throw getCreateEmailException("Empty email-fields for advance ", advance.getId().toString());
        }
        EmailContainer container = new EmailContainer(from, to, subject, text);
        return container;

    }

    private String getPhoneNumber(TripAdvance advance) throws MessagingException {
        String template = appProperties.getSmsPhoneTemplate();
        String number = advance.getContractor().getContact().getPhone();
        if (StringUtils.isBlank(number) || StringUtils.isBlank(template)) {
            log.error("Empty phone number fields.");
            throw getCreateSmsException("Empty phone number fields for advance ", advance.getId().toString());
        }
        try {
            number = String.format(template, number);
        } catch (IllegalFormatException e) {
            log.error("Format message error: " + e.getMessage());
            throw getCreateSmsException("Format phone-number error: " + e.getMessage(), advance.getId().toString());
        }
        return number;
    }


    private String getSmsText(TripAdvance advance) throws MessagingException {
        String result = null;
        String template = appProperties.getSmsMessageTemplate();
        URL url = appProperties.getLkUrl();
        boolean isShortLink = appProperties.isSmsCutLinks() == null ?
            false : appProperties.isSmsCutLinks();

        if (null == template || null == url ||
            StringUtils.isBlank(url.toString()) ||
            StringUtils.isBlank(template)){
            throw getCreateEmailException("Format email-message error: Empty e-mail creation properties: ", advance.getId().toString());
        }

        String link = url.toString() + advance.getAdvanceUuid();
        try {
            if (isShortLink) {
                link = urlCutter.editUrl(link);
                log.info("Short url is: " + link);
            }
            String tripNum = advance.getTrip().getNum();
            Double sum = advance.getAdvancePaymentSum();
            if (advance == null || StringUtils.isBlank(template) ||
                StringUtils.isBlank(template) ||
                StringUtils.isBlank(link)) {
                log.error("Empty phone number fields.");
                throw getCreateSmsException("Empty phone number fields.", advance.getId().toString());
            }
            result = formatMessageWithUrl(template, tripNum, sum, link);
        } catch (IllegalFormatException e) {
            log.info("Format message error: " + e.getMessage());
            throw getCreateSmsException("Format sms text error: " + e.getMessage(), advance.getId().toString());
        }
        return result;
    }

    private String getEmailText(TripAdvance advance) throws MessagingException {
        String result = null;
        String template = appProperties.getEmailMessageTemplate();
        URL url = appProperties.getLkUrl();
        boolean isShortLink = appProperties.isEmailCutLinks() == null ?
                                false : appProperties.isEmailCutLinks();


        if (null == template || null == url ||
                StringUtils.isBlank(url.toString()) ||
                StringUtils.isBlank(template)){
            throw getCreateEmailException("Format email-message error: Empty e-mail creation properties: ", advance.getId().toString());
        }

        String link = url.toString() + advance.getAdvanceUuid();
        try {
            if (isShortLink) {
                link = urlCutter.editUrl(link);
            }
            String tripNum = advance.getTrip().getNum();
            Double sum = advance.getAdvancePaymentSum();
            result = formatMessageWithUrl(template, tripNum, sum, link);
        } catch (IllegalFormatException e) {
            log.info("Format message error: " + e.getMessage());
            throw getCreateEmailException("Format email-message error: " + e.getMessage(),
                advance.getId().toString());
        }
        return result;
    }

    private String getEmailHeader(TripAdvance advance) throws MessagingException {
        String result = null;
        String template = appProperties.getEmailHeaderTemplate();
        if (template == null){
            throw getCreateEmailException("Format email-header error: Empty property",
                advance.getId().toString());
        }
        try {
            String tripNum = advance.getTrip().getNum();
            result = formatMessageHeader(template, tripNum);
        } catch (IllegalFormatException e) {
            log.info("Format message error: " + e.getMessage());
            throw getCreateEmailException("Format email-header error: " + e.getMessage(),
                advance.getId().toString());
        } catch (MessagingException e) {
            throw getCreateEmailException("Format email-header error: " + e.getMessage(),
                advance.getId().toString());
        }
        return result;
    }


    private String formatMessageHeader(String headerTemplate, String tripNum) throws MessagingException {
        if (StringUtils.isBlank(headerTemplate) ||
            StringUtils.isBlank(tripNum)) {
            log.error("Empty message-header fields.");
            throw getFormatException("Empty message-header fields for tripNum ", tripNum);
        }
        return String.format(headerTemplate, tripNum);
    }

    private String formatMessage(String textTemplate, String num, Double sum, String lkLink) {
        return String.format(textTemplate, num, sum, lkLink);
    }

    private String formatMessageWithUrl(String textTemplate, String num, Double sum, String lkLink) throws MessagingException {
        if (StringUtils.isBlank(textTemplate) ||
            StringUtils.isBlank(num) ||
            sum == 0.0f ||
            StringUtils.isBlank(lkLink)) {
            log.error("Empty message-text fields.");
            throw getFormatException("Empty message-text fields for tripNum ", num);
        }
        return String.format(textTemplate, num, sum, lkLink);
    }

    private MessagingException getCreateEmailException(String message, String id) {
        Error error = new Error("Error while email creating for advance "
            + id + ". Messages: " + message);
        return new MessagingException(HttpStatus.INTERNAL_SERVER_ERROR, error);
    }

    private MessagingException getCreateSmsException(String message, String id) {
        Error error = new Error("Error while creating SMS for advance "
            + id + ". Messages: " + message);
        return new MessagingException(HttpStatus.INTERNAL_SERVER_ERROR, error);
    }

    private MessagingException getFormatException(String message, String id) {
        Error error = new Error("Error while formatting message:"
            + id + ". Messages: " + message);
        return new MessagingException(HttpStatus.INTERNAL_SERVER_ERROR, error);
    }
}
