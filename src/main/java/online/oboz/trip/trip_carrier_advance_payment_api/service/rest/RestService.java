package online.oboz.trip.trip_carrier_advance_payment_api.service.rest;

import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.exception.AuthException;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.ResponseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URL;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.MOVED_PERMANENTLY;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

@Service
public class RestService implements RestTemplateService {
    // Use RestTemplate here

    private static final Logger log = LoggerFactory.getLogger(RestService.class);
    private final ApplicationProperties applicationProperties;
    private final RestTemplate restTemplate;

    @Autowired
    public RestService(
        ApplicationProperties applicationProperties,
        RestTemplate restTemplate
    ) {
        this.applicationProperties = applicationProperties;
        this.restTemplate = restTemplate;
    }

    private String ACCESS_TOKEN = null;

    @EventListener(ContextRefreshedEvent.class)
    public void contextRefreshedEvent() {
        String user = applicationProperties.getUsername();
        String password = applicationProperties.getPassword();
        ResponseToken responseToken = requestToken(user, password);
        ACCESS_TOKEN = responseToken.getAccessToken();
        log.info("Success request token. for user  " + applicationProperties.getUsername() + " " + ACCESS_TOKEN);
    }

    public ResponseToken requestToken(String userName, String password) {
        try {
            String url = applicationProperties.getTokenAuthUrl() + applicationProperties.getTokenUrlPostfix();
            String authBody = String.format(applicationProperties.getTokenBody(), userName, password);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf(APPLICATION_FORM_URLENCODED_VALUE));
            HttpEntity httpEntity = new HttpEntity(authBody, headers);

            ResponseToken responseToken = restTemplate.exchange(url, POST, httpEntity, ResponseToken.class).getBody();
            responseToken.setTokenType("Bearer");
            return responseToken;
        } catch (Exception e) {
            throw new AuthException("Failed request token from keycloak. ", e);
        }
    }

    public ResponseEntity<Resource> authGetRequestResource(String url) {
        log.info("Got resource-request to: '{}'", url);
        HttpHeaders headers = new HttpHeaders();
        headers.add(AUTHORIZATION, "Bearer " + ACCESS_TOKEN);
        return getRequestResource(url, headers);
    }

    public ResponseEntity<Resource> getRequestResource(String url, HttpHeaders headers) {
        log.info("Got resource-request to: '{}'", url);
        try {
            HttpEntity<String> request = new HttpEntity<>(headers);
            ResponseEntity<Resource> response = restTemplate.exchange(url, GET, request, Resource.class);
            if (response.getStatusCode().value() == 200 || response.getStatusCode() == MOVED_PERMANENTLY) {
                return response;
            }
        } catch (Exception e) {
            log.error("Failed resource-request to url: '{}' . Error: {} .", url, e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return null;
    }

    public <T> ResponseEntity<String> authPostRequest(String url, HttpHeaders headers, T body) {
        headers.add(AUTHORIZATION, "Bearer " + ACCESS_TOKEN);
        HttpEntity<T> request = new HttpEntity<>(body, headers);
        return restTemplate.exchange(url, POST, request, String.class);
    }

    public ResponseEntity<String> authGetRequest(String url, HttpHeaders headers) {
        headers.add(AUTHORIZATION, "Bearer " + ACCESS_TOKEN);
        HttpEntity<String> request = new HttpEntity<>(headers);
        return restTemplate.exchange(url, GET, request, String.class);
    }

    public ResponseEntity<String> getRequest(String url) {
        return restTemplate.exchange(url, GET, null, String.class);
    }

    public ResponseEntity<String> postForEntity(URL url, Object container) {
        String strUrl = url.toString();
        return restTemplate.postForEntity(strUrl, container, String.class);
    }

}
