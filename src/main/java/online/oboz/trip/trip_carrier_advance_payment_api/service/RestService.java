package online.oboz.trip.trip_carrier_advance_payment_api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.Trip;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.TripDocuments;
import online.oboz.trip.trip_carrier_advance_payment_api.exception.AuthException;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.ResponseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class RestService extends AbstractService {
    private static final Logger log = LoggerFactory.getLogger(RestService.class);
    private final ApplicationProperties applicationProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public RestService(
        ApplicationProperties applicationProperties,
        RestTemplate restTemplate,
        ObjectMapper objectMapper
    ) {
        this.applicationProperties = applicationProperties;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    private String ACCESS_TOKEN = null;

    @EventListener(ContextRefreshedEvent.class)
    public void contextRefreshedEvent() {
        String user = applicationProperties.getUsername();
        String password = applicationProperties.getPassword();
        ResponseToken responseToken = getToken(user, password);
        ACCESS_TOKEN = responseToken.getAccessToken();
    }

    public ResponseToken getToken(String userName, String password) {
        try {
            ResponseToken responseToken;

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

            responseToken = response.getBody();
            responseToken.setTokenType("Bearer");
            return responseToken;
        } catch (Exception e) {
            throw new AuthException("Failed request token from keycloak. ", e);
        }
    }

    public ResponseEntity<Resource> getResourceResponseEntity(String url, HttpHeaders headers) {
        try {
            HttpEntity request = new HttpEntity(headers);
            ResponseEntity<Resource> response = restTemplate.exchange(url, HttpMethod.GET, request, Resource.class);
            if (response.getStatusCode().value() == 200 || response.getStatusCode() == HttpStatus.MOVED_PERMANENTLY) {
                return response;
            }
        } catch (Exception e) {
            log.error("Some Exception", e);
        }
        return null;
    }

    public ResponseEntity<Resource> getResourceBStore(String uuidFile) {
        String url = applicationProperties.getBStoreUrl() + uuidFile;
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + ACCESS_TOKEN);
        ResponseEntity<Resource> response = getResourceResponseEntity(url, headers);
        if (response != null) {
            return response;
        }
        log.error("server {} returned bad response", url);
        return null;
    }

    private static final String SAVE_TRIP_DOCUMENTS_REQUEST_BODY = "{\"trip_document\":{" +
        "\"id\":null,\"file_id\":\"%s\"," +
        "\"document_type_code\":\"assignment_advance_request\"," +
        "\"name\":\"Заявка на авансирование\"}" +
        "}";

    public ResponseEntity<Void> saveTripDocuments(String url, String fileUuid) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + ACCESS_TOKEN);
        String requestBody = String.format(SAVE_TRIP_DOCUMENTS_REQUEST_BODY, fileUuid);
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.POST, request, Void.class);
        if (response.getStatusCode().value() == 200) {
            log.info("saveTripDocuments ok");
        } else {
            log.error("saveTripDocuments fail. http code {}", response.getStatusCode().value());
        }
        return response;
    }


    public ResponseEntity<String> getFileUuid(MultipartFile filename, String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "multipart/form-data");
        final String bearer = "Bearer " + ACCESS_TOKEN;
        log.debug("Bearer is: {}", bearer);
        headers.add(HttpHeaders.AUTHORIZATION, bearer);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", filename.getResource());
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
        return restTemplate.exchange(url, HttpMethod.POST, request, String.class);
    }

    public Map<String, String> findTripRequestDocs(Trip trip) {
        Map<String, String> fileUuidMap = new HashMap<>();
        try {
            TripDocuments tripDocuments = objectMapper.readValue(
                getDocumentWithUuidFiles(trip.getOrderId(), trip.getId()), TripDocuments.class
            );
            tripDocuments.getTripDocuments().forEach(doc -> {
                final String fileId = doc.getFileId();
                if (fileId != null &&
                    ("trip_request".equals(doc.documentTypeCode) || "request".equals(doc.documentTypeCode))
                ) {
                    fileUuidMap.put(doc.documentTypeCode, fileId);
                    log.info(doc.getFileId());
                }
            });
        } catch (IOException e) {
            log.error("can't parse response", e);
        }
        return fileUuidMap;
    }

    public Map<String, String> findAdvanceRequestDocs(Trip trip) {
        Map<String, String> fileUuidMap = new HashMap<>();
        try {
            TripDocuments tripDocuments = objectMapper.readValue(
                getDocumentWithUuidFiles(trip.getOrderId(), trip.getId()), TripDocuments.class
            );
            tripDocuments.getTripDocuments().forEach(doc -> {
                final String fileId = doc.getFileId();
                if (fileId != null && "assignment_advance_request".equals(doc.documentTypeCode)) {
                    fileUuidMap.put(doc.documentTypeCode, fileId);
                    log.info(fileId);
                }
            });
        } catch (IOException e) {
            log.error("can't parse response", e);
        }
        return fileUuidMap;
    }

    private String getDocumentWithUuidFiles(Long orderId, Long tripId) {
        String url = String.format(applicationProperties.getOrdersApiUrl(), orderId, tripId);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + ACCESS_TOKEN);
            HttpEntity request = new HttpEntity(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            if (response.getStatusCode().value() == 200) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("Some Exception", e);
        }
        log.error("server {} returned bad response", url);
        return "";
    }
}
