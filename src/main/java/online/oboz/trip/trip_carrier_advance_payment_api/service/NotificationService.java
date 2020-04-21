package online.oboz.trip.trip_carrier_advance_payment_api.service;

import lombok.RequiredArgsConstructor;
import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.service.dto.MessageDto;
import online.oboz.trip.trip_carrier_advance_payment_api.service.dto.SendSmsRequest;
import online.oboz.trip.trip_carrier_advance_payment_api.service.dto.SmsRequestDelayed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static final String RUSSIAN_COUNTRY_CODE = "7";
    private static final String SEND_SMS_METHOD_PATH = "/v1/send-sms";
    private static final String EMAIL_HEADER_TEMPLATE = "Компания %s  предлагает аванс по заказу %s ";
    private static final String MESSAGE_TEXT = "Компания %s  предлагает аванс по \n " +
        "заказу %s на сумму %s руб., для подтверждения пройдите по ссылке %s";

    private final JavaMailSender emailSender;
    private final RestTemplate restTemplate;
    private final ApplicationProperties applicationProperties;

    private void sendSms(String url, SmsRequestDelayed sms) {
        ResponseEntity<String> response = restTemplate.postForEntity(
            url + SEND_SMS_METHOD_PATH,
            new SendSmsRequest(sms.getText(), sms.getPhone(), sms.getTripNum()),
            String.class
        );
        if (response.getStatusCode().value() != 200) {
            throw new RuntimeException("Sms server returned bad response" + response);
        }
        log.info("Success send notification sms to " + sms.getPhone());

        if (response.getStatusCode() != HttpStatus.OK) {
            log.error("Sms server returned bad response {}", response);
        }
    }

    public void sendSmsDelay(MessageDto messageDto) {
        log.info("Send sms " + messageDto);
        try {
            SmsRequestDelayed smsRequestDelayed = new SmsRequestDelayed(
                getMessageText(messageDto),
                RUSSIAN_COUNTRY_CODE + messageDto.getPhone(),
                messageDto.getTripNum(),
                applicationProperties.getSmsSendDelay()
            );

            sendSms(applicationProperties.getSmsSenderUrl(), smsRequestDelayed);
        } catch (Exception e) {
            log.error("Failed send sms " + messageDto, e);
        }
    }

    public void sendEmail(MessageDto messageDto) {
        if (applicationProperties.getMailEnable()) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(applicationProperties.getMailUsername());
            message.setTo(messageDto.getEmail());
            String subject = String.format(EMAIL_HEADER_TEMPLATE,
                messageDto.getContractorName(), messageDto.getTripNum()
            );
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
        } else {
            log.info("Sending message properties disable, email message  to - {}", messageDto.getEmail());
        }
    }

    private String getMessageText(MessageDto messageDto) {
        return String.format(MESSAGE_TEXT,
            getContractorContractorName(messageDto),
            messageDto.getTripNum(),
            messageDto.getAdvancePaymentSum(),
            messageDto.getLKLink()
        );
    }

    private String getContractorContractorName(MessageDto messageDto) {
        return messageDto.getContractorName();
    }
}

