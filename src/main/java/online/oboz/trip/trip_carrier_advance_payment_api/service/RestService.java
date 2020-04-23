package online.oboz.trip.trip_carrier_advance_payment_api.service;

import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.exception.AuthException;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.ResponseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RestService {
    private static final Logger log = LoggerFactory.getLogger(RestService.class);
    private final ApplicationProperties applicationProperties;
    private final RestTemplate restTemplate;

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
        log.info("Success request token. " + ACCESS_TOKEN);
    }

    public ResponseToken requestToken(String userName, String password) {
        try {
            String authBody = String.format(
                "grant_type=password&client_id=elp&username=%s&password=%s",
                userName,
                password
            );
            ResponseEntity<ResponseToken> response = restTemplate.exchange(
                applicationProperties.getTokenAuthUrl() + "/token",
                HttpMethod.POST,
                createHttpEntityToAuth(authBody),
                ResponseToken.class
            );
            ResponseToken responseToken = response.getBody();
            responseToken.setTokenType("Bearer");
            return responseToken;
        } catch (Exception e) {
            throw new AuthException("Failed request token from keycloak. ", e);
        }
    }

    public ResponseEntity<Resource> authRequestResource(String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + ACCESS_TOKEN);
        return requestResource(url, headers);
    }

    public ResponseEntity<Resource> requestResource(String url, HttpHeaders headers) {
        try {
            HttpEntity<String> request = new HttpEntity<>(headers);
            ResponseEntity<Resource> response = restTemplate.exchange(url, HttpMethod.GET, request, Resource.class);
            if (response.getStatusCode().value() == 200 || response.getStatusCode() == HttpStatus.MOVED_PERMANENTLY) {
                return response;
            }
        } catch (Exception e) {
            log.error("Failed request to " + url, e);
        }
        return null;
    }

    public <T> ResponseEntity<String> executePostAuthRequest(
        String url,
        HttpHeaders headers,
        T body
    ) {
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + ACCESS_TOKEN);
        HttpEntity<T> request = new HttpEntity<>(body, headers);
        return restTemplate.exchange(url, HttpMethod.POST, request, String.class);
    }

    public ResponseEntity<String> executeGetAuthRequest(
        String url,
        HttpHeaders headers
    ) {
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + ACCESS_TOKEN);
        HttpEntity<String> request = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, request, String.class);
    }

    private HttpEntity createHttpEntityToAuth(String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(MediaType.APPLICATION_FORM_URLENCODED_VALUE));
        return new HttpEntity(body, headers);
    }
}
