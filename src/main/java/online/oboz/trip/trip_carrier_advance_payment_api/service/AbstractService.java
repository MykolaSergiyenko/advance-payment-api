package online.oboz.trip.trip_carrier_advance_payment_api.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public abstract class AbstractService {

    protected HttpEntity createHttpEntityToAuth(String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(MediaType.APPLICATION_FORM_URLENCODED_VALUE));
        return new HttpEntity(body, headers);
    }
}
