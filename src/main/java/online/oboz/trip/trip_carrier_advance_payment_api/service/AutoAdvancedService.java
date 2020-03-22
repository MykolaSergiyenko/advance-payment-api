package online.oboz.trip.trip_carrier_advance_payment_api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.*;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;

@Slf4j
@Service
@EnableScheduling
public class AutoAdvancedService {

    private final AdvancePaymentCostRepository advancePaymentCostRepository;
    private final TripRequestAdvancePaymentRepository tripRequestAdvancePaymentRepository;
    private final ContractorAdvancePaymentContactRepository contractorAdvancePaymentContactRepository;
    private final TripRepository tripRepository;
    private final ContractorRepository contractorRepository;
    private final RestTemplate restTemplate;
    private final ApplicationProperties applicationProperties;
    private final ObjectMapper objectMapper;
    private final AdvancePaymentContactService advancePaymentContactService;
    private final NotificationService notificationService;
//    private final AdvancePaymentDelegateImpl advancePaymentDelegate;

    @Autowired
    public AutoAdvancedService(AdvancePaymentCostRepository advancePaymentCostRepository,
                               TripRequestAdvancePaymentRepository tripRequestAdvancePaymentRepository,
                               ContractorAdvancePaymentContactRepository contractorAdvancePaymentContactRepository,
                               TripRepository tripRepository,
                               ContractorRepository contractorRepository,
                               RestTemplate restTemplate,
                               ApplicationProperties applicationProperties,
                               ObjectMapper objectMapper,
                               AdvancePaymentContactService advancePaymentContactService,
                               NotificationService notificationService/*,
                               AdvancePaymentDelegateImpl advancePaymentDelegate*/) {
        this.advancePaymentCostRepository = advancePaymentCostRepository;
        this.tripRequestAdvancePaymentRepository = tripRequestAdvancePaymentRepository;
        this.contractorAdvancePaymentContactRepository = contractorAdvancePaymentContactRepository;
        this.tripRepository = tripRepository;
        this.contractorRepository = contractorRepository;
        this.restTemplate = restTemplate;
        this.applicationProperties = applicationProperties;
        this.objectMapper = objectMapper;
        this.advancePaymentContactService = advancePaymentContactService;
        this.notificationService = notificationService;
//        this.advancePaymentDelegate = advancePaymentDelegate;

    }

    //        @Scheduled(cron = "${cron.expression:0 /1 * * * *}")
//    @Scheduled(fixedDelayString = "10000")
    void createTripRequestAdvancePayment() {
        tripRepository.getAutoApprovedTrips().forEach(trip -> {
                final Long id = trip.getId();
                //              advancePaymentCost закэшировать
                AdvancePaymentCost advancePaymentCost = advancePaymentCostRepository.searchAdvancePaymentCost(trip.getCost());
                TripRequestAdvancePayment tripRequestAdvancePayment = new TripRequestAdvancePayment();
                tripRequestAdvancePayment.setTripId(id);
                tripRequestAdvancePayment.setContractorId(trip.getContractorId());
                tripRequestAdvancePayment.setDriverId(trip.getDriverId());
                tripRequestAdvancePayment.setTripTypeCode(trip.getTripTypeCode());
                tripRequestAdvancePayment.setPaymentContractorId(trip.getPaymentContractorId());
                tripRequestAdvancePayment.setIsAutomationRequest(true);
                tripRequestAdvancePayment.setTripCost(trip.getCost());
                tripRequestAdvancePayment.setAdvancePaymentSum(advancePaymentCost.getAdvancePaymentSum());
                tripRequestAdvancePayment.setRegistrationFee(advancePaymentCost.getRegistrationFee());
                tripRequestAdvancePayment.setLoadingComplete(false);
                tripRequestAdvancePayment.setPageCarrierUrlIsAccess(true);
                tripRequestAdvancePayment.setIs1CSendAllowed(true);
                tripRequestAdvancePayment.setCancelAdvance(false);
                tripRequestAdvancePayment.setIsUnfSend(false);
                tripRequestAdvancePayment.setIsPaid(false);
                tripRequestAdvancePayment.setUuidRequest(UUID.randomUUID());
// TODO Спросить у Сергея если нажали кнопку в обозе  то вв кабинете перевозчика  что делать с кнопкой push_button_at: это разные кнопки
                ContractorAdvancePaymentContact contact = advancePaymentContactService.getAdvancePaymentContact(trip.getContractorId());
                tripRequestAdvancePaymentRepository.save(tripRequestAdvancePayment);
//                TODO не инжектится
//                if (contact != null && contact.getEmail() != null) {
//                    String paymentContractor = contractorRepository.getContractor(trip.getPaymentContractorId());
//                    MessageDto messageDto = advancePaymentDelegate.getMessageDto(tripRequestAdvancePayment, contact, paymentContractor);
//                    if (contact.getEmail() != null) {
//                        notificationService.sendEmail(messageDto);
//                    }
//                    if (contact.getPhone() != null) {
//                        notificationService.sendSms(messageDto);
//                    }
//                }
            }
        );
    }

    //    @Scheduled(cron = "${cron.expression:0 /1 * * * *}")
//    @Scheduled(fixedDelayString = "10000")
    void updateFileUuid() {
        List<TripRequestAdvancePayment> tripRequestAdvancePayments = tripRequestAdvancePaymentRepository.findRequestAdvancePaymentWithOutUuidFiles();
        tripRequestAdvancePayments.forEach(tripRequestAdvancePayment -> {
                final Long tripId = tripRequestAdvancePayment.getTripId();
            Trip trip = tripRepository.findById(tripId).get();
            Map<String, String> fileUuidMap = findTripRequestDocs(trip);
                if (!fileUuidMap.isEmpty()) {
                    String fileContractRequestUuid = Optional.ofNullable(fileUuidMap.get("request")).orElse(fileUuidMap.get("trip_request"));
                    if (fileContractRequestUuid != null) {
                        tripRequestAdvancePayment.setIsDownloadedContractApplication(true);
                        tripRequestAdvancePayment.setUuidContractApplicationFile(fileContractRequestUuid);
                    }
                    String fileAdvanceRequestUuid = fileUuidMap.get("assignment_advance_request");
                    if (fileAdvanceRequestUuid != null) {
                        tripRequestAdvancePayment.setIsDownloadedContractApplication(true);
                        tripRequestAdvancePayment.setUuidAdvanceApplicationFile(fileAdvanceRequestUuid);
                    }
                    if (tripRequestAdvancePayment.getUuidContractApplicationFile() != null &&
                        tripRequestAdvancePayment.getUuidAdvanceApplicationFile() != null) {
                        tripRequestAdvancePayment.setIs1CSendAllowed(true);
                    }
                }
            }
        );
        tripRequestAdvancePaymentRepository.saveAll(tripRequestAdvancePayments);
    }

    //    @Scheduled(cron = "${cron.expression:0 /1 * * * *}")
//    @Scheduled(fixedDelayString = "10000")
    void updateAutoAdvanse() {
        List<Contractor> contractors = contractorRepository.getContractor(applicationProperties.getMinCountTrip(),
            applicationProperties.getMinDateTrip());
        contractors.forEach(c -> c.setIsAutoAdvancePayment(true));
        contractorRepository.saveAll(contractors);
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
            ResponseEntity<String> response = new RestTemplate().exchange(url, HttpMethod.GET, request, String.class);

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
