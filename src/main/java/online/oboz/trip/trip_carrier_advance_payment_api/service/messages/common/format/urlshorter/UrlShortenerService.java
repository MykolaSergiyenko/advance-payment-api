package online.oboz.trip.trip_carrier_advance_payment_api.service.messages.common.format.urlshorter;

import io.undertow.util.BadRequestException;
import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.Error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;


@Service
public class UrlShortenerService implements UrlService {
    private static final Logger log = LoggerFactory.getLogger(UrlShortenerService.class);

    private ApplicationProperties properties;
    private RestTemplate template;

    public UrlShortenerService() {
    }


    @Autowired
    public UrlShortenerService(RestTemplate template, ApplicationProperties properties) {
        this.template=template;
        this.properties=properties;
    }

    public String editUrl(String url) {
        try{
            log.info("Input url is "+ url);
            return cutUrl(url);
        } catch (UrlCutterException e){
            log.info("UrlCutter error: "+ e.getErrors());
            return url;
        }
    }

    public String editUrl(URL url) {
        try{
            log.info("Input url is "+ url.toString());
            return cutUrl(url);
        } catch (UrlCutterException e){
            log.info("UrlCutter error: "+ e.getErrors());
            return url.toString();
        }
    }

    private String cutUrl(URL url) throws UrlCutterException {
        try {
            return getShortUrl(url);
        } catch (BadRequestException e){
            log.info("BadRequestException   "+ e.getMessage());
            throw getCutterException("Ошибка конвертации урла "+ e.getMessage()
                , HttpStatus.BAD_REQUEST);
        }
    }

    private String cutUrl(String stringUrl) throws UrlCutterException{
        try {
            return cutUrl(new URL(URLDecoder.decode(stringUrl, "UTF-8")));
        } catch (MalformedURLException | UnsupportedEncodingException e){
            log.info("MalformedURLException   "+ e.getMessage());
            throw getCutterException("URL convertation error: "+ e.getMessage()
                , HttpStatus.BAD_REQUEST);
        }
    }


    private String getShortUrl(URL url) throws BadRequestException{
        if (null == properties.getCutLinkUrl()){
            throw new BadRequestException("URL-shortener service link empty.");
        }

        ResponseEntity<String> response = template.exchange(properties.getCutLinkUrl() + url.toString(),
            HttpMethod.GET, null, String.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            log.error("URL-shortener server returned bad response {}", response);
            throw new BadRequestException("URL-shortener response error.");
        }

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
