package online.oboz.trip.trip_carrier_advance_payment_api.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

@Service
@EnableScheduling
public class NotificationService {
//    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
//    private static final String IN_SMS_COMPANY_NAME = "ОБОЗ";
//    private static final String RUSSIAN_COUNTRY_CODE = "7";
//    private static final String SEND_SMS_METHOD_PATH = "/v1/send-sms";
//    private static final String EMAIL_HEADER_TEMPLATE = "Компания %s  предлагает аванс по заказу %s ";
//    private static final String MESSAGE_TEXT = "Компания %s  предлагает аванс по заказу\n" +
//        "%s на сумму %.0f руб., для просмотра пройдите по ссылке \n%s";
//
//    private static final String MESSAGE_TEXT_SMS = "Компания %s  предлагает аванс по заказу " +
//        "%s на сумму %.0f руб., для просмотра пройдите по ссылке %s";
//
    private final JavaMailSender emailSender;

    public NotificationService(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }
//    private final RestTemplate restTemplate;
//
//    private final ApplicationProperties applicationProperties;
//
//    private final AdvanceRequestRepository advanceRequestRepository;
//    private final AdvanceContactRepository advanceContactRepository;
//    private final ContractorRepository contractorRepository;
//    private final TripRepository tripRepository;

//    public NotificationService(JavaMailSender emailSender, RestTemplate restTemplate, ApplicationProperties applicationProperties, AdvanceRequestRepository advanceRequestRepository, AdvanceContactRepository advanceContactRepository, ContractorRepository contractorRepository, TripRepository tripRepository) {
//        this.emailSender = emailSender;
//        this.restTemplate = restTemplate;
//        this.applicationProperties = applicationProperties;
//        this.advanceRequestRepository = advanceRequestRepository;
//        this.advanceContactRepository = advanceContactRepository;
//        this.contractorRepository = contractorRepository;
//        this.tripRepository = tripRepository;
//    }


//    @Scheduled(cron = "${cron.sms-notify: 0 0/1 * * * *}")
//    public void scheduledSms() {
//        // 1. Filter advanceRequests only one time
//        // 2. Dependent on tripAdvance and appProps only
//
//        // 3. 'Motor', ''
//        List<TripRequestAdvancePayment> advances = advanceRequestRepository.findForNotification();
//        advances.forEach(advance -> {
//                log.info("Found advance-request with unread e-mail - {}.", advance.getId());
//                try {
//                    String errMessage;
//                    Trip trip = tripRepository.getMotorTrip(advance.getTripId()).orElse(null);
//                    if (null == trip) {
//                        errMessage = "Trip not found, id = " + advance.getTripId();
//                        throw getSmsException(errMessage, HttpStatus.NOT_FOUND);
//                    }
//
//                    // get Trip
//                    // get Contact
//                    // get Advance
//
//                    // create MessageDTO
//
//                    //send scheduled Message;
//
//                    // set advance sms-sent
//                    // or other message - MessaDTO must contain Type
//
//                    // get of Advance in Message dto
//                    ContractorAdvancePaymentContact contact = null;
//
////                    ContractorAdvancePaymentContact contact = advanceContactRepository.find(trip.getContractorId()).orElse(null);
////                    if (null == contact) {
////                        errMessage = "Contact not found for trip " + trip.getNum();
////                        throw getSmsException(errMessage, HttpStatus.NOT_FOUND);
////                    }
//                    MessageDto messageDto = new MessageDto(applicationProperties, advance,
//                       contact);
//                   sendSms(messageDto);
//
//                    setSmsSent(advance);
//                } catch (SmsSendingException e) {
//
//                    log.error("Sms-sending error: {}", e.getErrors());
//                }
//            }
//        );
//    }
//
//    // convert applicationProperties into Message Dto
//    //
//    private void sendSms(MessageDto messageDto) throws SmsSendingException {
//        log.info("Send sms " + messageDto);
//        URL smsSenderUrl = applicationProperties.getSmsSenderUrl();
//        messageDto.setContractorName(IN_SMS_COMPANY_NAME);
//        String text = getSmsText(messageDto);
//        String phone = getPhoneNumber(messageDto);
//        String tripNum = getTripNumber(messageDto);
//        if (StringUtils.isBlank(text)) { //phone
//            throw getSmsException("SMS text is empty.", HttpStatus.NO_CONTENT);
//        }
//        SendSmsRequest smsRequest = new SendSmsRequest(
//            text,
//            phone,
//            tripNum
//        );
//        //sendSmsRequest(smsSenderUrl.getSmsSenderUrl(), smsRequest);
//    }
//
//
//    private void sendSmsRequest(String url, SendSmsRequest sms) throws SmsSendingException {
//        String errMessage = "SMS sending error for phone-number " + sms.getPhone();
//        try {
//            ResponseEntity<String> response = restTemplate.postForEntity(
//                url + SEND_SMS_METHOD_PATH,
//                new SendSmsRequest(sms.getText(), sms.getPhone(), sms.getTripNum()),
//                String.class
//            );
//            if (response.getStatusCode() != HttpStatus.OK) {
//                throw getSmsException(errMessage, response.getStatusCode());
//            }
//            log.info("Success send notification sms to " + sms.getPhone());
//        } catch (HttpServerErrorException e) {
//            throw getSmsException(errMessage + ". Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//

    // Call-back for TripAdvance
//    private void setSmsSent(TripRequestAdvancePayment advance) {
//        advance.setIsSmsSent(true);
//        advanceRequestRepository.save(advance);
//        log.info("Set sms-sent for advance-request " + advance.getId());
//    }
//
//
//    public void sendEmail(MessageDto messageDto) {
//        if (applicationProperties.getEmailEnable()) {
//            SimpleMailMessage message = new SimpleMailMessage();
//            message.setFrom(applicationProperties.getMailUsername());
//            message.setTo(messageDto.getEmail());
//            String subject = String.format(EMAIL_HEADER_TEMPLATE,
//                messageDto.getContractorName(), messageDto.getTripNum()
//            );
//            //String text = formatMessageWithUrl(MESSAGE_TEXT, messageDto, messageDto.getLKLink());
//            String text = formatMessageWithUrl(MESSAGE_TEXT, messageDto, "хуй");
//            if (text.isEmpty()) {
//                log.warn("E-mail message text for {} is empty.", messageDto.getEmail());
//                return;
//            }
//            message.setText(text);
//            message.setSubject(subject);
//            try {
//                emailSender.send(message);
//                log.info("Sending message to email: {} with text: {} send success", message.getTo(), text);
//            } catch (Exception ex) {
//                log.error("Error while sending message. to - {} subject - {}", message.getTo(), subject, ex);
//                throw ex;
//            }
//        } else {
//            log.info("Sending message properties disable, email message  to - {}", messageDto.getEmail());
//        }
//    }
//
//    private String getSmsText(MessageDto messageDto) {
//        try {
//            return "";
//            //messageDto.getFormatedMessage();
//            //Edit URL (cut in sms)
//            //TODO: Incapsulate in  MessageDto via Type
//            // try{
//            // to lkLink(UUID advance) <-- return URL
//            // to shortLink() // if SMS  <-- external-api, return URL
//            // to xyu-message // if both <-- "internal format", return Message?
//
//            //String shortUrl = getShortUrl(messageDto.getLKLink());
//            //log.info("Short URL for LK is: {} .", shortUrl);
//            //Format text
//            // return getFormatText(messageDto, shortUrl)
//            //return formatMessageWithUrl(MESSAGE_TEXT_SMS, messageDto, "shortUrl");
//        } catch (Exception e) {
//            log.error("Failed to shorten link. So use long-link.", e);
//            return formatMessageWithUrl(MESSAGE_TEXT_SMS, messageDto, messageDto.getLKLink());
//        }
//    }
//
//    private String getPhoneNumber(MessageDto messageDto) {
//        //TODO
//        // use formatNumberHere
//        // from applicationProperties....
//        // use template nor code
//        //String phone = RUSSIAN_COUNTRY_CODE + messageDto.getPhone();
//        return messageDto.getPhone();
//    }
//
//    private String getTripNumber(MessageDto messageDto){
//        // TODO:
//        // fromat tripNumber Here
//        // use #, № maybe
//        String number = messageDto.getTripNum().trim();
//        return number;
//    }
//
//
//    String getFormatText(String type, MessageDto message, ApplicationProperties applicationProperties){
//
//        //applicationProperties....
//
//
////        if (type.equals("SMS"){
////            String smsTemplate = "";// from app.props;
////
////            return String.format(smsTemplate,
////                //contractor - is contact.fullName?
////                //or message-sender-contact
////                //
////                message.getContractorName(),
////                //advance.num ? not tripNum?
////                message.getTripNum(), //advance.trip.tripnum
////                message.getAdvancePaymentSum(), // advance.sum cost with vat?
////                message.getLKLink() //URL? = appProps.lk-link+advance.uuid. short?
////            );
////
////
////        }
//        return "";
//    }
//
//
//
//
//
//    private String formatMessageWithUrl(String textTemplate, MessageDto message, String url) {
//        return String.format(textTemplate,
//            message.getContractorName(),
//            message.getTripNum(),
//            message.getAdvancePaymentSum(),
//            url
//        );
//    }
//
//    private String getShortUrl(String urlForEdit) throws BadRequestException {
//        if (StringUtils.isBlank(urlForEdit)) {
//            throw new IllegalArgumentException("Input URL is empty.");
//        }
//        URL serviceUrl = applicationProperties.getCutLinkUrl();
//        if (StringUtils.isBlank(serviceUrl.toString())) {
//            throw new IllegalArgumentException("Link-shortener service URL is empty.");
//        }
//
//        ResponseEntity<String> response = restTemplate.exchange(serviceUrl + urlForEdit,
//            HttpMethod.GET, null, String.class
//        );
//
//        if (response.getStatusCode() != HttpStatus.OK) {
//            log.error("URL-shortener server returned bad response {}", response);
//            throw new BadRequestException("URL-shortener error.");
//        } else {
//            return response.getBody();
//        }
//    }
//
//
//    private SmsSendingException getSmsException(String s, HttpStatus status) {
//        Error error = new Error();
//        error.setErrorMessage(s);
//        error.setErrorCode(Integer.toString(status.value()));
//        error.setStatus(status.toString());
//        return new SmsSendingException(status, error);
//    }
}

