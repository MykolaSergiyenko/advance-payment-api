package online.oboz.trip.trip_carrier_advance_payment_api.service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.Trip;
import online.oboz.trip.trip_carrier_advance_payment_api.service.RestService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.dto.TripDocuments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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


    //TODO: use in app.props?
    private static final String SAVE_TRIP_DOCUMENTS_REQUEST_BODY = "{\"trip_document\":{" +
        "\"id\":null,\"file_id\":\"%s\"," +
        "\"document_type_code\":\"assignment_advance_request\"," +
        "\"name\":\"Заявка на авансирование\"}" +
        "}";

    public boolean saveTripDocuments(long orderId, long tripId, String fileUuid) {
        log.info("saveTripDocuments for order {} trip {} fileUuid {} ", orderId, tripId, fileUuid);
        String url = String.format(applicationProperties.getOrdersApiUrl().toString(), orderId, tripId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String requestBody = String.format(SAVE_TRIP_DOCUMENTS_REQUEST_BODY, fileUuid);

        ResponseEntity<String> response = restService.executePostAuthRequest(url, headers, requestBody);

        if (response.getStatusCode().value() == 200) {
            log.info("Success save TripDocuments " + fileUuid);
            return true;
        } else {
            log.error("Failed save TripDocuments {}", response);
            return false;
        }
    }

    public Map<String, String> findAdvanceRequestDocs(Trip trip) {
        return findDocuments(trip, "assignment_advance_request");
    }

    public Map<String, String> findTripRequestDocs(Trip trip) {
        return findDocuments(trip, "trip_request", "request");
    }

    public Map<String, String> findDocuments(Trip trip, String... types) {
        List<String> expectedTypes = Arrays.asList(types);
        Map<String, String> fileUuidMap = new HashMap<>();
        TripDocuments tripDocuments;
        String response = getDocumentWithUuidFiles(trip.getOrderId(), trip.getId());
        try {
            tripDocuments = objectMapper.readValue(response, TripDocuments.class);
        } catch (IOException e) {
            log.error("Failed parse TripDocuments", e);
            return new HashMap<>();
        }
        tripDocuments.getTripDocuments().stream()
            .filter(d -> d.getFileId() != null)
            .filter(doc -> expectedTypes.contains(doc.getDocumentTypeCode()))
            .forEach(doc -> fileUuidMap.put(doc.getDocumentTypeCode(), doc.getFileId()));
        return fileUuidMap;
    }

    private String getDocumentWithUuidFiles(Long orderId, Long tripId) {
        String url =
            String.format(applicationProperties.getOrdersApiUrl().toString(),
                orderId, tripId);
        try {
            ResponseEntity<String> response = restService.executeGetAuthRequest(url,  new HttpHeaders());
            if (response.getStatusCode().value() == 200) {
                return response.getBody();
            } else {
                log.error("Failed request to orders-api " + response);
            }
        } catch (Exception e) {
            log.error("Failed request to orders-api ", e);
        }
        return "";
    }
}
