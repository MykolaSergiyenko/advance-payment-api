package online.oboz.trip.trip_carrier_advance_payment_api.service.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.service.RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class BStoreService {
    private static final Logger log = LoggerFactory.getLogger(BStoreService.class);

    private final ApplicationProperties applicationProperties;
    private final RestService restService;
    private final ObjectMapper objectMapper;

    public BStoreService(
        ApplicationProperties applicationProperties,
        RestService restService,
        ObjectMapper objectMapper
    ) {
        this.applicationProperties = applicationProperties;
        this.restService = restService;
        this.objectMapper = objectMapper;
    }

    public ResponseEntity<Resource> requestResourceFromBStore(String uuidFile) {
        String url = applicationProperties.getBStoreUrl() + uuidFile;
        ResponseEntity<Resource> response = restService.authRequestResource(url);
        if (response != null) {
            return response;
        }
        log.error("server {} returned bad response", url);
        return null;
    }

    public String getFileUuid(MultipartFile filename) {
        String url = applicationProperties.getBStoreUrl() + "pdf/";
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "multipart/form-data");

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", filename.getResource());

        ResponseEntity<String> response = restService.executePostAuthRequest(url, headers, body);

        String result = null;
        if (response.getStatusCode().value() == 200) {
            try {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                result = jsonNode.get("file_uuid").asText();
            } catch (IOException e) {
                log.error("Failed parse response from bstore. " + response);
            }
        }
        return result;
    }
}
