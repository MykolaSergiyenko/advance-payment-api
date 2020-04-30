package online.oboz.trip.trip_carrier_advance_payment_api.service;

import io.undertow.util.BadRequestException;
import lombok.RequiredArgsConstructor;
import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.*;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.error.SmsSendingException;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.*;
import online.oboz.trip.trip_carrier_advance_payment_api.service.dto.MessageDto;
import online.oboz.trip.trip_carrier_advance_payment_api.service.dto.SendSmsRequest;
import online.oboz.trip.trip_carrier_advance_payment_api.service.dto.SmsRequestDelayed;
import online.oboz.trip.trip_carrier_advance_payment_api.util.DtoUtils;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.Error;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@EnableScheduling
@RequiredArgsConstructor
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static final String IN_SMS_COMPANY_NAME = "ОБОЗ";
    private static final String RUSSIAN_COUNTRY_CODE = "7";
    private static final String SEND_SMS_METHOD_PATH = "/v1/send-sms";
    private static final String EMAIL_HEADER_TEMPLATE = "Компания %s  предлагает аванс по заказу %s ";
    private static final String MESSAGE_TEXT = "Компания %s  предлагает аванс по заказу\n" +
        "%s на сумму %.0f руб., для просмотра пройдите по ссылке \n%s";

    private static final String MESSAGE_TEXT_SMS = "Компания %s  предлагает аванс по заказу " +
        "%s на сумму %.0f руб., для просмотра пройдите по ссылке %s";

    private final JavaMailSender emailSender;
    private final RestTemplate restTemplate;
    private final ApplicationProperties applicationProperties;

    private final AdvanceRequestRepository advanceRequestRepository;
    private final AdvanceContactRepository advanceContactRepository;
    private final ContractorRepository contractorRepository;
    private final TripRepository tripRepository;


    @Scheduled(cron = "${cron.sms-notify: 0 0/1 * * * *}")
    public void scheduledSms() {
        List<TripRequestAdvancePayment> advances = advanceRequestRepository.findForNotification();
        advances.forEach(advance -> {
                log.info("Found advance-request with unread e-mail - {}.", advance.getId());
                try {
                    String errMessage;
                    Trip trip = tripRepository.getMotorTrip(advance.getTripId()).orElse(null);
                    if (null == trip) {
                        errMessage = "Trip not found, id = " + advance.getTripId();
                        throw getSmsException(errMessage, HttpStatus.NOT_FOUND);
                    }
                    ContractorAdvancePaymentContact contact = advanceContactRepository.find(trip.getContractorId()).orElse(null);
                    if (null == contact) {
                        errMessage = "Contact not found for trip " + trip.getNum();
                        throw getSmsException(errMessage, HttpStatus.NOT_FOUND);
                    }
                    MessageDto messageDto = DtoUtils.newMessage(advance, contact, trip.getNum(),
                        contractorRepository, applicationProperties);
                    sendSms(messageDto);

                    setSmsSent(advance);
                } catch (SmsSendingException e) {
                    log.error("Sms-sending error: {}", e.getErrors());
                }
            }
        );
    }

    private void sendSms(MessageDto messageDto) throws SmsSendingException {
        log.info("Send sms " + messageDto);
        messageDto.setContractorName(IN_SMS_COMPANY_NAME);
        String text = getMessageText(messageDto);
        if (StringUtils.isBlank(text)) {
            throw getSmsException("SMS text is empty.", HttpStatus.NO_CONTENT);
        }
        SmsRequestDelayed smsRequestDelayed = new SmsRequestDelayed(
            text,
            RUSSIAN_COUNTRY_CODE + messageDto.getPhone(),
            messageDto.getTripNum(),
            applicationProperties.getSmsSendDelay()
        );
        sendSmsRequest(applicationProperties.getSmsSenderUrl(), smsRequestDelayed);
    }


    private void sendSmsRequest(String url, SmsRequestDelayed sms) throws SmsSendingException {
        String errMessage = "SMS sending error for phone-number " + sms.getPhone();
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                url + SEND_SMS_METHOD_PATH,
                new SendSmsRequest(sms.getText(), sms.getPhone(), sms.getTripNum()),
                String.class
            );
            if (response.getStatusCode() != HttpStatus.OK) {
                throw getSmsException(errMessage, response.getStatusCode());
            }
            log.info("Success send notification sms to " + sms.getPhone());
        } catch (HttpServerErrorException e) {
            throw getSmsException(errMessage + ". Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void setSmsSent(TripRequestAdvancePayment advance) {
        advance.setIsSmsSent(true);
        advanceRequestRepository.save(advance);
        log.info("Set sms-sent for advance-request " + advance.getId());
    }


    public void sendEmail(MessageDto messageDto) {
        if (applicationProperties.getMailEnable()) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(applicationProperties.getMailUsername());
            message.setTo(messageDto.getEmail());
            String subject = String.format(EMAIL_HEADER_TEMPLATE,
                messageDto.getContractorName(), messageDto.getTripNum()
            );
            String text = formatMessageWithUrl(MESSAGE_TEXT, messageDto, messageDto.getLKLink());
            if (text.isEmpty()) {
                log.warn("E-mail message text for {} is empty.", messageDto.getEmail());
                return;
            }
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
        try {
            String shortUrl = getShortUrl(messageDto.getLKLink());
            log.info("Short URL for LK is: {} .", shortUrl);
            return formatMessageWithUrl(MESSAGE_TEXT_SMS, messageDto, shortUrl);
        } catch (Exception e) {
            log.error("Failed to shorten link. So use long-link.", e);
            return formatMessageWithUrl(MESSAGE_TEXT_SMS, messageDto, messageDto.getLKLink());
        }
    }

    private String formatMessageWithUrl(String textTemplate, MessageDto message, String url) {
        return String.format(textTemplate,
            message.getContractorName(),
            message.getTripNum(),
            message.getAdvancePaymentSum(),
            url
        );
    }

    private String getShortUrl(String url) throws BadRequestException {
        if (StringUtils.isBlank(url)) {
            throw new IllegalArgumentException("Input URL is empty.");
        }
        String serviceUrl = applicationProperties.getCutLinkUrl();
        if (StringUtils.isBlank(serviceUrl)) {
            throw new IllegalArgumentException("Link-shortener service URL is empty.");
        }

        ResponseEntity<String> response = restTemplate.exchange(serviceUrl + url,
            HttpMethod.GET, null, String.class
        );

        if (response.getStatusCode() != HttpStatus.OK) {
            log.error("URL-shortener server returned bad response {}", response);
            throw new BadRequestException("URL-shortener error.");
        } else {
            return response.getBody();
        }
    }


    private SmsSendingException getSmsException(String s, HttpStatus status) {
        Error error = new Error();
        error.setErrorMessage(s);
        error.setErrorCode(Integer.toString(status.value()));
        error.setStatus(status.toString());
        return new SmsSendingException(status, error);
    }
}

