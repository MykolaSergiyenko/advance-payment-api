package online.oboz.trip.trip_carrier_advance_payment_api.service.messages;

import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.TripAdvance;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.TripAdvanceRepository;

import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.common.format.MessageCreateService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.common.format.TextService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.common.format.urlshorter.UrlShortenerService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.email.EmailSender;
import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.email.EmailSenderService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.sms.SmsSender;
import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.sms.SmsSenderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static io.github.benas.randombeans.api.EnhancedRandom.random;

@RunWith(SpringRunner.class)
public class NewNotificationServiceTest {
    private JavaMailSender mailSender;
    private RestTemplate rest;
    private ApplicationProperties props;
    private TextService messageTextService;
    private EmailSender emailSender;
    private SmsSender smsSender;
    private Notificator notificator;
    private TripAdvanceRepository advances;

    @BeforeEach
    void initTest() {
        System.out.println("--- init ---");
        rest = new RestTemplate();
        props = new ApplicationProperties();

        UrlShortenerService urlShortenerService = new UrlShortenerService(rest, props);
        messageTextService = new MessageCreateService(props, urlShortenerService);
        mailSender = mock(JavaMailSender.class);
        emailSender = new EmailSenderService(mailSender);
        smsSender = new SmsSenderService(rest, props);
        notificator = new NewNotificationService(
            props,
            messageTextService,
            emailSender,
            smsSender
        );

        advances = mock(TripAdvanceRepository.class);
        when(advances.getOne(any())).thenReturn(random(TripAdvance.class));
    }

    @Test
    void testScheduledSms() {
        System.out.println("--- scheduled sms ---");
        props.setSmsScheduleEnable(true);
        props.setEmailScheduleEnabled(false);
        TripAdvance x = advances.getOne(669l); //random new TripAdvance();
        System.out.println("Create random trip-advance: " + x);
        assertNotNull(x);
        notificator.scheduledNotificate(x);

        props.setSmsMessageTemplate("Компания ОБОЗ+ предлагает аванс по заказу %s на сумму %.0f руб., для просмотра пройдите по ссылке \n %s");
        notificator.scheduledNotificate(x);
        props.setSmsPhoneTemplate("7%s");
        props.setSmsCutLinks(true);
        notificator.scheduledNotificate(x);
        try {
            props.setCutLinkUrl(new URL("https://clck.ru/--?url="));
            props.setSmsSenderUrl(new URL("http://sms-sender.r14.k.dev.oboz:30080/"));
            props.setLkUrl(new URL("https://oboz.online/carrier-advance/"));
            notificator.scheduledNotificate(x);
        } catch (MalformedURLException e) {
            System.out.println("Error with LK-url: " + e.getMessage());
        }
    }

    @Test
    void testScheduledEmails() {
        System.out.println("--- scheduled emails ---");
        props.setEmailScheduleEnabled(true);
        props.setSmsScheduleEnable(false);
        TripAdvance x = advances.getOne(668l);
        System.out.println("Create random trip-advance: " + x);
        assertNotNull(x);
        notificator.scheduledNotificate(x);

        props.setEmailHeaderTemplate("Компания ObOz предлагает аванс по заказу %s");
        notificator.scheduledNotificate(x);
        props.setEmailMessageTemplate("Компания ОБОЗ предлагает аванс\n по заказу %s на сумму %.0f руб., для просмотра пройдите по ссылке \n%s");
        notificator.scheduledNotificate(x);
        try {
            URL lk = new URL("https://oboz.online/carrier-advance/");
            props.setLkUrl(lk);
            notificator.scheduledNotificate(x);
        } catch (MalformedURLException e) {
            System.out.println("Error with LK-url: " + e.getMessage());
        }
        props.setMailUsername("test@test.com");
        notificator.scheduledNotificate(x);
    }

    @Test
    void testSms() {
        System.out.println("--- sms ---");
        props.setEmailEnabled(false);
        props.setSmsEnable(true);
        TripAdvance x = advances.getOne(667l);
        System.out.println("Create random trip-advance: " + x);
        assertNotNull(x);
        notificator.notificate(x);
        // errors while sms-ing
        props.setSmsMessageTemplate("Компания ОБОЗ+ предлагает аванс по заказу %s на сумму %.0f руб., для просмотра пройдите по ссылке \n %s");
        notificator.notificate(x);
        props.setSmsPhoneTemplate("7%s");
        props.setSmsCutLinks(true);
        notificator.notificate(x);
        try {
            props.setCutLinkUrl(new URL("https://clck.ru/--?url="));
            props.setSmsSenderUrl(new URL("http://sms-sender.r14.k.dev.oboz:30080/"));
            props.setLkUrl(new URL("https://oboz.online/carrier-advance/"));
            notificator.notificate(x);
        } catch (MalformedURLException e) {
            System.out.println("Error with LK-url: " + e.getMessage());
        }
    }

    @Test
    void testEmails() {
        System.out.println("--- emails ---");
        props.setEmailEnabled(true);
        props.setSmsEnable(false);
        TripAdvance x = advances.getOne(666l);
        System.out.println("Create random trip-advance: " + x);
        assertNotNull(x);
        notificator.notificate(x);
        // errors while emailing
        props.setEmailHeaderTemplate("Компания ObOz предлагает аванс по заказу %s");
        notificator.notificate(x);
        props.setEmailMessageTemplate("Компания ОБОЗ предлагает аванс\n по заказу %s на сумму %.0f руб., для просмотра пройдите по ссылке \n%s");
        notificator.notificate(x);
        try {
            URL lk = new URL("https://oboz.online/carrier-advance/");
            props.setLkUrl(lk);
            notificator.notificate(x);
        } catch (MalformedURLException e) {
            System.out.println("Error with LK-url: " + e.getMessage());
        }
        props.setMailUsername("test@test.com");
        notificator.notificate(x);
    }
}
