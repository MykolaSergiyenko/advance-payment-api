package online.oboz.trip.trip_carrier_advance_payment_api.service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.Trip;
import online.oboz.trip.trip_carrier_advance_payment_api.service.RestService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.dto.TripDocuments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class OrdersApiService {

    private static final Logger log = LoggerFactory.getLogger(OrdersApiService.class);

    private final ApplicationProperties applicationProperties;
    private final RestService restService;
    private final ObjectMapper objectMapper;

    public OrdersApiService(
        ApplicationProperties applicationProperties,
        RestService restService,
        ObjectMapper objectMapper
    ) {
        this.applicationProperties = applicationProperties;
        this.restService = restService;
        this.objectMapper = objectMapper;
    }


    public boolean saveTripDocuments(long orderId, long tripId, String fileUuid) {
        log.info("Save trip documents for order: {}, trip: {}, fileUuid: {} .", orderId, tripId, fileUuid);
        String url = String.format(applicationProperties.getOrdersApiUrl().toString(), orderId, tripId);
        log.info("Save trip documents url: {} .", url);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String requestBody = String.format(applicationProperties.getOrdersApiSaveBody().value(), fileUuid);
        ResponseEntity<String> response = restService.authPostRequest(url, headers, requestBody);
        log.info("Save trip documents response: {} .", response);
        if (response.getStatusCode().value() == 200) {
            log.info("Success save TripDocuments " + fileUuid);
            return true;
        } else {
            log.error("Failed save TripDocuments {}", response);
            return false;
        }
    }

    public Map<String, String> findAdvanceRequestDocs(Advance advance) {
        TripDocuments docs = findAllDocuments(advance);
        return docs.findAdvanceRequestDocsFileMap();
    }

    public Map<String, String> findTripRequestDocs(Advance advance) {
        TripDocuments docs = findAllDocuments(advance);
        return docs.findTripRequestDocsFileMap();
    }



    public TripDocuments findAllDocuments(Advance advance) {
        TripDocuments tripDocuments = null;
        String response = getDocumentWithUuidFiles(advance.getAdvanceTripFields().getOrderId(), advance.getAdvanceTripFields().getTripId());
        try {
            tripDocuments = objectMapper.readValue(response, TripDocuments.class);
        } catch (IOException e) {
            log.error("Failed parse TripDocuments", e);
        }
        return tripDocuments;
    }

    private String getDocumentWithUuidFiles(Long orderId, Long tripId) {
        String formattedUrl = String.format(applicationProperties.getOrdersApiUrl().toString(), orderId, tripId);
        try {
            ResponseEntity<String> response = restService.authGetRequest(formattedUrl, new HttpHeaders());
            if (response.getStatusCode().value() == 200) {
                return response.getBody();
            } else {
                log.error("Failed request to orders-api: " + response);
                return null;
            }
        } catch (Exception e) {
            log.error("Failed request to orders-api: ", e);
            return null;
        }
    }
}
