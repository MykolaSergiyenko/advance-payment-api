package online.oboz.trip.trip_carrier_advance_payment_api.service.messages;


import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import online.oboz.trip.trip_carrier_advance_payment_api.service.advance.tools.contacts.ContactService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.edit_message.MessagingException;
import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.edit_message.MessagesService;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.notificatoins.EmailContainer;
import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.email.EmailSender;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.notificatoins.SendSmsRequest;
import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.sms.SmsSender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;


/**
 * <b>Новый сервис отправки уведомлений по авансированию</b>
 * <p>
 *
 * @author s‡udent
 * @see Notificator
 * @see ApplicationProperties
 * @see MessagesService
 * @see EmailSender
 * @see SmsSender
 */
@Service
public class NotificationService implements Notificator {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final ContactService contactService;
    private final MessagesService messagesService;
    private final EmailSender emailSender;
    private final SmsSender smsSender;

    private final Boolean emailEnabled;
    private final Boolean smsEnabled;

    private final Boolean emailScheduleEnabled;
    private final Boolean smsScheduleEnabled;


    public NotificationService(
        ApplicationProperties applicationProperties,
        MessagesService messageService,
        EmailSender emailSender,
        SmsSender smsSender,
        ContactService contactService
    ) {
        this.messagesService = messageService;
        this.emailSender = emailSender;
        this.smsSender = smsSender;
        this.contactService = contactService;

        this.emailEnabled = applicationProperties.isEmailEnabled();
        this.smsEnabled = applicationProperties.isSmsEnabled();

        this.emailScheduleEnabled = applicationProperties.isEmailScheduleEnabled();
        this.smsScheduleEnabled = applicationProperties.isSmsScheduleEnabled();
    }

    /**
     * @param advance Аванс ("Заявка на авансирование")
     *                Notificate contractor for advance by "simple" (at-moment) messages:
     *                by sms and e-mail if both enable in application properties.
     */
    @Override
    public Advance notify(Advance advance) {
        if (emailEnabled || smsEnabled) advance = notificate(advance, emailEnabled, smsEnabled);
        else {
            log.info("[Сообщения об авансе] - смс и и-мейл отключены. Аванс: {}.", advance.getId());
        }
        return advance;
    }


    /**
     * @param advances Аванс
     *                 Notificate contractor for advance by "delayed" (scheduled) messages:
     *                 by sms and e-mail if both enable in application properties.
     */
    @Override
    public List<Advance> repeatNotify(List<Advance> advances) {
        notificate(advances, emailScheduleEnabled, smsScheduleEnabled);
        return advances;
    }


    private void notificate(List<Advance> advances, boolean emailEnable, boolean smsEnable) {
        if (emailEnable || smsEnable) {
            advances.forEach(advance -> {
                notificate(advance, emailEnable, smsEnable);
                advance.setNotifiedAt(OffsetDateTime.now());
            });
        } else {
            log.info("[Сообщения об авансе] - отложенные смс и и-мейлы отключены. " +
                "Но найдено {} непрочитанных сообщений об авансе.", advances.size());
        }
    }

    private Advance notificate(Advance advance, boolean emailEnable, boolean smsEnable) {
        if (emailEnable) {
            advance = sendEmails(advance);
        } else {
            log.info("[Сообщения об авансе] по имейлу отключены.");
        }
        if (smsEnable) {
            advance = sendSMSes(advance);
        } else {
            log.info("[Сообщения об авансе] по смс отключены.");
        }
        return advance;
    }

    private Advance sendEmails(Advance advance) {
        try {
            sendEmail(advance);
            advance.setEmailSentAt(OffsetDateTime.now());
        } catch (MessagingException e) {
            log.error("MessagingException while email:" + e.getErrors());
        }
        return advance;
    }

    private void sendEmail(Advance advance) throws MessagingException {
        log.info("Сообщения по электронной почте включены. Отправляем сообщение по авансу: {}.", advance.getId());
        EmailContainer email = messagesService.createEmail(advance,
            contactService.getEmail(advance.getContractorId()));
        log.info("Электронное письмо: " + email.getMessage().toString());
        emailSender.sendEmail(email);
        log.info("Электронное письмо отправлено по авансу: {}.", advance.getId());
    }

    private Advance sendSMSes(Advance advance) {
        try {
            sendSms(advance);
            advance.setSmsSentAt(OffsetDateTime.now());
        } catch (MessagingException e) {
            log.error("MessagingException while sms:" + e.getErrors());
        }
        return advance;
    }


    private void sendSms(Advance advance) throws MessagingException {
        log.info("Уведомления по СМС включены. Отправляем сообщение по авансу: {}.",  advance.getId());
        SendSmsRequest container = messagesService.createSms(advance,
            contactService.getPhone(advance.getContractorId()));
        log.info("СМС: " + container.toString());
        smsSender.sendSms(container);
        log.info("СМС отправлена по авансу: {}.", advance.getId());
    }

}
