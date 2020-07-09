package online.oboz.trip.trip_carrier_advance_payment_api.service.integration.bstore;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.service.rest.RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;


import static org.springframework.http.HttpStatus.OK;

import java.io.IOException;
import java.util.UUID;

@Service
public class BStoreService implements StoreService {
    private static final Logger log = LoggerFactory.getLogger(BStoreService.class);

    private final ApplicationProperties applicationProperties;
    private final RestService restService;
    private final ObjectMapper objectMapper;

    @Autowired
    public BStoreService(
        ApplicationProperties applicationProperties,
        RestService restService,
        ObjectMapper objectMapper
    ) {
        this.applicationProperties = applicationProperties;
        this.restService = restService;
        this.objectMapper = objectMapper;
    }

    public ResponseEntity<Resource> requestResourceFromBStore(UUID uuidFile) {
        String url = applicationProperties.getbStoreUrl() + uuidFile.toString();
        ResponseEntity<Resource> response = restService.authGetRequestResource(url);
        if (response.getStatusCode() == OK) {
            return response;
        } else {
            log.error("B-Store-server {} returned bad response: {}.", url, response);
            return null;
        }
    }


    public UUID getFileUuid(MultipartFile filename) {
        String url = applicationProperties.getbStoreUrl().toString() +
            applicationProperties.getbStorePdf();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "multipart/form-data");
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", filename.getResource());
        ResponseEntity<String> response = null;
        try {
            response = restService.authPostRequest(url, headers, new LinkedMultiValueMap<>());
            if (response.getStatusCode() == OK) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                String result = jsonNode.get("file_uuid").asText();
                UUID fileUuid = UUID.fromString(result);
                log.info("Success save file to BStore " + fileUuid);
                return fileUuid;
            }
        } catch (IOException e) {
            log.error("Failed parse response from bstore. Filename: {}. Response: {}",
                filename, response);
        }
        return null;
    }
}
