package online.oboz.trip.trip_carrier_advance_payment_api.service.advance.tools.files.ordersapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.service.rest.RestService;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.response.dto.TripDocuments;
import online.oboz.trip.trip_carrier_advance_payment_api.service.util.ErrorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Deprecated
@Service
public class OrdersApiService implements OrdersFilesService {

    private static final Logger log = LoggerFactory.getLogger(OrdersApiService.class);

    private final ApplicationProperties applicationProperties;
    private final RestService restService;
    private final ObjectMapper objectMapper;
    private URL ordersApiUrl;
    private String saveAssignmentBody;

    @Deprecated
    public OrdersApiService(
        ApplicationProperties applicationProperties,
        RestService restService,
        ObjectMapper objectMapper
    ) {
        this.applicationProperties = applicationProperties;
        this.restService = restService;
        this.objectMapper = objectMapper;
        try {
            this.ordersApiUrl = new URL("https://oboz.online/api/orders/dispatcher/orders/%d/trips/%d/documents");
            this.saveAssignmentBody = "{\"trip_document\":{\"id\":null,\n" +
                "                       \"file_id\":%s,\n" +
                "                       \"document_type_code\":\"assignment_advance_request\",\n" +
                "                       \"name\":\"Заявка на авансирование\"}}";
        } catch (MalformedURLException e) {
            throw ordersApiError("Wrong service Orders-api-service URL.");
        }
    }


    @Deprecated
    @Override
    public Map<String, String> findAdvanceRequestDocs(Advance advance) {
        TripDocuments docs = findAllDocuments(advance);
        return docs.findAdvanceRequestDocsFileMap();
    }

    @Deprecated
    @Override
    public Map<String, String> findTripRequestDocs(Advance advance) {
        TripDocuments docs = findAllDocuments(advance);
        return docs.findTripRequestDocsFileMap();
    }

    @Deprecated
    @Override
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

    @Deprecated
    @Override
    public Boolean saveTripDocuments(Long orderId, Long tripId, UUID fileUuid) {
        if (fileUuid != null && orderId != null && tripId != null) {
            log.info("Save trip documents for order: {}, trip: {}, fileUuid: {} .", orderId, tripId, fileUuid);
            String url = String.format(ordersApiUrl.toString(), orderId, tripId);
            log.info("Save trip documents url: {} .", url);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String requestBody = String.format(saveAssignmentBody, fileUuid);
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
        return false;
    }

    @Deprecated
    public Boolean isDownloadAllDocuments(Advance advance) {
        //использовать только в confirm
        Map<String, String> fileRequestUuidMap = findTripRequestDocs(advance);
        Map<String, String> fileAdvanceRequestUuidMap = findAdvanceRequestDocs(advance);
        String requestFileUuid = Optional.ofNullable(fileRequestUuidMap.get("request"))
            .orElse(fileRequestUuidMap.get("trip_request"));
        String advanceRequestFileUuid = fileAdvanceRequestUuidMap.get("assignment_advance_request");
        boolean isAllDocsUpload = requestFileUuid != null && advanceRequestFileUuid != null;
        if (!isAllDocsUpload) {
            log.info("Не загружены документы. " + advance.getUuid());
        }
        return isAllDocsUpload;
    }

    @Deprecated
    private String getDocumentWithUuidFiles(Long orderId, Long tripId) {
        String formattedUrl = String.format(ordersApiUrl.toString(), orderId, tripId);
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

    @Deprecated
    private BusinessLogicException ordersApiError(String message) {
        return ErrorUtils.getInternalError(message);
    }

}