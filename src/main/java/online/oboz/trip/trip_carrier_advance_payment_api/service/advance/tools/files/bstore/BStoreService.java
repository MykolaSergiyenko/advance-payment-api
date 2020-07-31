package online.oboz.trip.trip_carrier_advance_payment_api.service.advance.tools.files.bstore;

import com.fasterxml.jackson.databind.ObjectMapper;
import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.service.rest.RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;


import static org.springframework.http.HttpStatus.OK;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

@Service
public class BStoreService implements StoreService {
    private static final Logger log = LoggerFactory.getLogger(BStoreService.class);

    private final RestService restService;
    private final ObjectMapper objectMapper;
    private final URL bStoreUrl;
    private final String bStoreSuffix;

    public BStoreService(
        ApplicationProperties applicationProperties,
        RestService restService,
        ObjectMapper objectMapper
    ) {
        this.restService = restService;
        this.objectMapper = objectMapper;
        this.bStoreUrl = applicationProperties.getbStoreUrl();
        this.bStoreSuffix = applicationProperties.getbStorePdf();
    }

    public ResponseEntity<Resource> requestResourceFromBStore(UUID uuidFile) {
        String url = bStoreUrl + uuidFile.toString();
        ResponseEntity<Resource> response = restService.authGetRequestResource(url);
        if (response.getStatusCode() == OK) {
            return response;
        } else {
            log.error("B-Store-server {} returned bad response: {}.", url, response);
            return null;
        }
    }


    public UUID saveFile(MultipartFile file) {
        String url = bStoreUrl.toString() + bStoreSuffix;
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "multipart/form-data");

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource());

        ResponseEntity<String> response = null;
        try {
            response = restService.authPostRequest(url, headers, body);
            if (response.getStatusCode() == OK) {
                UUID fileUuid = UUID.fromString(objectMapper.readTree(response.getBody()).get("file_uuid").asText());
                log.info("Success save file to BStore {}.", fileUuid);
                return fileUuid;
            } else {
                log.info("B-Store response isn't OK: {}.", response);
            }
        } catch (IOException e) {
            log.error("Failed parse response from bstore. Filename: {}. Response: {}", file, response);
        }
        return null;
    }
}
