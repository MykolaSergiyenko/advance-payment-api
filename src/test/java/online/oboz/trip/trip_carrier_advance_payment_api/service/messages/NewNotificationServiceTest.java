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
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

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
    @Value("${services.notifications.sms.cut-link-url:https://clck.ru/--?url=}")
    URL cutLinkUrl;

    @Autowired
    private TripAdvanceRepository tripAdvanceRepository;

    private JavaMailSender mailSender;
    private RestTemplate rest;

    @Autowired
    private ApplicationProperties prop;
    private TextService messageTextService;
    private  EmailSender emailSender;
    private  SmsSender smsSender;


    @BeforeEach
    void initTest(){
        mailSender = mock(JavaMailSender.class);
        rest = new RestTemplate();
        prop = new ApplicationProperties();
        prop.setEmailEnabled(true);
        prop.setSmsEnable(false);
        prop.setCutLinkUrl(cutLinkUrl);



        UrlShortenerService urlShortenerService = new UrlShortenerService(rest, prop);

        messageTextService = new MessageCreateService(prop, urlShortenerService);
        emailSender = new EmailSenderService(mailSender);
        smsSender = new SmsSenderService(rest, prop);
    }



    @Test
    void testEmails(){
        Notificator notificator = new NewNotificationService(
            prop,
            messageTextService,
            emailSender,
            smsSender
        );

        TripAdvanceRepository advances = mock(TripAdvanceRepository.class);
        when(advances.getOne(any())).thenReturn(random(TripAdvance.class));

        TripAdvance x = advances.getOne(666l); //random new TripAdvance();
        System.out.println("Create random trip-advance: "+x);
        assertNotNull(x);
        // error while emailing
        notificator.notificate(x);

        prop.setEmailHeaderTemplate("Компания ObOz предлагает аванс по заказу %s");
        notificator.notificate(x);

        prop.setEmailMessageTemplate("Компания ОБОЗ предлагает аванс\n по заказу %s на сумму %.0f руб., для просмотра пройдите по ссылке \n%s");
        notificator.notificate(x);
        try {
            URL lk = new URL("https://oboz.online/carrier-advance/");
            prop.setLkUrl(lk);
            notificator.notificate(x);
        } catch (MalformedURLException e ){
            System.out.println("Error with LK-url: "+e.getMessage());
        }
    }
}
