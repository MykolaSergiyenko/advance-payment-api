package online.oboz.trip.trip_carrier_advance_payment_api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.TripRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.service.dto.MessageDto;
import online.oboz.trip.trip_carrier_advance_payment_api.service.dto.SmsRequestDelayed;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
    private static final String RUSSIAN_COUNTRY_CODE = "7";
    private static final String SEND_SMS_METHOD_PATH = "/v1/send-sms";
    //    "Компания %Название компании% (из поля «Юр. лицо для взаиморасчетов» в ОБОЗе) предлагает аванс по \n " +
//        "заказу %номер заказа поставщика% на сумму %сумма аванса с НДС%, для подтверждения пройдите по ссылке (ссылка на зеркало). ";
    private static final String EMAIL_HEADER_TEMPLATE = "Компания %s  предлагает аванс.";
    private static final String MESSAGE_TEXT = "Компания %s  предлагает аванс по \n " +
        "заказу %s на сумму %s, для подтверждения пройдите по ссылке %s.";
    private final JavaMailSender emailSender;
    private final DelayQueue<Delayed> delayQueue = new DelayQueue<>();
    private final RestTemplate restTemplate;
    private final ApplicationProperties applicationProperties;
    private final TripRepository tripRepository;

    @Scheduled(cron = "${cron.update: 0 0/30 * * * *}")
    private void checkDelayedSendSms() {
        String url = applicationProperties.getSmsSenderUrl();
        try {
            Delayed sms = delayQueue.poll();
            while (sms != null) {
                sendSms(url, sms);
                sms = delayQueue.poll();
            }
        } catch (Exception e) {
            log.error("Some Exeption", e);
        }
    }

    private void sendSms(String url, Delayed sms) {
        ResponseEntity<String> response = restTemplate.postForEntity(
            url + SEND_SMS_METHOD_PATH,
            sms,
            String.class
        );
        log.info("Sms server response {}", response);
        if (response.getStatusCode().value() != 200) {
            log.error("Sms server returned bad response {}", response);
        }
    }

    public void sendSmsDelay(MessageDto messageDto) {
        SmsRequestDelayed smsRequestDelayed = new SmsRequestDelayed(
            getMessageText(messageDto),
            RUSSIAN_COUNTRY_CODE + messageDto.getPhone(),
            applicationProperties.getSmsSendDelay()
        );
        log.info("add smsRequestDelayed to delayQueue");
        delayQueue.add(smsRequestDelayed);
    }

    public void sendEmail(MessageDto messageDto) {
        if (applicationProperties.getMailEnable()) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(applicationProperties.getMailUsername());
            message.setTo(messageDto.getEmail());
            String subject = String.format(EMAIL_HEADER_TEMPLATE,
                messageDto.getContractorName());
            String text = getMessageText(messageDto);
            message.setText(text);
            message.setSubject(subject);
            try {
                emailSender.send(message);
                log.info("Sending message to email: {} with text: {} send success", message.getTo(), text);
            } catch (Exception ex) {
                log.error("Error while sending message. to - {} subject - {}", message.getTo(), subject, ex);
                throw ex;
            }
        } else log.info("Sending message properties disable, email message  to - {}", messageDto.getEmail());
    }

    private String getMessageText(MessageDto messageDto) {
        return String.format(MESSAGE_TEXT,
            getContractorContractorName(messageDto),
            messageDto.getTripNum(),
            messageDto.getAdvancePaymentSum(),
            messageDto.getLKLink());
    }

    private String getContractorContractorName(MessageDto messageDto) {
        return messageDto.getContractorName();
    }

}

