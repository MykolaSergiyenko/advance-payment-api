package online.oboz.trip.trip_carrier_advance_payment_api.service.messages;

import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.TripAdvanceRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringJUnit4ClassRunner.class)
public class NewNotificationServiceTest {
    @Autowired
    TripAdvanceRepository rep;

    @Test
    public void testMessenger() {
        ApplicationProperties prop = new ApplicationProperties();
        RestTemplate rest = new RestTemplate();
//        MessageSenderService

//        prop.setEmailEnable(true);
//        prop.setSmsEnable(true);
//        prop.setEmailScheduleEnable(true);
//        prop.setSmsScheduleEnable(true);
//        UrlShortenerService urlShortenerService = new UrlShortenerService(rest, prop);
//        MessageTextService text = new MessageTextService(prop, urlShortenerService, rep);
//        MessageSenderService serv = new MessageSenderService(prop, text, emailSender, restTemplate, messageTextService1, repository, emailSender1, smsSenderService);
//        serv.notificate(333, false);
//        serv.notificate(333, true);
//        prop.setEmailEnable(true);
//        prop.setSmsEnable(false);
//        prop.setEmailScheduleEnable(true);
//        prop.setSmsScheduleEnable(false);
//        serv = new MessageSenderService(prop, text, emailSender, restTemplate, messageTextService1, repository, emailSender1, smsSenderService);
//        serv.notificate(333, false);
//        serv.notificate(333, true);
//        prop.setEmailEnable(false);
//        prop.setSmsEnable(true);
//        prop.setEmailScheduleEnable(false);
//        prop.setSmsScheduleEnable(true);
//        serv = new MessageSenderService(prop, text, emailSender, restTemplate, messageTextService1, repository, emailSender1, smsSenderService);
//        serv.notificate(333, false);
//        serv.notificate(333, true);
//        prop.setEmailEnable(false);
//        prop.setSmsEnable(false);
//        prop.setEmailScheduleEnable(false);
//        prop.setSmsScheduleEnable(false);
//        serv = new MessageSenderService(prop, text, emailSender, restTemplate, messageTextService1, repository, emailSender1, smsSenderService);
//        serv.notificate(333, false);
//        serv.notificate(333, true);
    }
}
