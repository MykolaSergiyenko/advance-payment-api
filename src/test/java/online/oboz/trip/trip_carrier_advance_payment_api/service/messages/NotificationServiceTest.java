package online.oboz.trip.trip_carrier_advance_payment_api.service.messages;

import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.dicts.contacts.AdvanceContactsBook;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.Trip;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.Person;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.AdvanceContactsBookRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.AdvanceRepository;

import online.oboz.trip.trip_carrier_advance_payment_api.repository.TripRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.PersonRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.service.advance.AdvanceService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.contacts.ContactService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.persons.BasePersonService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.persons.PersonService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.rest.RestService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.common.format.MessageCreateService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.common.format.TextService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.trip.BaseTripService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.trip.TripService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.urleditor.UrlShortenerService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.email.EmailSender;
import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.email.EmailSenderService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.sms.SmsSender;
import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.sms.SmsSenderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static io.github.benas.randombeans.api.EnhancedRandom.random;

@RunWith(SpringJUnit4ClassRunner.class)
public class NotificationServiceTest {
    private JavaMailSender mailSender;
    private RestTemplate rest;
    private RestService restService;
    private ApplicationProperties props;
    private TextService messageTextService;
    private EmailSender emailSender;
    private SmsSender smsSender;
    private Notificator notificator;
    private AdvanceRepository advances;
    private TripRepository trips;
    private PersonRepository users;
    private AdvanceContactsBookRepository contacts;

    private AdvanceService advanceService;
    private BaseTripService tripService;
    private BasePersonService personService;
    private ContactService contactService;



    @BeforeEach
    void initTest() {
        System.out.println("--- init ---");

        props = new ApplicationProperties();
        restService = new RestService(props, rest);

        try {
            props.setLkUrl(new URL("https://oboz.online/carrier-advance/"));
            props.setCutLinkUrl(new URL("https://clck.ru/--?url="));
            props.setSmsCutLinks(true);
            props.setEmailCutLinks(true);
        } catch (Exception e){
            System.out.println("LK-link: "+props.getLkUrl());
        }
        UrlShortenerService cutter = new UrlShortenerService( props);
        messageTextService = new MessageCreateService(props, contactService, cutter);
        mailSender = mock(JavaMailSender.class);
        emailSender = new EmailSenderService(mailSender);
        smsSender = new SmsSenderService(props);
        notificator = new NotificationService(
            props,
            messageTextService,
            emailSender,
            smsSender);

        users = mock(PersonRepository.class);
        when(users.getOne(any())).thenReturn(random(Person.class));

        trips = mock(TripRepository.class);
        when(trips.getOne(any())).thenReturn(random(Trip.class));

        advances = mock(AdvanceRepository.class);
        when(advances.getOne(any())).thenReturn(random(Advance.class));

        contacts = mock(AdvanceContactsBookRepository.class);
        when(contacts.getOne(any())).thenReturn(random(AdvanceContactsBook.class));
    }

    @Test
    void testScheduledSms() {
        System.out.println("--- scheduled sms ---");
        props.setSmsScheduleEnable(true);
        props.setEmailScheduleEnabled(false);
        Advance x = advances.getOne(669l); //random new TripAdvance();
        System.out.println("Create random trip-advance: " + x.toString());

        Person user = users.getOne(55l);
        Trip randomTrip = trips.getOne(666l); //
        System.out.println("--- randomTrip contractor id: " + randomTrip.getContractorId());

        Advance x2 = new Advance(user);
        //TripAdvance x2 = new TripAdvance();
        //x2.setAdvanceFromTrip(randomTrip, user);
        advances.save(x2);
        System.out.println("--- advance contractor id: " + x2.getContact());
        System.out.println("Create Random Trip: " + randomTrip);
        System.out.println("Create trip-advance of random Trip: " + x2.toString());
        assertNotNull(x);
        assertNotNull(x2);
        notificator.scheduledNotify(x);

        props.setSmsMessageTemplate("Компания ОБОЗ+ предлагает аванс по заказу %s на сумму %.0f руб., для просмотра пройдите по ссылке \n %s");
        notificator.scheduledNotify(x);
        props.setSmsPhoneTemplate("7%s");
        props.setSmsCutLinks(true);
        notificator.scheduledNotify(x);
        try {
            props.setCutLinkUrl(new URL("https://clck.ru/--?url="));
            props.setSmsSenderUrl(new URL("http://sms-sender.r14.k.dev.oboz:30080/"));
            props.setLkUrl(new URL("https://oboz.online/carrier-advance/"));
            notificator.scheduledNotify(x);
        } catch (MalformedURLException e) {
            System.out.println("Error with LK-url: " + e.getMessage());
        }
    }

    @Test
    void testScheduledEmails() {
        System.out.println("--- scheduled emails ---");
        props.setEmailScheduleEnabled(true);
        props.setSmsScheduleEnable(false);
        Advance x = advances.getOne(668l);
        System.out.println("Create random trip-advance: " + x);
        assertNotNull(x);
        notificator.scheduledNotify(x);

        props.setEmailHeaderTemplate("Компания ObOz предлагает аванс по заказу %s");
        notificator.scheduledNotify(x);
        props.setEmailMessageTemplate("Компания ОБОЗ предлагает аванс\n по заказу %s на сумму %.0f руб., для просмотра пройдите по ссылке \n%s");
        notificator.scheduledNotify(x);
        try {
            URL lk = new URL("https://oboz.online/carrier-advance/");
            props.setLkUrl(lk);
            notificator.scheduledNotify(x);
        } catch (MalformedURLException e) {
            System.out.println("Error with LK-url: " + e.getMessage());
        }
        props.setMailUsername("test@test.com");
        notificator.scheduledNotify(x);
    }

    @Test
    void testSms() {
        System.out.println("--- sms ---");
        props.setEmailEnabled(false);
        props.setSmsEnable(true);
        Advance x = advances.getOne(667l);
        System.out.println("Create random trip-advance: " + x);
        assertNotNull(x);
        notificator.notify(x);
        // errors while sms-ing
        props.setSmsMessageTemplate("Компания ОБОЗ+ предлагает аванс по заказу %s на сумму %.0f руб., для просмотра пройдите по ссылке \n %s");
        notificator.notify(x);
        props.setSmsPhoneTemplate("7%s");
        props.setSmsCutLinks(true);
        notificator.notify(x);
        try {
            props.setCutLinkUrl(new URL("https://clck.ru/--?url="));
            props.setSmsSenderUrl(new URL("http://sms-sender.r14.k.dev.oboz:30080/"));
            props.setLkUrl(new URL("https://oboz.online/carrier-advance/"));
            notificator.notify(x);
        } catch (MalformedURLException e) {
            System.out.println("Error with LK-url: " + e.getMessage());
        }
    }

    @Test
    void testEmails() {
        System.out.println("--- emails ---");
        props.setEmailEnabled(true);
        props.setSmsEnable(false);
        Advance x = advances.getOne(666l);
        System.out.println("Create random trip-advance: " + x);
        assertNotNull(x);
        notificator.notify(x);
        // errors while emailing
        props.setEmailHeaderTemplate("Компания ObOz предлагает аванс по заказу %s");
        notificator.notify(x);
        props.setEmailMessageTemplate("Компания ОБОЗ предлагает аванс\n по заказу %s на сумму %.0f руб., для просмотра пройдите по ссылке \n%s");
        notificator.notify(x);
        try {
            URL lk = new URL("https://oboz.online/carrier-advance/");
            props.setLkUrl(lk);
            notificator.notify(x);
        } catch (MalformedURLException e) {
            System.out.println("Error with LK-url: " + e.getMessage());
        }
        props.setMailUsername("test@test.com");
        notificator.notify(x);
    }
}
