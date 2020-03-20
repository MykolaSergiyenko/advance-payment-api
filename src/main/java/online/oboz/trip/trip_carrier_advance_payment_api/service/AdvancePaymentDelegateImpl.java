package online.oboz.trip.trip_carrier_advance_payment_api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.*;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.*;
import online.oboz.trip.trip_carrier_advance_payment_api.util.SecurityUtils;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.controller.AdvancePaymentApiDelegate;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.Error;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AdvancePaymentDelegateImpl implements AdvancePaymentApiDelegate {

    private static final String COMMENT = "Данному поставщику отправлен запрос на аванс в автоматическом режиме";
    private final AdvancePaymentCostRepository advancePaymentCostRepository;
    private final TripRequestAdvancePaymentRepository tripRequestAdvancePaymentRepository;
    private final ContractorAdvancePaymentContactRepository contractorAdvancePaymentContactRepository;
    private final ContractorAdvanceExclusionRepository contractorAdvanceExclusionRepository;
    private final TripRepository tripRepository;
    private final ContractorRepository contractorRepository;
    private final AutoAdvancedService autoAdvancedService;
    private final NotificationService notificationService;
    private final PersonRepository personRepository;
    private final RestTemplate restTemplate;
    private final ApplicationProperties applicationProperties;
    private final ObjectMapper objectMapper;
    private final AdvancePaymentContactService advancePaymentContactService;


    @Autowired
    public AdvancePaymentDelegateImpl(AdvancePaymentCostRepository advancePaymentCostRepository,
                                      TripRequestAdvancePaymentRepository tripRequestAdvancePaymentRepository,
                                      ContractorAdvancePaymentContactRepository contractorAdvancePaymentContactRepository,
                                      ContractorAdvanceExclusionRepository contractorAdvanceExclusionRepository,
                                      TripRepository tripRepository,
                                      ContractorRepository contractorRepository,
                                      AutoAdvancedService autoAdvancedService,
                                      NotificationService notificationService,
                                      PersonRepository personRepository,
                                      RestTemplate restTemplate,
                                      ApplicationProperties applicationProperties, ObjectMapper objectMapper, AdvancePaymentContactService advancePaymentContactService) {
        this.advancePaymentCostRepository = advancePaymentCostRepository;
        this.tripRequestAdvancePaymentRepository = tripRequestAdvancePaymentRepository;
        this.contractorAdvancePaymentContactRepository = contractorAdvancePaymentContactRepository;
        this.contractorAdvanceExclusionRepository = contractorAdvanceExclusionRepository;
        this.tripRepository = tripRepository;
        this.contractorRepository = contractorRepository;
        this.autoAdvancedService = autoAdvancedService;
        this.notificationService = notificationService;
        this.personRepository = personRepository;
        this.restTemplate = restTemplate;
        this.applicationProperties = applicationProperties;
        this.objectMapper = objectMapper;
        this.advancePaymentContactService = advancePaymentContactService;
    }

    @Override
    public ResponseEntity<ResponseAdvancePayment> searchAdvancePaymentRequest(Filter filter) {
//TODO:
        Pageable pageable = PageRequest.of(filter.getPage() - 1, filter.getPerPage());
        List<FrontAdvancePaymentResponse> responseList;
        responseList = tripRequestAdvancePaymentRepository.findTripRequestAdvancePayment(pageable).stream()
            .map(reс -> {
                FrontAdvancePaymentResponse frontAdvancePaymentResponse = new FrontAdvancePaymentResponse();
                ContractorAdvancePaymentContact contractorAdvancePaymentContact =
                    contractorAdvancePaymentContactRepository.findContractorAdvancePaymentContact(reс.getContractorId())
                        .orElse(new ContractorAdvancePaymentContact());
                Contractor contractor = contractorRepository.findById(reс.getContractorId()).orElse(new Contractor());
                String contractorPaymentName = contractorRepository.getContractor(reс.getPaymentContractorId());
                Trip trip = tripRepository.findById(reс.getTripId()).orElse(new Trip());
                AdvancePaymentDelegateImpl.this.getFrontAdvancePaymentResponse(reс, frontAdvancePaymentResponse, contractorAdvancePaymentContact, contractor, contractorPaymentName, trip);
                return frontAdvancePaymentResponse;
            })
            .collect(Collectors.toList());
        final ResponseAdvancePayment responseAdvancePayment = new ResponseAdvancePayment();
        responseAdvancePayment.setRequestAdvancePayment(responseList);
        return new ResponseEntity<>(responseAdvancePayment, HttpStatus.OK);
    }


    @Override
    @Transactional
    public ResponseEntity<Void> confirmAdvancePayment(Long requestAdvansePaymentId) {
        TripRequestAdvancePayment tripRequestAdvancePayment = getTripRequestAdvancePayment(requestAdvansePaymentId);
        Trip trip = tripRepository.findById(tripRequestAdvancePayment.getId()).orElseThrow(() -> {
                Error error = new Error();
                error.setErrorMessage("tripRequestAdvancePayment not found");
                return new BusinessLogicException(HttpStatus.UNPROCESSABLE_ENTITY, error);
            }
        );
        if (isDownloadAllDocuments(trip) && confirmRequestToUnf()) {
            ContractorAdvanceExclusion contractorAdvanceExclusion = contractorAdvanceExclusionRepository
                .findByContractorId(trip.getContractorId()).orElse(new ContractorAdvanceExclusion());
            tripRequestAdvancePayment.setIsUnfSend(true);
            tripRequestAdvancePayment.setPageCarrierUrlIsAccess(false);
            trip.setIsAdvancedPayment(true);
            tripRepository.save(trip);
            tripRequestAdvancePaymentRepository.save(tripRequestAdvancePayment);

            //TODO: проверить наличие записи контрактора в таблице исключений  при  отсутствии  добавить запись
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return getVoidResponseEntity("tripRequestAdvancePayment not found");
    }

    @Override
    public ResponseEntity<IsAdvancedRequestResponse> isAdvanced(Long tripId) {
        Trip trip = getMotorTrip(tripId);
        Contractor contractor = contractorRepository.findById(trip.getContractorId()).orElseThrow(() -> {
                Error error = new Error();
                error.setErrorMessage("Contractor not found for tripId: " + tripId);
                return new BusinessLogicException(HttpStatus.UNPROCESSABLE_ENTITY, error);
            }
        );
        TripRequestAdvancePayment tripRequestAdvancePayment = tripRequestAdvancePaymentRepository
            .findRequestAdvancePayment(tripId, trip.getDriverId(), trip.getContractorId());
        IsAdvancedRequestResponse isAdvancedRequestResponse = new IsAdvancedRequestResponse();

        setButtonAccessGetAdvance(trip, contractor, tripRequestAdvancePayment, isAdvancedRequestResponse);

        ContractorAdvanceExclusion contractorAdvanceExclusion = contractorAdvanceExclusionRepository
            .findByContractorId(trip.getContractorId()).orElse(new ContractorAdvanceExclusion());
        if (contractorAdvanceExclusion.getIsConfirmAdvance() != null &&
            !contractorAdvanceExclusion.getIsConfirmAdvance() &&
            contractorAdvanceExclusion.getDeletedAt() != null) {
            isAdvancedRequestResponse.setIsContractorLock(true);
        }
        if (tripRequestAdvancePayment == null) {
            isAdvancedRequestResponse.setIsAdvanssed(false);

            log.info("isAdvancedRequestResponse not found for tripId: {} , DriverId: {} , ContractorId {}", tripId, trip.getDriverId(), trip.getContractorId());
            return new ResponseEntity<>(isAdvancedRequestResponse, HttpStatus.OK);
        }
        final Boolean isAutomationRequest = tripRequestAdvancePayment.getIsAutomationRequest();
        if (isAutomationRequest) {
            isAdvancedRequestResponse.setIsAutoRequested(true);
            isAdvancedRequestResponse.setComment(COMMENT);
        }
        final Long authorId = tripRequestAdvancePayment.getAuthorId();
        if (authorId != null) {
            Person author = personRepository.findById(authorId).orElse(new Person());
            isAdvancedRequestResponse.setFirstName(author.getFirstName());
            isAdvancedRequestResponse.setLastName(author.getLastName());
            isAdvancedRequestResponse.setMiddleName(author.getMiddleName());
            isAdvancedRequestResponse.setAuthorId(authorId);
        }


        isAdvancedRequestResponse.setIsAdvanssed(tripRequestAdvancePayment.getIsUnfSend());
        isAdvancedRequestResponse.setTripTypeCode(tripRequestAdvancePayment.getTripTypeCode());
        isAdvancedRequestResponse.setCreatedAt(tripRequestAdvancePayment.getCreatedAt());

        return new ResponseEntity<>(isAdvancedRequestResponse, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> requestGiveAdvancePayment(Long tripId) {
        Double tripCostWithNds = tripRepository.getTripCostWithVat(tripId);
        AdvancePaymentCost advancePaymentCost = advancePaymentCostRepository.searchAdvancePaymentCost(tripCostWithNds);
        Trip trip = tripRepository.getMotorTrip(tripId).orElse(new Trip());


        if (advancePaymentCost == null || trip.getDriverId() == null || trip.getContractorId() == null) {
            return getVoidResponseEntity("Не назначены необходимы поля: advancePaymentCost DriverId ContractorId");
        }
        if (autoAdvancedService.findTripRequestDocs(trip).isEmpty()) {
            getVoidResponseEntity("Не загружены Договор заявка / заявка ");
        }

        TripRequestAdvancePayment tripRequestAdvancePayment = tripRequestAdvancePaymentRepository
            .findRequestAdvancePayment(tripId,
                trip.getDriverId(),
                trip.getContractorId());
        if (tripRequestAdvancePayment != null) {
            getVoidResponseEntity("tripRequestAdvancePayment is present");
        }
        Contractor contractor = contractorRepository.findById(trip.getContractorId()).orElse(new Contractor());
        String paymentContractor = contractorRepository.getContractor(trip.getPaymentContractorId());
        tripRequestAdvancePayment = getTripRequestAdvancePayment(tripId,
            contractor,
            tripCostWithNds,
            advancePaymentCost,
            tripRequestAdvancePayment.getIsUnfSend(),
            trip);
        ContractorAdvancePaymentContact contact = getAdvancePaymentContact(trip.getContractorId());
        if (contact != null && contact.getEmail() != null) {
            MessageDto messageDto = getMessageDto(tripRequestAdvancePayment, contact, paymentContractor);
            if (contact.getEmail() != null) {
                notificationService.sendEmail(messageDto);
            }
            if (contact.getPhone() != null) {
                notificationService.sendSms(messageDto);
            }
        }
// TODO заполнить        push_button_at:
//        description: "Дата и время нажатия на кнопку"
//        first_loading_address:
//        description: "Адрес первой погрузки"
//        last_unloading_address:
//        description: "Адрес последней разгрузки"
        tripRequestAdvancePaymentRepository.save(tripRequestAdvancePayment);
        return new ResponseEntity<>(HttpStatus.OK);

    }

    @Override
    public ResponseEntity<Resource> downloadAvanceRequestTemplate(String tripNum) {
        StringBuilder url = new StringBuilder();
        ResponseEntity<Resource> response;
        url.append("https://reports.oboz.com/reportserver/reportserver/httpauthexport?key=avance_request&user=bertathar&apikey=nzybc16h&p_trip_num=");
        TripRequestAdvancePayment tripRequestAdvancePayment = tripRequestAdvancePaymentRepository.findRequestAdvancePaymentByTripNum(tripNum);
        if (tripRequestAdvancePayment != null) {
            url.append(tripNum)
                .append("&p_avance_sum=")
                .append(tripRequestAdvancePayment.getAdvancePaymentSum().toString())
                .append("&p_avance_comission=")
                .append(tripRequestAdvancePayment.getRegistrationFee().toString())
                .append("&format=PDF");
            response = getResourceResponseEntity(url.toString(), new HttpHeaders());
            if (response != null) {
                return response;
            }
            log.error("server {} returned bad response", url);
        }
        return null;
    }


    @Override
    public ResponseEntity<Resource> downloadAvanseRequest(String tripNum) {
        String uuidFile = tripRequestAdvancePaymentRepository.findRequestAdvancePaymentByTripNum(tripNum).getUuidAdvanceApplicationFile();
        String url = "https://preprod.oboz.online/api/bstore/" + uuidFile;
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJtZWwxMDZWemFlZGpmU3ctUVRtblVLa3ZuQUlfdXB3TGdRZHY4Vm85SEk4In0.eyJqdGkiOiJmMGY4OTBmMi03OTZiLTQwMDEtYjg2Yi0zY2YzMGM1NTRhYWQiLCJleHAiOjE2MTU0NTkzNjgsIm5iZiI6MCwiaWF0IjoxNTgzOTIzMzY4LCJpc3MiOiJodHRwczovL3ByZXByb2Qub2Jvei5vbmxpbmUvYXV0aC9yZWFsbXMvbWFzdGVyIiwiYXVkIjoiZWxwIiwic3ViIjoiZjpmY2MwMzM2Yy1lNmYyLTRlZTctYjllYi1jMjU2NDY3M2IwMzY6NDI2MjciLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJlbHAiLCJhdXRoX3RpbWUiOjAsInNlc3Npb25fc3RhdGUiOiI3ZTY1NGMzYi04OTMwLTQwMDctOGQ2Zi01N2JlYzQyNzU1OTAiLCJhY3IiOiIxIiwiY2xpZW50X3Nlc3Npb24iOiJhYTRlZjg4ZC02MjYxLTRkZTItYWQ2MS1mNDQ4OTg3MWJjOGMiLCJhbGxvd2VkLW9yaWdpbnMiOlsiKiJdLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsiY3JlYXRlLXJlYWxtIiwiY2FycmllciIsImVscC1hZG1pbiIsInNlbmRlciIsImFkbWluIiwidW1hX2F1dGhvcml6YXRpb24iLCJkaXNwYXRjaGVyIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsiZHJpdmVyLWFwcC1yZWFsbSI6eyJyb2xlcyI6WyJ2aWV3LWlkZW50aXR5LXByb3ZpZGVycyIsInZpZXctcmVhbG0iLCJtYW5hZ2UtaWRlbnRpdHktcHJvdmlkZXJzIiwiaW1wZXJzb25hdGlvbiIsImNyZWF0ZS1jbGllbnQiLCJtYW5hZ2UtdXNlcnMiLCJ2aWV3LWF1dGhvcml6YXRpb24iLCJtYW5hZ2UtZXZlbnRzIiwibWFuYWdlLXJlYWxtIiwidmlldy1ldmVudHMiLCJ2aWV3LXVzZXJzIiwidmlldy1jbGllbnRzIiwibWFuYWdlLWF1dGhvcml6YXRpb24iLCJtYW5hZ2UtY2xpZW50cyJdfSwiZHJpdmVyLWFwcC1yZWdpc3RyYXRpb24tcmVhbG0iOnsicm9sZXMiOlsidmlldy1yZWFsbSIsInZpZXctaWRlbnRpdHktcHJvdmlkZXJzIiwibWFuYWdlLWlkZW50aXR5LXByb3ZpZGVycyIsImltcGVyc29uYXRpb24iLCJjcmVhdGUtY2xpZW50IiwibWFuYWdlLXVzZXJzIiwidmlldy1hdXRob3JpemF0aW9uIiwibWFuYWdlLWV2ZW50cyIsIm1hbmFnZS1yZWFsbSIsInZpZXctZXZlbnRzIiwidmlldy11c2VycyIsInZpZXctY2xpZW50cyIsIm1hbmFnZS1hdXRob3JpemF0aW9uIiwibWFuYWdlLWNsaWVudHMiXX0sImRtcy1yZWFsbSI6eyJyb2xlcyI6WyJ2aWV3LWlkZW50aXR5LXByb3ZpZGVycyIsInZpZXctcmVhbG0iLCJtYW5hZ2UtaWRlbnRpdHktcHJvdmlkZXJzIiwiaW1wZXJzb25hdGlvbiIsImNyZWF0ZS1jbGllbnQiLCJtYW5hZ2UtdXNlcnMiLCJ2aWV3LWF1dGhvcml6YXRpb24iLCJtYW5hZ2UtZXZlbnRzIiwibWFuYWdlLXJlYWxtIiwidmlldy1ldmVudHMiLCJ2aWV3LXVzZXJzIiwidmlldy1jbGllbnRzIiwibWFuYWdlLWF1dGhvcml6YXRpb24iLCJtYW5hZ2UtY2xpZW50cyJdfSwicHVibGljLXJlYWxtIjp7InJvbGVzIjpbInZpZXctcmVhbG0iLCJ2aWV3LWlkZW50aXR5LXByb3ZpZGVycyIsIm1hbmFnZS1pZGVudGl0eS1wcm92aWRlcnMiLCJpbXBlcnNvbmF0aW9uIiwiY3JlYXRlLWNsaWVudCIsIm1hbmFnZS11c2VycyIsInZpZXctYXV0aG9yaXphdGlvbiIsIm1hbmFnZS1ldmVudHMiLCJtYW5hZ2UtcmVhbG0iLCJ2aWV3LWV2ZW50cyIsInZpZXctdXNlcnMiLCJ2aWV3LWNsaWVudHMiLCJtYW5hZ2UtYXV0aG9yaXphdGlvbiIsIm1hbmFnZS1jbGllbnRzIl19LCJtYXN0ZXItcmVhbG0iOnsicm9sZXMiOlsidmlldy1yZWFsbSIsInZpZXctaWRlbnRpdHktcHJvdmlkZXJzIiwibWFuYWdlLWlkZW50aXR5LXByb3ZpZGVycyIsImltcGVyc29uYXRpb24iLCJjcmVhdGUtY2xpZW50IiwibWFuYWdlLXVzZXJzIiwidmlldy1hdXRob3JpemF0aW9uIiwibWFuYWdlLWV2ZW50cyIsIm1hbmFnZS1yZWFsbSIsInZpZXctZXZlbnRzIiwidmlldy11c2VycyIsInZpZXctY2xpZW50cyIsIm1hbmFnZS1hdXRob3JpemF0aW9uIiwibWFuYWdlLWNsaWVudHMiXX0sImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sImNvbnRyYWN0b3IiOnsiaWQiOiI2MDkifSwicGVyc29uIjp7ImlkIjoiNDI2MjcifSwibmFtZSI6ItCT0LXQvdC90LDQtNC40Lkg0JzQsNC60LDRgNC-0LIiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiIwMDAwMDAxMDA1IiwiZ2l2ZW5fbmFtZSI6ItCT0LXQvdC90LDQtNC40LkiLCJmYW1pbHlfbmFtZSI6ItCc0LDQutCw0YDQvtCyIiwiZW1haWwiOiJnbWFrYXJvdkBvYm96LmNvbSJ9.Mm9jTH1B-n-awgofxTy7aNJLhu83J4-mG98nhWkrzBq8oGCQbEzAranSO1r_LsgARg8-bqb0ek4AC9Fa_CCwNNEPlCQB1ufOA3CVDNttm1o5I2HuYk1jlcmQBInePxBqQJqkAyeAYVkwn5AT_21Rv0VJRkH9VkXPeDV9vseBp5P_N1bYjoWLRlhPwqjqwSnzpCukduFzTerws5ngf54H1CVVTaBR9FS7w_y3ql5RSeECE-Z3_4-lkSgC7WjzPxEUfxz-1I1f7fqQn9NZKW4FdFOdVUoUW-tOqVYWfJ_erDWcRvt0VXw-1iKX_r7g50-ACV0VYFscPzEziSKihtw8sA");
        ResponseEntity<Resource> response = getResourceResponseEntity(url, headers);
        if (response != null) return response;
        log.error("server {} returned bad response", url);
        return null;
    }

    @Override
    public ResponseEntity<Resource> downloadRequest(String tripNum) {
        String uuidFile = tripRequestAdvancePaymentRepository.findRequestAdvancePaymentByTripNum(tripNum).getUuidContractApplicationFile();
        String url = "https://preprod.oboz.online/api/bstore/" + uuidFile;
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJtZWwxMDZWemFlZGpmU3ctUVRtblVLa3ZuQUlfdXB3TGdRZHY4Vm85SEk4In0.eyJqdGkiOiJmMGY4OTBmMi03OTZiLTQwMDEtYjg2Yi0zY2YzMGM1NTRhYWQiLCJleHAiOjE2MTU0NTkzNjgsIm5iZiI6MCwiaWF0IjoxNTgzOTIzMzY4LCJpc3MiOiJodHRwczovL3ByZXByb2Qub2Jvei5vbmxpbmUvYXV0aC9yZWFsbXMvbWFzdGVyIiwiYXVkIjoiZWxwIiwic3ViIjoiZjpmY2MwMzM2Yy1lNmYyLTRlZTctYjllYi1jMjU2NDY3M2IwMzY6NDI2MjciLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJlbHAiLCJhdXRoX3RpbWUiOjAsInNlc3Npb25fc3RhdGUiOiI3ZTY1NGMzYi04OTMwLTQwMDctOGQ2Zi01N2JlYzQyNzU1OTAiLCJhY3IiOiIxIiwiY2xpZW50X3Nlc3Npb24iOiJhYTRlZjg4ZC02MjYxLTRkZTItYWQ2MS1mNDQ4OTg3MWJjOGMiLCJhbGxvd2VkLW9yaWdpbnMiOlsiKiJdLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsiY3JlYXRlLXJlYWxtIiwiY2FycmllciIsImVscC1hZG1pbiIsInNlbmRlciIsImFkbWluIiwidW1hX2F1dGhvcml6YXRpb24iLCJkaXNwYXRjaGVyIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsiZHJpdmVyLWFwcC1yZWFsbSI6eyJyb2xlcyI6WyJ2aWV3LWlkZW50aXR5LXByb3ZpZGVycyIsInZpZXctcmVhbG0iLCJtYW5hZ2UtaWRlbnRpdHktcHJvdmlkZXJzIiwiaW1wZXJzb25hdGlvbiIsImNyZWF0ZS1jbGllbnQiLCJtYW5hZ2UtdXNlcnMiLCJ2aWV3LWF1dGhvcml6YXRpb24iLCJtYW5hZ2UtZXZlbnRzIiwibWFuYWdlLXJlYWxtIiwidmlldy1ldmVudHMiLCJ2aWV3LXVzZXJzIiwidmlldy1jbGllbnRzIiwibWFuYWdlLWF1dGhvcml6YXRpb24iLCJtYW5hZ2UtY2xpZW50cyJdfSwiZHJpdmVyLWFwcC1yZWdpc3RyYXRpb24tcmVhbG0iOnsicm9sZXMiOlsidmlldy1yZWFsbSIsInZpZXctaWRlbnRpdHktcHJvdmlkZXJzIiwibWFuYWdlLWlkZW50aXR5LXByb3ZpZGVycyIsImltcGVyc29uYXRpb24iLCJjcmVhdGUtY2xpZW50IiwibWFuYWdlLXVzZXJzIiwidmlldy1hdXRob3JpemF0aW9uIiwibWFuYWdlLWV2ZW50cyIsIm1hbmFnZS1yZWFsbSIsInZpZXctZXZlbnRzIiwidmlldy11c2VycyIsInZpZXctY2xpZW50cyIsIm1hbmFnZS1hdXRob3JpemF0aW9uIiwibWFuYWdlLWNsaWVudHMiXX0sImRtcy1yZWFsbSI6eyJyb2xlcyI6WyJ2aWV3LWlkZW50aXR5LXByb3ZpZGVycyIsInZpZXctcmVhbG0iLCJtYW5hZ2UtaWRlbnRpdHktcHJvdmlkZXJzIiwiaW1wZXJzb25hdGlvbiIsImNyZWF0ZS1jbGllbnQiLCJtYW5hZ2UtdXNlcnMiLCJ2aWV3LWF1dGhvcml6YXRpb24iLCJtYW5hZ2UtZXZlbnRzIiwibWFuYWdlLXJlYWxtIiwidmlldy1ldmVudHMiLCJ2aWV3LXVzZXJzIiwidmlldy1jbGllbnRzIiwibWFuYWdlLWF1dGhvcml6YXRpb24iLCJtYW5hZ2UtY2xpZW50cyJdfSwicHVibGljLXJlYWxtIjp7InJvbGVzIjpbInZpZXctcmVhbG0iLCJ2aWV3LWlkZW50aXR5LXByb3ZpZGVycyIsIm1hbmFnZS1pZGVudGl0eS1wcm92aWRlcnMiLCJpbXBlcnNvbmF0aW9uIiwiY3JlYXRlLWNsaWVudCIsIm1hbmFnZS11c2VycyIsInZpZXctYXV0aG9yaXphdGlvbiIsIm1hbmFnZS1ldmVudHMiLCJtYW5hZ2UtcmVhbG0iLCJ2aWV3LWV2ZW50cyIsInZpZXctdXNlcnMiLCJ2aWV3LWNsaWVudHMiLCJtYW5hZ2UtYXV0aG9yaXphdGlvbiIsIm1hbmFnZS1jbGllbnRzIl19LCJtYXN0ZXItcmVhbG0iOnsicm9sZXMiOlsidmlldy1yZWFsbSIsInZpZXctaWRlbnRpdHktcHJvdmlkZXJzIiwibWFuYWdlLWlkZW50aXR5LXByb3ZpZGVycyIsImltcGVyc29uYXRpb24iLCJjcmVhdGUtY2xpZW50IiwibWFuYWdlLXVzZXJzIiwidmlldy1hdXRob3JpemF0aW9uIiwibWFuYWdlLWV2ZW50cyIsIm1hbmFnZS1yZWFsbSIsInZpZXctZXZlbnRzIiwidmlldy11c2VycyIsInZpZXctY2xpZW50cyIsIm1hbmFnZS1hdXRob3JpemF0aW9uIiwibWFuYWdlLWNsaWVudHMiXX0sImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sImNvbnRyYWN0b3IiOnsiaWQiOiI2MDkifSwicGVyc29uIjp7ImlkIjoiNDI2MjcifSwibmFtZSI6ItCT0LXQvdC90LDQtNC40Lkg0JzQsNC60LDRgNC-0LIiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiIwMDAwMDAxMDA1IiwiZ2l2ZW5fbmFtZSI6ItCT0LXQvdC90LDQtNC40LkiLCJmYW1pbHlfbmFtZSI6ItCc0LDQutCw0YDQvtCyIiwiZW1haWwiOiJnbWFrYXJvdkBvYm96LmNvbSJ9.Mm9jTH1B-n-awgofxTy7aNJLhu83J4-mG98nhWkrzBq8oGCQbEzAranSO1r_LsgARg8-bqb0ek4AC9Fa_CCwNNEPlCQB1ufOA3CVDNttm1o5I2HuYk1jlcmQBInePxBqQJqkAyeAYVkwn5AT_21Rv0VJRkH9VkXPeDV9vseBp5P_N1bYjoWLRlhPwqjqwSnzpCukduFzTerws5ngf54H1CVVTaBR9FS7w_y3ql5RSeECE-Z3_4-lkSgC7WjzPxEUfxz-1I1f7fqQn9NZKW4FdFOdVUoUW-tOqVYWfJ_erDWcRvt0VXw-1iKX_r7g50-ACV0VYFscPzEziSKihtw8sA");
        ResponseEntity<Resource> response = getResourceResponseEntity(url, headers);
        if (response != null) return response;
        log.error("server {} returned bad response", url);
        return null;
    }

    @Override
    public ResponseEntity<Resource> downloadAvanceRequestTemplateForCarrier(String tripNum) {
        log.info("downloadAvanceRequestTemplate success");
        return downloadAvanceRequestTemplate(tripNum);
    }

    @Override
    public ResponseEntity<Void> uploadRequestAdvance(MultipartFile filename, String tripNum) {
        String url = "https://preprod.oboz.online/api/bstore/pdf/";// applicationProperties.getBStoreUrl();
//        ResponseEntity<Resource> response;
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "multipart/form-data;");
        final String bearer = "Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJtZWwxMDZWemFlZGpmU3ctUVRtblVLa3ZuQUlfdXB3TGdRZHY4Vm85SEk4In0.eyJqdGkiOiJmMGY4OTBmMi03OTZiLTQwMDEtYjg2Yi0zY2YzMGM1NTRhYWQiLCJleHAiOjE2MTU0NTkzNjgsIm5iZiI6MCwiaWF0IjoxNTgzOTIzMzY4LCJpc3MiOiJodHRwczovL3ByZXByb2Qub2Jvei5vbmxpbmUvYXV0aC9yZWFsbXMvbWFzdGVyIiwiYXVkIjoiZWxwIiwic3ViIjoiZjpmY2MwMzM2Yy1lNmYyLTRlZTctYjllYi1jMjU2NDY3M2IwMzY6NDI2MjciLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJlbHAiLCJhdXRoX3RpbWUiOjAsInNlc3Npb25fc3RhdGUiOiI3ZTY1NGMzYi04OTMwLTQwMDctOGQ2Zi01N2JlYzQyNzU1OTAiLCJhY3IiOiIxIiwiY2xpZW50X3Nlc3Npb24iOiJhYTRlZjg4ZC02MjYxLTRkZTItYWQ2MS1mNDQ4OTg3MWJjOGMiLCJhbGxvd2VkLW9yaWdpbnMiOlsiKiJdLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsiY3JlYXRlLXJlYWxtIiwiY2FycmllciIsImVscC1hZG1pbiIsInNlbmRlciIsImFkbWluIiwidW1hX2F1dGhvcml6YXRpb24iLCJkaXNwYXRjaGVyIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsiZHJpdmVyLWFwcC1yZWFsbSI6eyJyb2xlcyI6WyJ2aWV3LWlkZW50aXR5LXByb3ZpZGVycyIsInZpZXctcmVhbG0iLCJtYW5hZ2UtaWRlbnRpdHktcHJvdmlkZXJzIiwiaW1wZXJzb25hdGlvbiIsImNyZWF0ZS1jbGllbnQiLCJtYW5hZ2UtdXNlcnMiLCJ2aWV3LWF1dGhvcml6YXRpb24iLCJtYW5hZ2UtZXZlbnRzIiwibWFuYWdlLXJlYWxtIiwidmlldy1ldmVudHMiLCJ2aWV3LXVzZXJzIiwidmlldy1jbGllbnRzIiwibWFuYWdlLWF1dGhvcml6YXRpb24iLCJtYW5hZ2UtY2xpZW50cyJdfSwiZHJpdmVyLWFwcC1yZWdpc3RyYXRpb24tcmVhbG0iOnsicm9sZXMiOlsidmlldy1yZWFsbSIsInZpZXctaWRlbnRpdHktcHJvdmlkZXJzIiwibWFuYWdlLWlkZW50aXR5LXByb3ZpZGVycyIsImltcGVyc29uYXRpb24iLCJjcmVhdGUtY2xpZW50IiwibWFuYWdlLXVzZXJzIiwidmlldy1hdXRob3JpemF0aW9uIiwibWFuYWdlLWV2ZW50cyIsIm1hbmFnZS1yZWFsbSIsInZpZXctZXZlbnRzIiwidmlldy11c2VycyIsInZpZXctY2xpZW50cyIsIm1hbmFnZS1hdXRob3JpemF0aW9uIiwibWFuYWdlLWNsaWVudHMiXX0sImRtcy1yZWFsbSI6eyJyb2xlcyI6WyJ2aWV3LWlkZW50aXR5LXByb3ZpZGVycyIsInZpZXctcmVhbG0iLCJtYW5hZ2UtaWRlbnRpdHktcHJvdmlkZXJzIiwiaW1wZXJzb25hdGlvbiIsImNyZWF0ZS1jbGllbnQiLCJtYW5hZ2UtdXNlcnMiLCJ2aWV3LWF1dGhvcml6YXRpb24iLCJtYW5hZ2UtZXZlbnRzIiwibWFuYWdlLXJlYWxtIiwidmlldy1ldmVudHMiLCJ2aWV3LXVzZXJzIiwidmlldy1jbGllbnRzIiwibWFuYWdlLWF1dGhvcml6YXRpb24iLCJtYW5hZ2UtY2xpZW50cyJdfSwicHVibGljLXJlYWxtIjp7InJvbGVzIjpbInZpZXctcmVhbG0iLCJ2aWV3LWlkZW50aXR5LXByb3ZpZGVycyIsIm1hbmFnZS1pZGVudGl0eS1wcm92aWRlcnMiLCJpbXBlcnNvbmF0aW9uIiwiY3JlYXRlLWNsaWVudCIsIm1hbmFnZS11c2VycyIsInZpZXctYXV0aG9yaXphdGlvbiIsIm1hbmFnZS1ldmVudHMiLCJtYW5hZ2UtcmVhbG0iLCJ2aWV3LWV2ZW50cyIsInZpZXctdXNlcnMiLCJ2aWV3LWNsaWVudHMiLCJtYW5hZ2UtYXV0aG9yaXphdGlvbiIsIm1hbmFnZS1jbGllbnRzIl19LCJtYXN0ZXItcmVhbG0iOnsicm9sZXMiOlsidmlldy1yZWFsbSIsInZpZXctaWRlbnRpdHktcHJvdmlkZXJzIiwibWFuYWdlLWlkZW50aXR5LXByb3ZpZGVycyIsImltcGVyc29uYXRpb24iLCJjcmVhdGUtY2xpZW50IiwibWFuYWdlLXVzZXJzIiwidmlldy1hdXRob3JpemF0aW9uIiwibWFuYWdlLWV2ZW50cyIsIm1hbmFnZS1yZWFsbSIsInZpZXctZXZlbnRzIiwidmlldy11c2VycyIsInZpZXctY2xpZW50cyIsIm1hbmFnZS1hdXRob3JpemF0aW9uIiwibWFuYWdlLWNsaWVudHMiXX0sImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sImNvbnRyYWN0b3IiOnsiaWQiOiI2MDkifSwicGVyc29uIjp7ImlkIjoiNDI2MjcifSwibmFtZSI6ItCT0LXQvdC90LDQtNC40Lkg0JzQsNC60LDRgNC-0LIiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiIwMDAwMDAxMDA1IiwiZ2l2ZW5fbmFtZSI6ItCT0LXQvdC90LDQtNC40LkiLCJmYW1pbHlfbmFtZSI6ItCc0LDQutCw0YDQvtCyIiwiZW1haWwiOiJnbWFrYXJvdkBvYm96LmNvbSJ9.Mm9jTH1B-n-awgofxTy7aNJLhu83J4-mG98nhWkrzBq8oGCQbEzAranSO1r_LsgARg8-bqb0ek4AC9Fa_CCwNNEPlCQB1ufOA3CVDNttm1o5I2HuYk1jlcmQBInePxBqQJqkAyeAYVkwn5AT_21Rv0VJRkH9VkXPeDV9vseBp5P_N1bYjoWLRlhPwqjqwSnzpCukduFzTerws5ngf54H1CVVTaBR9FS7w_y3ql5RSeECE-Z3_4-lkSgC7WjzPxEUfxz-1I1f7fqQn9NZKW4FdFOdVUoUW-tOqVYWfJ_erDWcRvt0VXw-1iKX_r7g50-ACV0VYFscPzEziSKihtw8sA";
        headers.add("Authorization", bearer);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", filename.getResource());
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = new RestTemplate().exchange(url, HttpMethod.POST, request, String.class);
        if (response.getStatusCode().value() == 200) {
            try {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                String fileUuid = jsonNode.get("file_uuid").asText();
                return saveTripDocuments(tripNum, fileUuid, bearer);
            } catch (IOException e) {
                log.error("", e);
            }
        }
        Error error = new Error();
        log.error("uploadRequestAvance fail. http code {}", response.getStatusCode().value());
        error.setErrorMessage("uploadRequestAvance fail. http code " + response.getStatusCode().value());
        throw new BusinessLogicException(HttpStatus.UNPROCESSABLE_ENTITY, error);
    }

    @Override
    public ResponseEntity<Void> uploadRequestAvanceForCarrier(MultipartFile filename, String trip_num) {

        return null;
    }

    @Override
    public ResponseEntity<Void> addContactCarrier(CarrierContactDTO carrierContactDTO) {
        Optional<ContractorAdvancePaymentContact> contractorAdvancePaymentContact =
            contractorAdvancePaymentContactRepository.findContractorAdvancePaymentContact(carrierContactDTO.getContractorId());
        if (contractorAdvancePaymentContact.isPresent()) {
            getVoidResponseEntity("ContractorAdvancePaymentContact is present");
        }
        final ContractorAdvancePaymentContact entity = new ContractorAdvancePaymentContact();
        entity.setFullName(carrierContactDTO.getFullName());
        entity.setContractorId(carrierContactDTO.getContractorId());
        entity.setPhone(carrierContactDTO.getPhoneNumber());
        entity.setEmail(carrierContactDTO.getEmail());
        contractorAdvancePaymentContactRepository.save(entity);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> updateLoadingComplete(Long id, Boolean loadingComplete) {
        final TripRequestAdvancePayment entity = getTripRequestAdvancePayment(id);
        entity.setLoadingComplete(loadingComplete);
        tripRequestAdvancePaymentRepository.save(entity);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> updateContactCarrier(CarrierContactDTO carrierContactDTO) {
        Optional<ContractorAdvancePaymentContact> contractorAdvancePaymentContact =
            contractorAdvancePaymentContactRepository.findContractorAdvancePaymentContact(carrierContactDTO.getContractorId());
        final ContractorAdvancePaymentContact entity = contractorAdvancePaymentContact.orElseThrow(() -> {
                Error error = new Error();
                error.setErrorMessage("ContractorAdvancePaymentContact not found");
                return new BusinessLogicException(HttpStatus.UNPROCESSABLE_ENTITY, error);
            }
        );
        entity.setFullName(carrierContactDTO.getFullName());
        entity.setContractorId(carrierContactDTO.getContractorId());
        entity.setPhone(carrierContactDTO.getPhoneNumber());
        entity.setEmail(carrierContactDTO.getEmail());
        contractorAdvancePaymentContactRepository.save(entity);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> cancelAdvancePayment(Long id, String cancelAdvanceComment) {
        final TripRequestAdvancePayment entity = getTripRequestAdvancePayment(id);
        entity.setCancelAdvanceComment(cancelAdvanceComment);
        entity.setCancelAdvance(true);
        tripRequestAdvancePaymentRepository.save(entity);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CarrierContactDTO> getContactCarrier(Long contractorId) {
        CarrierContactDTO carrierContactDTO;
        ContractorAdvancePaymentContact contact = contractorAdvancePaymentContactRepository.findContractorAdvancePaymentContact(contractorId)
            .orElseGet(ContractorAdvancePaymentContact::new);
        carrierContactDTO = getCarrierContactDTO(contact);
        return new ResponseEntity<>(carrierContactDTO, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<FrontAdvancePaymentResponse> searchAdvancePaymentRequestByUuid(UUID uuid) {
// TODO  при отключенной ссылки возвращать 422 ошибка с сообщением
// TODO  в случае если кабинет не заблокирован, но по uuid ничего не найдено - тоже 422 ошибка
//        TODO  просетить в ответе значения:
//            .firstLoadingAddress(getFirstLoadingAddress)
//            .lastUnloadingAddress(getLastUnLoadingAddress);
        return null;
    }

    private ResponseEntity<Void> getVoidResponseEntity(String s) {
        Error error = new Error();
        error.setErrorMessage(s);
        throw new BusinessLogicException(HttpStatus.UNPROCESSABLE_ENTITY, error);
    }

    private MessageDto getMessageDto(TripRequestAdvancePayment tripRequestAdvancePayment,
                                     ContractorAdvancePaymentContact contact,
                                     String paymentContractorFullName) {
        MessageDto messageDto = new MessageDto();
        messageDto.setAdvancePaymentSum(tripRequestAdvancePayment.getAdvancePaymentSum());
        messageDto.setContractorName(paymentContractorFullName);
        messageDto.setEmail(contact.getEmail());
        messageDto.setPhone(contact.getPhone());
//        TODO add link from uuid trip + link to front
        messageDto.setLKLink("link");
        messageDto.setTripNum(getMotorTrip(tripRequestAdvancePayment.getTripId()).getNum());
        return messageDto;
    }

    private void setButtonAccessGetAdvance(Trip trip,
                                           Contractor contractor,
                                           TripRequestAdvancePayment tripRequestAdvancePayment,
                                           IsAdvancedRequestResponse isAdvancedRequestResponse) {
        final Boolean isAutoAdvancePayment = contractor.getIsAutoAdvancePayment();
        Map<String, String> downloadedDocuments = autoAdvancedService.findTripRequestDocs(trip);
        if (tripRequestAdvancePayment == null &&
            !isAutoAdvancePayment &&
            !downloadedDocuments.isEmpty()
        ) {
            isAdvancedRequestResponse.setIsButtonActive(true);
        } else if (tripRequestAdvancePayment != null && (
            tripRequestAdvancePayment.getCancelAdvance() ||
                isAutoAdvancePayment ||
                tripRequestAdvancePayment.getIsUnfSend() ||
                !trip.getDriverId().equals(tripRequestAdvancePayment.getDriverId()))) {
            isAdvancedRequestResponse.setIsButtonActive(false);
        }
    }

    private void getFrontAdvancePaymentResponse(TripRequestAdvancePayment reс,
                                                FrontAdvancePaymentResponse frontAdvancePaymentResponse,
                                                ContractorAdvancePaymentContact contractorAdvancePaymentContact,
                                                Contractor contractor,
                                                String contractorPaymentName,
                                                Trip trip) {
        frontAdvancePaymentResponse
            .id(reс.getId())
            .tripId(reс.getTripId())
            .tripNum(trip.getNum())
            .tripTypeCode(reс.getTripTypeCode())
            .createdAt(trip.getCreatedAt())
            .reqCreatedAt(reс.getCreatedAt())
            .contractorId(reс.getContractorId())
            .contractorName(contractor.getFullName())
            .contactFio(contractorAdvancePaymentContact.getFullName())
            .contactPhone(contractorAdvancePaymentContact.getPhone())
            .contactEmail(contractorAdvancePaymentContact.getEmail())
            .paymentContractor(contractorPaymentName)
            .isAutomationRequest(reс.getIsAutomationRequest())
            .tripCostWithVat(reс.getTripCost())
            .advancePaymentSum(reс.getAdvancePaymentSum())
            .registrationFee(reс.getRegistrationFee())
            //проставляется вручную сотрудниками авансирования
            .loadingComplete(reс.getLoadingComplete())
            .urlContractApplication(reс.getUuidContractApplicationFile())
            .urlAdvanceApplication(reс.getUuidAdvanceApplicationFile())
            .is1CSendAllowed(reс.getIs1CSendAllowed())
            .isUnfSend(reс.getIsUnfSend())
            .isPaid(reс.getIsPaid())
            .paidAt(reс.getPaidAt())
            .comment(reс.getComment())
            .cancelAdvance(reс.getCancelAdvance())
            .cancelAdvanceComment(reс.getCancelAdvanceComment())
            .authorId(reс.getAuthorId())
            .pageCarrierUrlIsAccess(reс.getPageCarrierUrlIsAccess());
//        TODO  просетить  значения:
//            .firstLoadingAddress(getFirstLoadingAddress)
//            .lastUnloadingAddress(getLastUnLoadingAddress);

    }

    private ResponseEntity<Void> saveTripDocuments(String tripNum, String fileUuid, String bearer) {
        String url = "https://preprod.oboz.online/api/orders/dispatcher/orders/%d/trips/%d/documents";// applicationProperties.getBaseUrl();
        Trip trip = tripRepository.getTripByNum(tripNum).orElseThrow(() -> {
                Error error = new Error();
                error.setErrorMessage("trip not found");
                return new BusinessLogicException(HttpStatus.UNPROCESSABLE_ENTITY, error);
            }
        );
        url = String.format(url, trip.getOrderId(), trip.getId());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", bearer);
        final String s = "{\"trip_document\":{\"id\":null,\"file_id\":\"%s\",\"document_type_code\":\"trip_request\",\"name\":\"Договор-заявка\"}}";
        HttpEntity<String> request = new HttpEntity<>(String.format(s, fileUuid), headers);
        ResponseEntity<Void> response = new RestTemplate().exchange(url, HttpMethod.POST, request, Void.class);
        if (response.getStatusCode().value() == 200) {
            log.info("saveTripDocuments ok");
        } else {
            log.error("saveTripDocuments fail. http code {}", response.getStatusCode().value());
        }
        return response;
    }

    private ResponseEntity<Resource> getResourceResponseEntity(String url, HttpHeaders headers) {
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

    private Boolean isDownloadAllDocuments(Trip trip) {
//        использовать только в confirm
        Map<String, String> fileRequestUuidMap = autoAdvancedService.findTripRequestDocs(trip);
        Map<String, String> fileAdvanceRequestUuidMap = autoAdvancedService.findAdvanceRequestDocs(trip);
        String requestFileUuid = Optional.ofNullable(fileRequestUuidMap.get("request")).orElse(fileRequestUuidMap.get("trip_request"));
        String advanceRequestFileUuid = fileAdvanceRequestUuidMap.get("assignment_advance_request");
//         log.info("Не загружены документы заявка / договор заявка  ");
        return requestFileUuid != null && advanceRequestFileUuid.isEmpty();
    }

    private TripRequestAdvancePayment getRequestAdvancePaymentByTrip(Long id) {
        return tripRequestAdvancePaymentRepository
            .findTripRequestAdvancePayment(id).orElseThrow(() -> {
                    Error error = new Error();
                    error.setErrorMessage("TripRequestAdvancePayment not found for id: " + id);
                    return new BusinessLogicException(HttpStatus.UNPROCESSABLE_ENTITY, error);
                }
            );
    }

    private Trip getMotorTrip(Long tripId) {
        return tripRepository.getMotorTrip(tripId).orElseThrow(() -> {
                Error error = new Error();
                error.setErrorMessage("trip not found");
                return new BusinessLogicException(HttpStatus.UNPROCESSABLE_ENTITY, error);
            }
        );
    }

    private TripRequestAdvancePayment getTripRequestAdvancePayment(Long tripId,
                                                                   Contractor contractor,
                                                                   Double tripCostWithNds,
                                                                   AdvancePaymentCost advancePaymentCost,
                                                                   boolean isUnfSend,
                                                                   Trip trip) {
        TripRequestAdvancePayment tripRequestAdvancePayment;
        tripRequestAdvancePayment = new TripRequestAdvancePayment();
        tripRequestAdvancePayment.setAuthorId(SecurityUtils.getAuthPersonId());
        tripRequestAdvancePayment.setTripId(advancePaymentCost.getId());
        tripRequestAdvancePayment.setIsUnfSend(isUnfSend);
        tripRequestAdvancePayment.setTripCost(tripCostWithNds);
        tripRequestAdvancePayment.setAdvancePaymentSum(advancePaymentCost.getAdvancePaymentSum());
        tripRequestAdvancePayment.setRegistrationFee(advancePaymentCost.getRegistrationFee());
        tripRequestAdvancePayment.setCancelAdvance(false);
        tripRequestAdvancePayment.setContractorId(trip.getContractorId());
        tripRequestAdvancePayment.setDriverId(trip.getDriverId());
        tripRequestAdvancePayment.setCreatedAt(OffsetDateTime.now());
        tripRequestAdvancePayment.setTripId(tripId);
        tripRequestAdvancePayment.setTripTypeCode(trip.getTripTypeCode());
        tripRequestAdvancePayment.setLoadingComplete(false);
        tripRequestAdvancePayment.setPaymentContractorId(trip.getPaymentContractorId());
        tripRequestAdvancePayment.setPageCarrierUrlIsAccess(true);
        tripRequestAdvancePayment.setIsPaid(false);
        tripRequestAdvancePayment.setPaidAt(OffsetDateTime.now());
        tripRequestAdvancePayment.setCancelAdvanceComment("");
        tripRequestAdvancePayment.setIsAutomationRequest(contractor.getIsAutoAdvancePayment());

        return tripRequestAdvancePayment;
    }

//    TODO добавить поля и их заполнение ()
//    TODO заполнение таблицы исключений тип заказа клиента  берем из текущего заказа

    Boolean confirmRequestToUnf() {
//        TODO : реализовать вызов метода Паши
        return true;
    }

    private TripRequestAdvancePayment getTripRequestAdvancePayment(Long id) {
        Optional<TripRequestAdvancePayment> tripRequestAdvancePayment = tripRequestAdvancePaymentRepository
            .findTripRequestAdvancePayment(id);
        return tripRequestAdvancePayment.orElseThrow(() -> {
                Error error = new Error();
                error.setErrorMessage("TripRequestAdvancePayment not found");
                return new BusinessLogicException(HttpStatus.UNPROCESSABLE_ENTITY, error);
            }
        );
    }

    private CarrierContactDTO getCarrierContactDTO(ContractorAdvancePaymentContact contractorAdvancePaymentContact) {
        CarrierContactDTO carrierContactDTO = new CarrierContactDTO();
        carrierContactDTO.setContractorId(contractorAdvancePaymentContact.getContractorId());
        carrierContactDTO.setEmail(contractorAdvancePaymentContact.getEmail());
        carrierContactDTO.setFullName(contractorAdvancePaymentContact.getFullName());
        carrierContactDTO.setPhoneNumber(contractorAdvancePaymentContact.getPhone());
        return carrierContactDTO;
    }

    private ContractorAdvancePaymentContact getAdvancePaymentContact(Long contractorId) {
        return contractorAdvancePaymentContactRepository
            .findContractorAdvancePaymentContact(contractorId).orElse(new ContractorAdvancePaymentContact());
    }
}
