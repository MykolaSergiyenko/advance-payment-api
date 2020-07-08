package online.oboz.trip.trip_carrier_advance_payment_api.service.rest;

import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.ResponseToken;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.net.URL;

public interface BaseRestService {

    ResponseEntity<String> getRequest(String url);

    ResponseEntity<String> postForEntity(URL url, Object container);

    void contextRefreshedEvent();


    ResponseEntity<Resource> authGetRequestResource(String url);

    ResponseEntity<Resource> getRequestResource(String url, HttpHeaders headers);

    <T> ResponseEntity<String> authPostRequest(String url, HttpHeaders headers, T body);

    ResponseEntity<String> authGetRequest(String url, HttpHeaders headers);

    ResponseToken requestToken(String userName, String password);


}
