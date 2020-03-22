package online.oboz.trip.trip_carrier_advance_payment_api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.Trip;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.TripDocuments;
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
@Slf4j
public class RestService {
    private final ApplicationProperties applicationProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public RestService(ApplicationProperties applicationProperties, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.applicationProperties = applicationProperties;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public ResponseEntity<Resource> getResourceResponseEntity(String url, HttpHeaders headers) {
        try {
            HttpEntity request = new HttpEntity(headers);
            ResponseEntity<Resource> response = new RestTemplate().exchange(url, HttpMethod.GET, request, Resource.class);
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
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJtZWwxMDZWemFlZGpmU3ctUVRtblVLa3ZuQUlfdXB3TGdRZHY4Vm85SEk4In0.eyJqdGkiOiI3MmRjZDA2NS1mMDhmLTRkZjAtYjMwMi0zNzNiYzUwZDAzNjYiLCJleHAiOjE2MTU2NDk0NjAsIm5iZiI6MCwiaWF0IjoxNTg0MTEzNDYwLCJpc3MiOiJodHRwczovL2Rldi5vYm96Lm9ubGluZS9hdXRoL3JlYWxtcy9tYXN0ZXIiLCJhdWQiOiJlbHAiLCJzdWIiOiJmOmZjYzAzMzZjLWU2ZjItNGVlNy1iOWViLWMyNTY0NjczYjAzNjo0MjYyNyIsInR5cCI6IkJlYXJlciIsImF6cCI6ImVscCIsImF1dGhfdGltZSI6MCwic2Vzc2lvbl9zdGF0ZSI6IjBjM2U4MzI4LTYxNjctNDlhYi05MWI2LWJjNGMyM2ZhZjljZSIsImFjciI6IjEiLCJjbGllbnRfc2Vzc2lvbiI6IjBmYTc4NjVmLTVmN2QtNDFhOC05NjU2LTE0MWQ0YjU3ODI1OSIsImFsbG93ZWQtb3JpZ2lucyI6WyIqIl0sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJjcmVhdGUtcmVhbG0iLCJjYXJyaWVyIiwiZWxwLWFkbWluIiwic2VuZGVyIiwiYWRtaW4iLCJ1bWFfYXV0aG9yaXphdGlvbiIsImRpc3BhdGNoZXIiXX0sInJlc291cmNlX2FjY2VzcyI6eyJkcml2ZXItYXBwLXJlYWxtIjp7InJvbGVzIjpbInZpZXctcmVhbG0iLCJ2aWV3LWlkZW50aXR5LXByb3ZpZGVycyIsIm1hbmFnZS1pZGVudGl0eS1wcm92aWRlcnMiLCJpbXBlcnNvbmF0aW9uIiwiY3JlYXRlLWNsaWVudCIsIm1hbmFnZS11c2VycyIsInZpZXctYXV0aG9yaXphdGlvbiIsIm1hbmFnZS1ldmVudHMiLCJtYW5hZ2UtcmVhbG0iLCJ2aWV3LWV2ZW50cyIsInZpZXctdXNlcnMiLCJ2aWV3LWNsaWVudHMiLCJtYW5hZ2UtYXV0aG9yaXphdGlvbiIsIm1hbmFnZS1jbGllbnRzIl19LCJkcml2ZXItYXBwLXJlZ2lzdHJhdGlvbi1yZWFsbSI6eyJyb2xlcyI6WyJ2aWV3LXJlYWxtIiwidmlldy1pZGVudGl0eS1wcm92aWRlcnMiLCJtYW5hZ2UtaWRlbnRpdHktcHJvdmlkZXJzIiwiaW1wZXJzb25hdGlvbiIsImNyZWF0ZS1jbGllbnQiLCJtYW5hZ2UtdXNlcnMiLCJ2aWV3LWF1dGhvcml6YXRpb24iLCJtYW5hZ2UtZXZlbnRzIiwibWFuYWdlLXJlYWxtIiwidmlldy1ldmVudHMiLCJ2aWV3LXVzZXJzIiwidmlldy1jbGllbnRzIiwibWFuYWdlLWF1dGhvcml6YXRpb24iLCJtYW5hZ2UtY2xpZW50cyJdfSwiZG1zLXJlYWxtIjp7InJvbGVzIjpbInZpZXctcmVhbG0iLCJ2aWV3LWlkZW50aXR5LXByb3ZpZGVycyIsIm1hbmFnZS1pZGVudGl0eS1wcm92aWRlcnMiLCJpbXBlcnNvbmF0aW9uIiwiY3JlYXRlLWNsaWVudCIsIm1hbmFnZS11c2VycyIsInZpZXctYXV0aG9yaXphdGlvbiIsIm1hbmFnZS1ldmVudHMiLCJtYW5hZ2UtcmVhbG0iLCJ2aWV3LWV2ZW50cyIsInZpZXctdXNlcnMiLCJ2aWV3LWNsaWVudHMiLCJtYW5hZ2UtYXV0aG9yaXphdGlvbiIsIm1hbmFnZS1jbGllbnRzIl19LCJwdWJsaWMtcmVhbG0iOnsicm9sZXMiOlsidmlldy1yZWFsbSIsInZpZXctaWRlbnRpdHktcHJvdmlkZXJzIiwibWFuYWdlLWlkZW50aXR5LXByb3ZpZGVycyIsImltcGVyc29uYXRpb24iLCJjcmVhdGUtY2xpZW50IiwibWFuYWdlLXVzZXJzIiwidmlldy1hdXRob3JpemF0aW9uIiwibWFuYWdlLWV2ZW50cyIsIm1hbmFnZS1yZWFsbSIsInZpZXctZXZlbnRzIiwidmlldy11c2VycyIsInZpZXctY2xpZW50cyIsIm1hbmFnZS1hdXRob3JpemF0aW9uIiwibWFuYWdlLWNsaWVudHMiXX0sIm1hc3Rlci1yZWFsbSI6eyJyb2xlcyI6WyJ2aWV3LXJlYWxtIiwidmlldy1pZGVudGl0eS1wcm92aWRlcnMiLCJtYW5hZ2UtaWRlbnRpdHktcHJvdmlkZXJzIiwiaW1wZXJzb25hdGlvbiIsImNyZWF0ZS1jbGllbnQiLCJtYW5hZ2UtdXNlcnMiLCJ2aWV3LWF1dGhvcml6YXRpb24iLCJtYW5hZ2UtZXZlbnRzIiwibWFuYWdlLXJlYWxtIiwidmlldy1ldmVudHMiLCJ2aWV3LXVzZXJzIiwidmlldy1jbGllbnRzIiwibWFuYWdlLWF1dGhvcml6YXRpb24iLCJtYW5hZ2UtY2xpZW50cyJdfSwiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwiY29udHJhY3RvciI6eyJpZCI6IjYwOSJ9LCJwZXJzb24iOnsiaWQiOiI0MjYyNyJ9LCJuYW1lIjoi0JPQtdC90L3QsNC00LjQuSDQnNCw0LrQsNGA0L7QsiIsInByZWZlcnJlZF91c2VybmFtZSI6IjAwMDAwMDEwMDUiLCJnaXZlbl9uYW1lIjoi0JPQtdC90L3QsNC00LjQuSIsImZhbWlseV9uYW1lIjoi0JzQsNC60LDRgNC-0LIiLCJlbWFpbCI6ImdtYWthcm92QG9ib3ouY29tIn0.DDmESVFTbTeZVViL_6C5PQidsbmNJ7MVgtEPHpkchF7E00gJ0lYNhvtknFK8M7S-d6_8j2_4_QQjN5VcyPY0tzIUBJgYTaIT-LgGu6NF94-G1qrWIqDxVe4btKEijMKKYcBfLNzp9v59bRDoWMpFzF78yHqmBKeSBzxBPllfMvwxbUEHtiQxqFAB7-DEu48-PRy91C1I3StemW8qyLSoDnOzkDpwawaO_5K2fK6tnOd6h4Di4S9oWYNQ9JHypyRMrstYTHMp1z9vAcUWbjFbQrUTmp-qpkYGi_eIKf__fAJdcGUrVZEboQFQZxA0mzYpCkVjHWu_P0mFnd13_AqYNw");
        ResponseEntity<Resource> response = getResourceResponseEntity(url, headers);
        if (response != null) return response;
        log.error("server {} returned bad response", url);
        return null;
    }

    public ResponseEntity<Void> saveTripDocuments(String url, String fileUuid, String bearer) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.AUTHORIZATION, bearer);
        final String s = "{\"trip_document\":{\"id\":null,\"file_id\":\"%s\",\"document_type_code\":\"assignment_advance_request\",\"name\":\"Заявка на авансирование\"}}";
        HttpEntity<String> request = new HttpEntity<>(String.format(s, fileUuid), headers);
        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.POST, request, Void.class);
        if (response.getStatusCode().value() == 200) {
            log.info("saveTripDocuments ok");
        } else {
            log.error("saveTripDocuments fail. http code {}", response.getStatusCode().value());
        }
        return response;
    }


    public ResponseEntity<String> getFileUuid(MultipartFile filename, String url, String bearer) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "multipart/form-data;");
        headers.add(HttpHeaders.AUTHORIZATION, bearer);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", filename.getResource());
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
        return restTemplate.exchange(url, HttpMethod.POST, request, String.class);
    }

    public Map<String, String> findTripRequestDocs(Trip trip) {
        Map<String, String> fileUuidMap = new HashMap<>();
        try {
            TripDocuments tripDocuments = objectMapper.readValue(getDocumentWithUuidFiles(trip.getOrderId(), trip.getId()), TripDocuments.class);
            tripDocuments.getTripDocuments().forEach(doc -> {
                final String fileId = doc.getFileId();
                if (fileId != null && ("trip_request".equals(doc.documentTypeCode) || "request".equals(doc.documentTypeCode))) {
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
            TripDocuments tripDocuments = objectMapper.readValue(getDocumentWithUuidFiles(trip.getOrderId(), trip.getId()), TripDocuments.class);
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
            headers.add("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJtZWwxMDZWemFlZGpmU3ctUVRtblVLa3ZuQUlfdXB3TGdRZHY4Vm85SEk4In0.eyJqdGkiOiI4ZmY5MzA0Zi05Mjc0LTQ0ZmItOWMxMC0wZGE1YmRlMmQ3MTAiLCJleHAiOjE2MTU1NjYyMjcsIm5iZiI6MCwiaWF0IjoxNTg0MDMwMjI3LCJpc3MiOiJodHRwczovL2Rldi5vYm96Lm9ubGluZS9hdXRoL3JlYWxtcy9tYXN0ZXIiLCJhdWQiOiJlbHAiLCJzdWIiOiJmOmZjYzAzMzZjLWU2ZjItNGVlNy1iOWViLWMyNTY0NjczYjAzNjo0MjYyNyIsInR5cCI6IkJlYXJlciIsImF6cCI6ImVscCIsImF1dGhfdGltZSI6MCwic2Vzc2lvbl9zdGF0ZSI6ImRkZWU2NTZiLWFhZTMtNGRlYS1iNWFlLWQyMGUzODhlZjY0ZCIsImFjciI6IjEiLCJjbGllbnRfc2Vzc2lvbiI6IjUzODgzY2YzLTVkZjgtNDFkOS1hMDc2LWFlZmQ2ZDFlZjAzZSIsImFsbG93ZWQtb3JpZ2lucyI6WyIqIl0sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJjcmVhdGUtcmVhbG0iLCJjYXJyaWVyIiwiZWxwLWFkbWluIiwic2VuZGVyIiwiYWRtaW4iLCJ1bWFfYXV0aG9yaXphdGlvbiIsImRpc3BhdGNoZXIiXX0sInJlc291cmNlX2FjY2VzcyI6eyJkcml2ZXItYXBwLXJlYWxtIjp7InJvbGVzIjpbInZpZXctcmVhbG0iLCJ2aWV3LWlkZW50aXR5LXByb3ZpZGVycyIsIm1hbmFnZS1pZGVudGl0eS1wcm92aWRlcnMiLCJpbXBlcnNvbmF0aW9uIiwiY3JlYXRlLWNsaWVudCIsIm1hbmFnZS11c2VycyIsInZpZXctYXV0aG9yaXphdGlvbiIsIm1hbmFnZS1ldmVudHMiLCJtYW5hZ2UtcmVhbG0iLCJ2aWV3LWV2ZW50cyIsInZpZXctdXNlcnMiLCJ2aWV3LWNsaWVudHMiLCJtYW5hZ2UtYXV0aG9yaXphdGlvbiIsIm1hbmFnZS1jbGllbnRzIl19LCJkcml2ZXItYXBwLXJlZ2lzdHJhdGlvbi1yZWFsbSI6eyJyb2xlcyI6WyJ2aWV3LXJlYWxtIiwidmlldy1pZGVudGl0eS1wcm92aWRlcnMiLCJtYW5hZ2UtaWRlbnRpdHktcHJvdmlkZXJzIiwiaW1wZXJzb25hdGlvbiIsImNyZWF0ZS1jbGllbnQiLCJtYW5hZ2UtdXNlcnMiLCJ2aWV3LWF1dGhvcml6YXRpb24iLCJtYW5hZ2UtZXZlbnRzIiwibWFuYWdlLXJlYWxtIiwidmlldy1ldmVudHMiLCJ2aWV3LXVzZXJzIiwidmlldy1jbGllbnRzIiwibWFuYWdlLWF1dGhvcml6YXRpb24iLCJtYW5hZ2UtY2xpZW50cyJdfSwiZG1zLXJlYWxtIjp7InJvbGVzIjpbInZpZXctcmVhbG0iLCJ2aWV3LWlkZW50aXR5LXByb3ZpZGVycyIsIm1hbmFnZS1pZGVudGl0eS1wcm92aWRlcnMiLCJpbXBlcnNvbmF0aW9uIiwiY3JlYXRlLWNsaWVudCIsIm1hbmFnZS11c2VycyIsInZpZXctYXV0aG9yaXphdGlvbiIsIm1hbmFnZS1ldmVudHMiLCJtYW5hZ2UtcmVhbG0iLCJ2aWV3LWV2ZW50cyIsInZpZXctdXNlcnMiLCJ2aWV3LWNsaWVudHMiLCJtYW5hZ2UtYXV0aG9yaXphdGlvbiIsIm1hbmFnZS1jbGllbnRzIl19LCJwdWJsaWMtcmVhbG0iOnsicm9sZXMiOlsidmlldy1yZWFsbSIsInZpZXctaWRlbnRpdHktcHJvdmlkZXJzIiwibWFuYWdlLWlkZW50aXR5LXByb3ZpZGVycyIsImltcGVyc29uYXRpb24iLCJjcmVhdGUtY2xpZW50IiwibWFuYWdlLXVzZXJzIiwidmlldy1hdXRob3JpemF0aW9uIiwibWFuYWdlLWV2ZW50cyIsIm1hbmFnZS1yZWFsbSIsInZpZXctZXZlbnRzIiwidmlldy11c2VycyIsInZpZXctY2xpZW50cyIsIm1hbmFnZS1hdXRob3JpemF0aW9uIiwibWFuYWdlLWNsaWVudHMiXX0sIm1hc3Rlci1yZWFsbSI6eyJyb2xlcyI6WyJ2aWV3LXJlYWxtIiwidmlldy1pZGVudGl0eS1wcm92aWRlcnMiLCJtYW5hZ2UtaWRlbnRpdHktcHJvdmlkZXJzIiwiaW1wZXJzb25hdGlvbiIsImNyZWF0ZS1jbGllbnQiLCJtYW5hZ2UtdXNlcnMiLCJ2aWV3LWF1dGhvcml6YXRpb24iLCJtYW5hZ2UtZXZlbnRzIiwibWFuYWdlLXJlYWxtIiwidmlldy1ldmVudHMiLCJ2aWV3LXVzZXJzIiwidmlldy1jbGllbnRzIiwibWFuYWdlLWF1dGhvcml6YXRpb24iLCJtYW5hZ2UtY2xpZW50cyJdfSwiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwiY29udHJhY3RvciI6eyJpZCI6IjYwOSJ9LCJwZXJzb24iOnsiaWQiOiI0MjYyNyJ9LCJuYW1lIjoi0JPQtdC90L3QsNC00LjQuSDQnNCw0LrQsNGA0L7QsiIsInByZWZlcnJlZF91c2VybmFtZSI6IjAwMDAwMDEwMDUiLCJnaXZlbl9uYW1lIjoi0JPQtdC90L3QsNC00LjQuSIsImZhbWlseV9uYW1lIjoi0JzQsNC60LDRgNC-0LIiLCJlbWFpbCI6ImdtYWthcm92QG9ib3ouY29tIn0.BpfwljwBvtUVDSeXlx_5lmfehVRlGSuk83DsmPNOP4Clzx0E61pg1rDt-7w77S-Al6Yc58AvOMHNgsdKQhIpmThd_W76jBCd71YbdtIt6iOQYqwTrzSzn42Ks7NjwUWlJFdS5829q8y8ec9IsTBWSyy-4JggEedncEeWuEs8XfLZhfw0QLTcgkoPr_h5T6Lk2_aEQ5dvhUmuKtVll3tanzl2pgg02oaEHLWDhRf267xjhefntW8wISZd8-Pe-tamlZkeb8RS8GABb-iTDCdtm4E_p7X1cHfPyhW8EIa8qwkk8dkFKoO2apKUJ-z_rNSoNRNKrWx7AEoS46v-g4h5Uw");
            HttpEntity request = new HttpEntity(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            if (response.getStatusCode().value() == 200) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("Some Exeption", e);
        }
        log.error("server {} returned bad response", url);
        return "";
    }
}
