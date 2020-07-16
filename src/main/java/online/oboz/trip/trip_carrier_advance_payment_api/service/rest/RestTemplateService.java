package online.oboz.trip.trip_carrier_advance_payment_api.service.rest;

import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.ResponseToken;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.net.URL;

public interface RestTemplateService {

    ResponseEntity<String> getRequest(String url);

    ResponseEntity<String> postForEntity(URL url, Object container);

    void contextRefreshedEvent();


    /**
     * POST-запрос с авторизацией
     * @param url
     * @param headers
     * @param body
     * @param <T>
     * @return
     */
    <T> ResponseEntity<String> authPostRequest(String url, HttpHeaders headers, T body);

    /**
     * GET-запрос с авторизацией
     * @param url
     * @param headers
     * @return
     */
    ResponseEntity<String> authGetRequest(String url, HttpHeaders headers);



    /**
     * GET-запрос с авторизацией к ресурсу
     * @param url
     * @return Resource
     */
    ResponseEntity<Resource> authGetRequestResource(String url);

    /**
     * GET-запрос к ресурсу
     * @param url
     * @param headers
     * @return Resource
     */
    ResponseEntity<Resource> getRequestResource(String url, HttpHeaders headers);

    /**
     * Запрос токена для технического пользователя
     * @param userName
     * @param password
     * @return
     */
    ResponseToken requestToken(String userName, String password);


}
