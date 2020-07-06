package online.oboz.trip.trip_carrier_advance_payment_api.service.messages.email;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.notificatoins.EmailContainer;
import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.common.format.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


/**
 * <b>Сервис отправки электронной почты</b>
 *
 * @author s‡udent
 * @see EmailSender
 * @see JavaMailSender
 * @see EmailContainer
 */
@Service
public class EmailSenderService implements EmailSender {
    Logger log = LoggerFactory.getLogger(EmailSenderService.class);

    //from springframework.mail
    JavaMailSender emailSender;

    @Autowired
    public EmailSenderService(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    @Override
    public void sendEmail(EmailContainer email) throws MessagingException {
        try {
            emailSender.send(email.getMessage());
        } catch (MailException e) {
            throw getSendingException(e.getMessage(), email);
        }
    }

    private MessagingException getSendingException(String message, EmailContainer email) {
        Error error = new Error("Error while email-sending to "
            + email.getMessage().getTo()[0]
            + ". Messages: " + message);
        return new MessagingException(HttpStatus.BAD_REQUEST, error);
    }
}
