package online.oboz.trip.trip_carrier_advance_payment_api.service.messages.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;

import org.springframework.mail.javamail.JavaMailSender;

import org.springframework.stereotype.Service;


@Service
public class EmailSenderService {
    Logger log = LoggerFactory.getLogger(EmailSenderService.class);

    //from springframework.mail
    JavaMailSender emailSender;

    @Autowired
    EmailSenderService(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    public void sendEmail(EmailContainer email) {
        try{
            emailSender.send(email.getMessage());
        } catch (MailException e){
            log.error("Error while email-sending to " + email.getMessage().getTo()[0] +
                ". Messages: "+e.getMessage());
        }
    }
}
