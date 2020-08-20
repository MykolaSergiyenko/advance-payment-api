package online.oboz.trip.trip_carrier_advance_payment_api.service.urleditor;

import io.undertow.util.BadRequestException;
import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.edit_message.MessageCreateService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.sandbox.TestApi;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.Error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.BAD_REQUEST;


/**
 * <b>Сервис для "обрезки" ссылок</b>
 * <p>
 * Использует API сервиса 'Кликер.ру' ( https://clck.ru/ ) через RestService.
 * <p>
 * <p>
 * Используется в {@link MessageCreateService} при формировании текста уведомлений.
 * <p>
 * Тестовый ендпоинт - {@link TestApi#cutUrl(String)}.
 * <p>
 * Тесты - online.oboz.trip.trip_carrier_advance_payment_api.service.messages.UrlShortenerServiceTest
 * <p>
 *
 * @author s‡udent
 * @see UrlService
 */
@Service
public class UrlShortenerService implements UrlService {
    private static final Logger log = LoggerFactory.getLogger(UrlShortenerService.class);

    private final URL shortenerURL;
    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    public UrlShortenerService(ApplicationProperties properties) {
        this.shortenerURL = properties.getCutLinkUrl();
    }

    public String editUrl(String url) {
        try {
            log.info("Входная ссылка: {}.", url);
            return cutUrl(url);
        } catch (UrlCutterException e) {
            log.info("Ошибка редактирования ссылки: {}. Входная ссылка: {}.", e.getErrors(), url);
            return url;
        }
    }

    public String editUrl(URL url) {
        try {
            return cutUrl(url);
        } catch (UrlCutterException e) {
            log.info("Ошибка сокращения ссылки: {}. Входная ссылка: {}.", e.getErrors(), url.toString());
            return url.toString();
        }
    }

    private String cutUrl(URL url) throws UrlCutterException {
        try {
            return getShortUrl(url);
        } catch (BadRequestException e) {
            //log.info("Ошибка запроса   " + e.getMessage());
            throw getCutterException("Ошибка запроса к URL-редактору: " + e.getMessage(), BAD_REQUEST);
        }
    }

    private String cutUrl(String stringUrl) throws UrlCutterException {
        try {
            return cutUrl(new URL(URLDecoder.decode(stringUrl, "UTF-8")));
        } catch (MalformedURLException | UnsupportedEncodingException e) {
            log.error("Битая ссылка: {}", e.getMessage());
            throw getCutterException("Ошибка преобразования URL: " + e.getMessage(), BAD_REQUEST);
        }
    }


    private String getShortUrl(URL url) throws BadRequestException {
        if (null == shortenerURL) {
            throw new BadRequestException("Не указан URL сервиса сокращения ссылок.");
        }
        ResponseEntity<String> response = restTemplate.exchange(shortenerURL + url.toString(), GET, null, String.class);
        if (response.getStatusCode() != OK) {
            log.error("[Сокращение ссылок]: Сервер вернул ошибку {}", response);
            throw new BadRequestException("[Сокращение ссылок]: Ошибка сервера.");
        }
        log.info("Короткая ссылка: " + response.getBody());

        return response.getBody();
    }


    private UrlCutterException getCutterException(String s, HttpStatus status) {
        Error error = new Error();
        error.setErrorMessage(s);
        error.setErrorCode(Integer.toString(status.value()));
        error.setStatus(status.toString());
        return new UrlCutterException(status, error);
    }

}
