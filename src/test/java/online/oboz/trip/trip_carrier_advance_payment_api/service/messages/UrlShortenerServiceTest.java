package online.oboz.trip.trip_carrier_advance_payment_api.service.messages;

import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.service.urleditor.UrlShortenerService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.net.URL;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Тесты для "сокращателя" {@link UrlShortenerService}
 */
@RunWith(SpringRunner.class)
public class UrlShortenerServiceTest {

    @Test
    public void testUrlByCutter() {
        try {
            //test cases
            String testUrl1 = "https://www.tutorialspoint.com/spring_boot/spring_boot_application_properties.html";
            String testUrl2 = "http://www.ya.ru";
            String testUrl3 = "http://www.oboz.com";
            String testUrl4 = "http://www.breeed.com";
            String testUrl5 = "ftp://bred.ru";
            String testUrl6 = "ftp://bred.ua/spring_boot/spring_boot_application_properties.html";
            String testUrl7 = "xxxxx";
            String testUrl8 = "ftp://xxxxx";
            String testUrl9 = "http://президент.рф";
            String testUrl10 = "xxxxx";
            String testUrl11 = "http://президент.рф";
            String testUrl12 = "";

            RestTemplate rest = new RestTemplate();

            URL cutLinkUrl = new URL("https://clck.ru/--?url=");
            System.out.println("Set cutLinkUrl to " + cutLinkUrl);
            ApplicationProperties prop = new ApplicationProperties();
            prop.setCutLinkUrl(cutLinkUrl);
            UrlShortenerService urlShortenerService = new UrlShortenerService(prop);


            // edit url string -> string
            String cuttedLink1 = urlShortenerService.editUrl(testUrl1);
            System.out.println("inputUrl1 " + testUrl1);
            System.out.println("outputUrl1 " + cuttedLink1);

            String cuttedLink2 = urlShortenerService.editUrl(testUrl2);
            System.out.println("inputUrl2 " + testUrl2);
            System.out.println("outputUrl2 " + cuttedLink2);

            String cuttedLink3 = urlShortenerService.editUrl(testUrl3);
            System.out.println("inputUrl3 " + testUrl3);
            System.out.println("outputUrl3 " + cuttedLink3);

            String cuttedLink4 = urlShortenerService.editUrl(testUrl4);
            System.out.println("inputUrl4 " + testUrl4);
            System.out.println("outputUrl4 " + cuttedLink4);

            String cuttedLink5 = urlShortenerService.editUrl(testUrl5);
            System.out.println("inputUrl5 " + testUrl5);
            System.out.println("outputUrl5 " + cuttedLink5);

            String cuttedLink6 = urlShortenerService.editUrl(testUrl6);
            System.out.println("inputUrl6 " + testUrl6);
            System.out.println("outputUrl6 " + cuttedLink6);

            String cuttedLink7 = urlShortenerService.editUrl(testUrl7);
            System.out.println("inputUrl7 " + testUrl7);
            System.out.println("outputUrl7 " + cuttedLink7);

            String cuttedLink8 = urlShortenerService.editUrl(testUrl8);
            System.out.println("inputUrl8 " + testUrl8);
            System.out.println("outputUrl8 " + cuttedLink8);

            String cuttedLink9 = urlShortenerService.editUrl(testUrl9);
            System.out.println("inputUrl9 " + testUrl9);
            System.out.println("outputUrl9 " + cuttedLink9);

            String cuttedLink10 = urlShortenerService.editUrl(testUrl10);
            System.out.println("inputUrl10 " + testUrl10);
            System.out.println("outputUrl10 " + cuttedLink10);

            String cuttedLink11 = urlShortenerService.editUrl(testUrl11);
            System.out.println("inputUrl11 " + testUrl11);
            System.out.println("outputUrl11 " + cuttedLink11);

            String cuttedLink12 = urlShortenerService.editUrl(testUrl12);
            System.out.println("inputUrl12 " + testUrl12);
            System.out.println("outputUrl12 " + cuttedLink12);


            assertEquals(cuttedLink10, cuttedLink7);
            assertEquals(cuttedLink11, cuttedLink9);
            assertEquals(cuttedLink2, "https://clck.ru/HR");

            assertNotEquals(cuttedLink5, "");
            assertEquals(cuttedLink12, "");

            assertNotNull(cuttedLink1);
            assertNotNull(cuttedLink2);
            assertNotNull(cuttedLink3);
            assertNotNull(cuttedLink4);


        } catch (MalformedURLException e) {
            System.out.println("Fail to set cut-link URL.");

        }
    }


}





