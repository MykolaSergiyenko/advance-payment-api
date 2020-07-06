package online.oboz.trip.trip_carrier_advance_payment_api.service.messages;


import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.AdvanceRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.common.format.MessagingException;
import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.common.format.TextService;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.notificatoins.EmailContainer;
import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.email.EmailSender;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.notificatoins.SmsContainer;
import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.sms.SmsSender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;


/**
 * <b>Новый сервис отправки уведомлений по авансированию</b>
 * <p>
 *
 * @author s‡udent
 * @see Notificator
 * @see ApplicationProperties
 * @see TextService
 * @see EmailSender
 * @see SmsSender
 */
@Service
public class NotificationService implements Notificator {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final ApplicationProperties applicationProperties;
    private final TextService messageTextService;
    private final EmailSender emailSender;
    private final SmsSender smsSender;

    public NotificationService(ApplicationProperties applicationProperties,
                               TextService messageTextService,
                               EmailSender emailSender,
                               SmsSender smsSender) {
        this.applicationProperties = applicationProperties;
        this.messageTextService = messageTextService;
        this.emailSender = emailSender;
        this.smsSender = smsSender;

    }

    /**
     * @param advance Аванс ("Заявка на авансирование")
     *                Notificate contractor for advance by "simple" (at-moment) messages:
     *                by sms and e-mail if both enable in application properties.
     */
    @Override
    public Advance notify(Advance advance) {
        notificate(advance,
            applicationProperties.isEmailEnabled(),
            applicationProperties.isSmsEnabled());
//        advance.setNotifiedAt(OffsetDateTime.now());
        return advance;
    }


    //TODO: must be used by cron-notification

    /**
     * @param advance advance Аванс ("Заявка на авансирование")
     *                Notificate contractor for advance by "simple" (at-moment) messages:
     *                by sms and e-mail if both enable in application properties.
     */
    @Override
    public Advance scheduledNotify(Advance advance) {
        notificate(advance,
            applicationProperties.isEmailScheduleEnabled(),
            applicationProperties.isSmsScheduleEnabled());

        return advance;
    }

    private void notificate(Advance advance,
                            boolean emailEnable, boolean smsEnable) {
        if (emailEnable) {
            try {
                log.info("Email-messages enable. Try to send message for advance - " + advance.getId());
                EmailContainer email = messageTextService.createEmail(advance);
                log.info("Create e-mail-message: " + email.getMessage().toString());
                emailSender.sendEmail(email);
                log.info("E-mail is sent for advance - " + advance.getId());
                advance.setEmailSentAt(OffsetDateTime.now());
            } catch (Exception e) {
                log.error("MessagingException while email - " + e.getMessage());
            }
        } else {
            log.info("Notification by e-mail is unable.");
        }
        if (smsEnable) {
            try {
                log.info("SMS-messages enable. Try to send message for advance - " + advance.getId());
                SmsContainer container = messageTextService.createSms(advance);
                log.info("Create SMS-message: " + container.toString());
                smsSender.sendSms(container);
                log.info("SMS is sent for advance " + advance.getId());
                advance.setSmsSentAt(OffsetDateTime.now());
            } catch (MessagingException e) {
                log.error("MessagingException while sms:" + e.getErrors());
            }
        } else {
            log.info("Notification by sms is unable.");
        }
    }
}
