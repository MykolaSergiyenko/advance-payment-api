package online.oboz.trip.trip_carrier_advance_payment_api.service.messages.edit_message;

import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;

import online.oboz.trip.trip_carrier_advance_payment_api.service.urleditor.UrlShortenerService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.urleditor.UrlService;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.notificatoins.EmailContainer;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.notificatoins.SendSmsRequest;
import online.oboz.trip.trip_carrier_advance_payment_api.service.util.ErrorUtils;
import online.oboz.trip.trip_carrier_advance_payment_api.service.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
 * @see MessagesService
 */
@Service
public class MessageCreateService implements MessagesService {
    private static final Logger log = LoggerFactory.getLogger(MessageCreateService.class);

    private final UrlService urlCutter;
    private final URL carrierUrl;

    private final String phoneTemplate;
    private final String smsTemplate;
    private final Boolean cutSmsUrl;

    private final String sendFromEmail;
    private final String emailTemplate;
    private final String emailHeaderTemplate;
    private final Boolean cutEmailUrl;


    @Autowired
    public MessageCreateService(
        ApplicationProperties appProperties,
        UrlService urlCutter
    ) {
        this.urlCutter = urlCutter;
        this.sendFromEmail = appProperties.getMailUsername();
        this.phoneTemplate = appProperties.getSmsPhoneTemplate();
        this.smsTemplate = appProperties.getSmsMessageTemplate();
        this.carrierUrl = appProperties.getLkUrl();
        this.cutSmsUrl = appProperties.isSmsCutLinks();
        this.emailTemplate = appProperties.getEmailMessageTemplate();
        this.emailHeaderTemplate = appProperties.getEmailHeaderTemplate();
        this.cutEmailUrl = appProperties.isEmailCutLinks();
    }


    @Override
    public SendSmsRequest createSms(Advance advance, String to) throws MessagingException {
        String text = getSmsText(advance);
        String phone = to;
        String tripNum = advance.getAdvanceTripFields().getNum();
        if (StringUtils.isEmptyStrings(tripNum, phone, text)) {
            throw getCreateSmsException("Не заполнены данные для смс по авансу: ", advance.getId().toString());
        }
        SendSmsRequest container = new SendSmsRequest(text, phone);
        return container;
    }


    @Override
    public EmailContainer createEmail(Advance advance, String to) throws MessagingException {
        // Send notifications from app.props email?
        String from = sendFromEmail;
        String subject = getEmailHeader(advance);
        String text = getEmailText(advance);
        if (StringUtils.isEmptyStrings(from, to, subject, text)) {
            throw getCreateEmailException("Не заполнен электронный адрес по авансу: {}.", advance.getId().toString());
        }
        EmailContainer email = new EmailContainer(from, to, subject, text);
        return email;
    }

    private String formatPhone(String num) throws MessagingException {
        String template = phoneTemplate;
        if (StringUtils.isEmptyStrings(template, num)) {
            log.error("Номер телефона пуст.");
            throw getMessagingError("Пустой телефонный номер ");
        }
        return String.format(template, num);
    }


    private String getSmsText(Advance advance) throws MessagingException {
        String template = smsTemplate;
        String link = carrierUrl.toString() + advance.getUuid();
        try {
            if (cutSmsUrl) {
                link = urlCutter.editUrl(link);
            }
            String tripNum = advance.getAdvanceTripFields().getNum();
            Double sum = advance.getTripAdvanceInfo().getAdvancePaymentSum();
            return formatMessageWithUrl(template, tripNum, sum, link);
        } catch (IllegalFormatException e) {
            log.info("Ошибка форматирования смс: " + e.getMessage());
            throw getCreateSmsException("Ошибка форматирования текста смс: " + e.getMessage(), advance.getId().toString());
        }
    }


    private String getEmailText(Advance advance) throws MessagingException {
        String template = emailTemplate;
        String link = carrierUrl.toString() + advance.getUuid();
        try {
            if (cutEmailUrl) {
                link = urlCutter.editUrl(link);
            }
            String tripNum = advance.getAdvanceTripFields().getNum();
            Double sum = advance.getTripAdvanceInfo().getAdvancePaymentSum();
            return formatMessageWithUrl(template, tripNum, sum, link);
        } catch (IllegalFormatException e) {
            log.info("Ошибка форматирования письма: " + e.getMessage());
            throw getCreateEmailException("Ошибка форматирования текста письма: " + e.getMessage(),
                advance.getId().toString());
        }
    }

    private String getEmailHeader(Advance advance) throws MessagingException {
        try {
            String tripNum = advance.getAdvanceTripFields().getNum();
            return formatMessageHeader(emailHeaderTemplate, tripNum);
        } catch (IllegalFormatException e) {
            log.info("Ошибка создания сообщения об авансе: {}.", e.getMessage());
            throw getCreateEmailException("Ошибка форматирования хедера электронного письма: " + e.getMessage(),
                advance.getId().toString());
        } catch (MessagingException e) {
            throw getCreateEmailException("Ошибка форматирования хедера электронного письма: " + e.getMessage(),
                advance.getId().toString());
        }
    }


    private String formatMessageHeader(String headerTemplate, String tripNum) throws MessagingException {
        if (StringUtils.isEmptyStrings(headerTemplate, tripNum)) {
            log.error("Пустые поля для заголовка электронного письма.");
            throw getFormatException("Пустые поля для заголовка электронного письма по заказу ", tripNum);
        }
        return String.format(headerTemplate, tripNum);
    }

    private String formatMessageWithUrl(String textTemplate, String num, Double sum, String lkLink) throws MessagingException {
        if (StringUtils.isEmptyStrings(textTemplate, num, sum.toString(), lkLink)) {
            log.error("Пустые поля для письма.");
            throw getFormatException("Пустые поля для уведомления по заказу ", num);
        }
        return formatMessage(textTemplate, num, sum, lkLink);
    }

    private String formatMessage(String textTemplate, String num, Double sum, String lkLink) {
        return String.format(textTemplate, num, sum, lkLink);
    }

    private MessagingException getCreateEmailException(String message, String id) {
        return ErrorUtils.getMessagingError("Ошибка создания уведомления по авансу: " + id + ". Ошибка: " + message);
    }

    private MessagingException getCreateSmsException(String message, String id) {
        String error = ("Ошибка создания СМС по авнсу: " + id + ". Ошибки: " + message);
        return ErrorUtils.getMessagingError(error);
    }

    private MessagingException getFormatException(String message, String id) {
        return ErrorUtils.getMessagingError("Ошибка форматирования сообщения по авансу:" + id + ". Ошибка: " + message);
    }

    private MessagingException getMessagingError(String message) {
        return ErrorUtils.getMessagingError("Ошибка форматирования сообщения. Ошибка: " + message);
    }

}
