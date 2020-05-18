package online.oboz.trip.trip_carrier_advance_payment_api.service.messages;

import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.common.format.urlshorter.UrlShortenerService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import java.net.URL;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Тесты для "сокращателя" {@link UrlShortenerService}
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class UrlShortenerServiceTest {
    @Value("${services.notifications.sms.cut-link-url:https://clck.ru/--?url=}")
    URL cutLinkUrl;

    @Value("https://www.tutorialspoint.com/spring_boot/spring_boot_application_properties.html")
    String testUrl1;

    @Value("http://www.ya.ru")
    URL testUrl2;

    @Value("http://www.oboz.com")
    URL testUrl3;

    @Value("http://www.breeed.com")
    URL testUrl4;

    @Value("ftp://bred.ru")
    URL testUrl5;

    @Value("ftp://bred.ua/spring_boot/spring_boot_application_properties.html")
    URL testUrl6;

    @Value("xxxxx")
    String testUrl7;

    @Value("ftp://xxxxx")
    String testUrl8;

    @Value("http://президент.рф")
    String testUrl9;

    @Value("xxxxx")
    String testUrl10;

    @Value("http://президент.рф")
    String testUrl11;

    @Value("")
    String testUrl12;


    public UrlShortenerServiceTest() {

    }


    @Test
    public void testUrlCutter() {
        RestTemplate rest = new RestTemplate();
        System.out.println("***  Set cutLinkUrl to " + cutLinkUrl);
        ApplicationProperties prop = new ApplicationProperties();
        prop.setCutLinkUrl(cutLinkUrl);

        UrlShortenerService urlShortenerService = new UrlShortenerService(rest, prop);

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

        // одинаковые ссылки
        assertEquals(cuttedLink10, cuttedLink7);

        // одинаковые в кириллице
        assertEquals(cuttedLink11, cuttedLink9);

        // "постоянная ссылка" на яндекс
        assertEquals(cuttedLink2, "https://clck.ru/HR");

        // переданы пустые значения
        assertEquals(cuttedLink12, "");

        // преобразования дали результат
        assertNotEquals(cuttedLink5, "");
        assertNotNull(cuttedLink1);
        assertNotNull(cuttedLink2);
        assertNotNull(cuttedLink3);
        assertNotNull(cuttedLink4);
    }

}





