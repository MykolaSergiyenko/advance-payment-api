package online.oboz.trip.trip_carrier_advance_payment_api.service.messages.common.format;

import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.dicts.contacts.AdvanceContactsBook;
import online.oboz.trip.trip_carrier_advance_payment_api.service.contacts.ContactService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.urleditor.UrlShortenerService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.urleditor.UrlService;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.notificatoins.EmailContainer;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.notificatoins.SmsContainer;
import online.oboz.trip.trip_carrier_advance_payment_api.util.StringUtils;
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
 * Используется в {@link online.oboz.trip.trip_carrier_advance_payment_api.service.messages.NotificationService}.
 * <p>
 *
 * @author s‡udent
 * @see TextService
 */
@Service
public class MessageCreateService implements TextService {
    private static final Logger log = LoggerFactory.getLogger(MessageCreateService.class);



    private final ApplicationProperties appProperties;
    private final ContactService contactService;
    private final UrlService urlCutter;
    private final String sendFromEmail;


    @Autowired
    public MessageCreateService(
        ApplicationProperties appProperties,
        ContactService contactService,
        UrlService urlCutter) {
        this.appProperties = appProperties;
        this.urlCutter = urlCutter;
        this.contactService = contactService;
        this.sendFromEmail = appProperties.getMailUsername();
    }


    @Override
    public SmsContainer createSms(Advance advance) throws MessagingException {
        String text = getSmsText(advance);
        String phone = getPhoneNumber(advance.getContact().getInfo().getPhone());
        String tripNum = advance.getAdvanceTripFields().getNum();
        if (StringUtils.isEmptyStrings(tripNum, phone, text)) {
            throw getCreateSmsException("Empty sms-fields for advance ", advance.getId().toString());
        }
        SmsContainer container = new SmsContainer(text, phone, tripNum);
        return container;
    }


    @Override
    public EmailContainer createEmail(Advance advance) throws MessagingException {
        // Send notifications from app.props email?
        String from = sendFromEmail;
        String to = contactService.findByContractor(advance.getContractorId()).getInfo().getEmail();

        String subject = getEmailHeader(advance);
        String text = getEmailText(advance);
        if (StringUtils.isEmptyStrings(from, to, subject, text)) {
            throw getCreateEmailException("Empty email-fields for advance ", advance.getId().toString());
        }
        EmailContainer email = new EmailContainer(from, to, subject, text);
        return email;
    }


    private String getPhoneNumber(String phoneNumber) throws MessagingException {
        try {
            return formatPhone(phoneNumber);
        } catch (IllegalFormatException e) {
            log.error("Format message error: " + e.getMessage());
            throw getMessagingError("Format phone-number error: " + phoneNumber
                + " --- " + e.getMessage());
        }
    }

    private String formatPhone(String num) throws MessagingException {
        String template = appProperties.getSmsPhoneTemplate();
        if (StringUtils.isEmptyStrings(template, num)) {
            log.error("Empty phone number fields.");
            throw getMessagingError("Empty phone number fields for advance ");
        }
        return String.format(template, num);
    }


    private String getSmsText(Advance advance) throws MessagingException {
        String template = appProperties.getSmsMessageTemplate();
        URL url = appProperties.getLkUrl();
        String link = url.toString() + advance.getUuid();
        try {
            if (appProperties.isSmsCutLinks()) {
                link = urlCutter.editUrl(link);
                log.info("Short url is: " + link);
            }
            String tripNum = advance.getAdvanceTripFields().getNum();
            Double sum = advance.getTripAdvanceInfo().getAdvancePaymentSum();
            return formatMessageWithUrl(template, tripNum, sum, link);
        } catch (IllegalFormatException e) {
            log.info("Format message error: " + e.getMessage());
            throw getCreateSmsException("Format sms text error: " + e.getMessage(), advance.getId().toString());
        }
    }


    private String getEmailText(Advance advance) throws MessagingException {
        String template = appProperties.getEmailMessageTemplate();
        URL url = appProperties.getLkUrl();
        String link = url.toString() + advance.getUuid();
        try {
            if (appProperties.isEmailCutLinks()) {
                link = urlCutter.editUrl(link);
            }
            String tripNum = advance.getAdvanceTripFields().getNum();
            Double sum = advance.getTripAdvanceInfo().getAdvancePaymentSum();
            return formatMessageWithUrl(template, tripNum, sum, link);
        } catch (IllegalFormatException e) {
            log.info("Format message error: " + e.getMessage());
            throw getCreateEmailException("Format email-message error: " + e.getMessage(),
                advance.getId().toString());
        }
    }

    private String getEmailHeader(Advance advance) throws MessagingException {
        String template = appProperties.getEmailHeaderTemplate();
        try {
            String tripNum = advance.getAdvanceTripFields().getNum();
            return formatMessageHeader(template, tripNum);
        } catch (IllegalFormatException e) {
            log.info("Format message error: " + e.getMessage());
            throw getCreateEmailException("Format email-header error: " + e.getMessage(),
                advance.getId().toString());
        } catch (MessagingException e) {
            throw getCreateEmailException("Format email-header error: " + e.getMessage(),
                advance.getId().toString());
        }
    }


    private String formatMessageHeader(String headerTemplate, String tripNum) throws MessagingException {
        if (StringUtils.isEmptyStrings(headerTemplate, tripNum)) {
            log.error("Empty message-header fields.");
            throw getFormatException("Empty message-header fields for tripNum ", tripNum);
        }
        return String.format(headerTemplate, tripNum);
    }

    private String formatMessageWithUrl(String textTemplate, String num, Double sum, String lkLink) throws MessagingException {
        if (StringUtils.isEmptyStrings(textTemplate, num, sum.toString(), lkLink)) {
            log.error("Empty message-text fields.");
            throw getFormatException("Empty message-text fields for tripNum ", num);
        }
        return formatMessage(textTemplate, num, sum, lkLink);
    }

    private String formatMessage(String textTemplate, String num, Double sum, String lkLink) {
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

    private MessagingException getMessagingError(String message) {
        Error error = new Error("Error while formatting message. Messages: " + message);
        return new MessagingException(HttpStatus.INTERNAL_SERVER_ERROR, error);
    }

}
