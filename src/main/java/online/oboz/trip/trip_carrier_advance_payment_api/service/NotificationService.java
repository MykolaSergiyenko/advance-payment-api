package online.oboz.trip.trip_carrier_advance_payment_api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.service.dto.MessageDto;
import online.oboz.trip.trip_carrier_advance_payment_api.service.dto.SendSmsRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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

    // String url = "http://da-checking-service:8080";
    // String url = "http://sms-sender.r14.k.preprod.oboz:30080";

    private final RestTemplate restTemplate;
    private final ApplicationProperties applicationProperties;
//
//    public NotificationService(RestTemplate restTemplate, ApplicationProperties applicationProperties) {
//        this.restTemplate = restTemplate;
//        this.applicationProperties = applicationProperties;
//    }

    public void sendSms(MessageDto messageDto) {
        String url = applicationProperties.getSmsSenderUrl();
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                url + SEND_SMS_METHOD_PATH,
                new SendSmsRequest(getMessageText(messageDto), RUSSIAN_COUNTRY_CODE + messageDto.getPhone()),
                String.class
            );
            if (response.getStatusCode().value() != 200) {
                log.error("Sms server returned bad response" + response);
            }
        } catch (Exception e) {
            log.error("Some Exeption" + e.getMessage());
        }
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

