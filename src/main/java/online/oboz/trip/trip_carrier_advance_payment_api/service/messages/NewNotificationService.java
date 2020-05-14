package online.oboz.trip.trip_carrier_advance_payment_api.service.messages;


import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.TripAdvance;
import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.common.format.MessageTextService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.email.EmailContainer;
import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.email.EmailSenderService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.sms.SmsContainer;
import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.sms.SmsSenderService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class NewNotificationService {
    private static final Logger log = LoggerFactory.getLogger(NewNotificationService.class);

    private final ApplicationProperties applicationProperties;
    private final MessageTextService messageTextService;
    private final EmailSenderService emailSender;
    private final SmsSenderService smsSender;

    public NewNotificationService(ApplicationProperties applicationProperties,
                                  MessageTextService messageTextService,
                                  EmailSenderService emailSender,
                                  SmsSenderService smsSender) {
        this.applicationProperties = applicationProperties;
        this.messageTextService = messageTextService;
        this.emailSender = emailSender;
        this.smsSender = smsSender;
    }

    public void notificate(TripAdvance advance){
        notificate(advance,
            applicationProperties.isEmailEnabled(),
            applicationProperties.isSmsEnabled());
    }

    public void sheduledNotificate(TripAdvance advance){
        notificate(advance,
            applicationProperties.isEmailScheduleEnabled(),
            applicationProperties.isSmsScheduleEnabled());
    }


    private void notificate(TripAdvance advance,
                            boolean emailEnable, boolean smsEnable){
        if (emailEnable) {
            EmailContainer email = messageTextService.createEmail(advance);
            emailSender.sendEmail(email);
        } else {
            log.info("Notification by e-mail is unable.");
        }
        if (smsEnable) {
            SmsContainer container = messageTextService.createSms(advance);
            smsSender.sendSms(container);
        } else {
            log.info("Notification by sms is unable.");
        }
    }

}
