package online.oboz.trip.trip_carrier_advance_payment_api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.*;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.*;
import online.oboz.trip.trip_carrier_advance_payment_api.service.dto.MessageDto;
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

import javax.persistence.Tuple;
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
    private final OrderRepository orderRepository;


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
                                      ApplicationProperties applicationProperties,
                                      ObjectMapper objectMapper,
                                      AdvancePaymentContactService advancePaymentContactService,
                                      OrderRepository orderRepository) {
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
        this.orderRepository = orderRepository;
    }

    @Override
    public ResponseEntity<ResponseAdvancePayment> searchAdvancePaymentRequest(Filter filter) {
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
        Trip trip = tripRepository.findById(tripRequestAdvancePayment.getTripId()).orElseThrow(() -> {
                Error error = new Error();
                error.setErrorMessage("trip not found");
                return new BusinessLogicException(HttpStatus.UNPROCESSABLE_ENTITY, error);
            }
        );
        final boolean downloadAllDocuments = isDownloadAllDocuments(trip);
        final Boolean cancelAdvance = tripRequestAdvancePayment.getCancelAdvance();
        if (downloadAllDocuments && !tripRequestAdvancePayment.getIsUnfSend() && !cancelAdvance) {
            confirmRequestToUnf();
            tripRequestAdvancePayment.setIsUnfSend(true);
            tripRequestAdvancePayment.setPageCarrierUrlIsAccess(false);
            trip.setIsAdvancedPayment(true);
            tripRepository.save(trip);
            tripRequestAdvancePaymentRepository.save(tripRequestAdvancePayment);
            final Long orderTypeId = orderRepository.findById(trip.getOrderId()).get().getOrderTypeId();
            ContractorAdvanceExclusion contractorAdvanceExclusion = contractorAdvanceExclusionRepository
                .findByContractorId(trip.getContractorId()).orElse(new ContractorAdvanceExclusion());
            //проверяем наличие записи контрактора в таблице исключений по типу заказа при  отсутствии  добавляем запись
            if (orderTypeId == contractorAdvanceExclusion.getOrderTypeId()) {
                contractorAdvanceExclusion = new ContractorAdvanceExclusion();
                contractorAdvanceExclusion.setCarrierId(trip.getContractorId());
                contractorAdvanceExclusion.setIsConfirmAdvance(false);
                contractorAdvanceExclusion.setOrderTypeId(orderTypeId);
                contractorAdvanceExclusion.setCarrierFullName(contractorRepository.findById(trip.getContractorId()).get().getFullName());
                contractorAdvanceExclusionRepository.save(contractorAdvanceExclusion);
            }
            return new ResponseEntity<>(HttpStatus.OK);
        }
        if (!downloadAllDocuments) {
            return getVoidResponseEntity("no download All Documents");
        } else if (cancelAdvance) {
            return getVoidResponseEntity("cancelAdvance is true");
        } else {
            return getVoidResponseEntity("unf already send");
        }
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
        } else {
            isAdvancedRequestResponse.setIsAdvanssed(true);

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

            isAdvancedRequestResponse.setTripTypeCode(tripRequestAdvancePayment.getTripTypeCode());
            isAdvancedRequestResponse.setCreatedAt(tripRequestAdvancePayment.getCreatedAt());
            return new ResponseEntity<>(isAdvancedRequestResponse, HttpStatus.OK);
        }
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
            false,
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
//        TODO set link from uuid trip + link to front

        tripRequestAdvancePaymentRepository.save(tripRequestAdvancePayment);
        return new ResponseEntity<>(HttpStatus.OK);
//        TODO заполнение таблицы исключений тип заказа клиента  берем из текущего заказа
    }

    @Override
    public ResponseEntity<Resource> downloadAvanceRequestTemplate(String tripNum) {
        StringBuilder url = new StringBuilder();
        ResponseEntity<Resource> response;
        url.append(applicationProperties.getReportServerUrl());
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
        String url = applicationProperties.getBStoreUrl() + uuidFile;
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJtZWwxMDZWemFlZGpmU3ctUVRtblVLa3ZuQUlfdXB3TGdRZHY4Vm85SEk4In0.eyJqdGkiOiI3MmRjZDA2NS1mMDhmLTRkZjAtYjMwMi0zNzNiYzUwZDAzNjYiLCJleHAiOjE2MTU2NDk0NjAsIm5iZiI6MCwiaWF0IjoxNTg0MTEzNDYwLCJpc3MiOiJodHRwczovL2Rldi5vYm96Lm9ubGluZS9hdXRoL3JlYWxtcy9tYXN0ZXIiLCJhdWQiOiJlbHAiLCJzdWIiOiJmOmZjYzAzMzZjLWU2ZjItNGVlNy1iOWViLWMyNTY0NjczYjAzNjo0MjYyNyIsInR5cCI6IkJlYXJlciIsImF6cCI6ImVscCIsImF1dGhfdGltZSI6MCwic2Vzc2lvbl9zdGF0ZSI6IjBjM2U4MzI4LTYxNjctNDlhYi05MWI2LWJjNGMyM2ZhZjljZSIsImFjciI6IjEiLCJjbGllbnRfc2Vzc2lvbiI6IjBmYTc4NjVmLTVmN2QtNDFhOC05NjU2LTE0MWQ0YjU3ODI1OSIsImFsbG93ZWQtb3JpZ2lucyI6WyIqIl0sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJjcmVhdGUtcmVhbG0iLCJjYXJyaWVyIiwiZWxwLWFkbWluIiwic2VuZGVyIiwiYWRtaW4iLCJ1bWFfYXV0aG9yaXphdGlvbiIsImRpc3BhdGNoZXIiXX0sInJlc291cmNlX2FjY2VzcyI6eyJkcml2ZXItYXBwLXJlYWxtIjp7InJvbGVzIjpbInZpZXctcmVhbG0iLCJ2aWV3LWlkZW50aXR5LXByb3ZpZGVycyIsIm1hbmFnZS1pZGVudGl0eS1wcm92aWRlcnMiLCJpbXBlcnNvbmF0aW9uIiwiY3JlYXRlLWNsaWVudCIsIm1hbmFnZS11c2VycyIsInZpZXctYXV0aG9yaXphdGlvbiIsIm1hbmFnZS1ldmVudHMiLCJtYW5hZ2UtcmVhbG0iLCJ2aWV3LWV2ZW50cyIsInZpZXctdXNlcnMiLCJ2aWV3LWNsaWVudHMiLCJtYW5hZ2UtYXV0aG9yaXphdGlvbiIsIm1hbmFnZS1jbGllbnRzIl19LCJkcml2ZXItYXBwLXJlZ2lzdHJhdGlvbi1yZWFsbSI6eyJyb2xlcyI6WyJ2aWV3LXJlYWxtIiwidmlldy1pZGVudGl0eS1wcm92aWRlcnMiLCJtYW5hZ2UtaWRlbnRpdHktcHJvdmlkZXJzIiwiaW1wZXJzb25hdGlvbiIsImNyZWF0ZS1jbGllbnQiLCJtYW5hZ2UtdXNlcnMiLCJ2aWV3LWF1dGhvcml6YXRpb24iLCJtYW5hZ2UtZXZlbnRzIiwibWFuYWdlLXJlYWxtIiwidmlldy1ldmVudHMiLCJ2aWV3LXVzZXJzIiwidmlldy1jbGllbnRzIiwibWFuYWdlLWF1dGhvcml6YXRpb24iLCJtYW5hZ2UtY2xpZW50cyJdfSwiZG1zLXJlYWxtIjp7InJvbGVzIjpbInZpZXctcmVhbG0iLCJ2aWV3LWlkZW50aXR5LXByb3ZpZGVycyIsIm1hbmFnZS1pZGVudGl0eS1wcm92aWRlcnMiLCJpbXBlcnNvbmF0aW9uIiwiY3JlYXRlLWNsaWVudCIsIm1hbmFnZS11c2VycyIsInZpZXctYXV0aG9yaXphdGlvbiIsIm1hbmFnZS1ldmVudHMiLCJtYW5hZ2UtcmVhbG0iLCJ2aWV3LWV2ZW50cyIsInZpZXctdXNlcnMiLCJ2aWV3LWNsaWVudHMiLCJtYW5hZ2UtYXV0aG9yaXphdGlvbiIsIm1hbmFnZS1jbGllbnRzIl19LCJwdWJsaWMtcmVhbG0iOnsicm9sZXMiOlsidmlldy1yZWFsbSIsInZpZXctaWRlbnRpdHktcHJvdmlkZXJzIiwibWFuYWdlLWlkZW50aXR5LXByb3ZpZGVycyIsImltcGVyc29uYXRpb24iLCJjcmVhdGUtY2xpZW50IiwibWFuYWdlLXVzZXJzIiwidmlldy1hdXRob3JpemF0aW9uIiwibWFuYWdlLWV2ZW50cyIsIm1hbmFnZS1yZWFsbSIsInZpZXctZXZlbnRzIiwidmlldy11c2VycyIsInZpZXctY2xpZW50cyIsIm1hbmFnZS1hdXRob3JpemF0aW9uIiwibWFuYWdlLWNsaWVudHMiXX0sIm1hc3Rlci1yZWFsbSI6eyJyb2xlcyI6WyJ2aWV3LXJlYWxtIiwidmlldy1pZGVudGl0eS1wcm92aWRlcnMiLCJtYW5hZ2UtaWRlbnRpdHktcHJvdmlkZXJzIiwiaW1wZXJzb25hdGlvbiIsImNyZWF0ZS1jbGllbnQiLCJtYW5hZ2UtdXNlcnMiLCJ2aWV3LWF1dGhvcml6YXRpb24iLCJtYW5hZ2UtZXZlbnRzIiwibWFuYWdlLXJlYWxtIiwidmlldy1ldmVudHMiLCJ2aWV3LXVzZXJzIiwidmlldy1jbGllbnRzIiwibWFuYWdlLWF1dGhvcml6YXRpb24iLCJtYW5hZ2UtY2xpZW50cyJdfSwiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwiY29udHJhY3RvciI6eyJpZCI6IjYwOSJ9LCJwZXJzb24iOnsiaWQiOiI0MjYyNyJ9LCJuYW1lIjoi0JPQtdC90L3QsNC00LjQuSDQnNCw0LrQsNGA0L7QsiIsInByZWZlcnJlZF91c2VybmFtZSI6IjAwMDAwMDEwMDUiLCJnaXZlbl9uYW1lIjoi0JPQtdC90L3QsNC00LjQuSIsImZhbWlseV9uYW1lIjoi0JzQsNC60LDRgNC-0LIiLCJlbWFpbCI6ImdtYWthcm92QG9ib3ouY29tIn0.DDmESVFTbTeZVViL_6C5PQidsbmNJ7MVgtEPHpkchF7E00gJ0lYNhvtknFK8M7S-d6_8j2_4_QQjN5VcyPY0tzIUBJgYTaIT-LgGu6NF94-G1qrWIqDxVe4btKEijMKKYcBfLNzp9v59bRDoWMpFzF78yHqmBKeSBzxBPllfMvwxbUEHtiQxqFAB7-DEu48-PRy91C1I3StemW8qyLSoDnOzkDpwawaO_5K2fK6tnOd6h4Di4S9oWYNQ9JHypyRMrstYTHMp1z9vAcUWbjFbQrUTmp-qpkYGi_eIKf__fAJdcGUrVZEboQFQZxA0mzYpCkVjHWu_P0mFnd13_AqYNw");
        ResponseEntity<Resource> response = getResourceResponseEntity(url, headers);
        if (response != null) return response;
        log.error("server {} returned bad response", url);
        return null;
    }

    @Override
    public ResponseEntity<Resource> downloadRequest(String tripNum) {
        String uuidFile = tripRequestAdvancePaymentRepository.findRequestAdvancePaymentByTripNum(tripNum).getUuidContractApplicationFile();
        String url = applicationProperties.getBStoreUrl() + uuidFile;
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJtZWwxMDZWemFlZGpmU3ctUVRtblVLa3ZuQUlfdXB3TGdRZHY4Vm85SEk4In0.eyJqdGkiOiI3MmRjZDA2NS1mMDhmLTRkZjAtYjMwMi0zNzNiYzUwZDAzNjYiLCJleHAiOjE2MTU2NDk0NjAsIm5iZiI6MCwiaWF0IjoxNTg0MTEzNDYwLCJpc3MiOiJodHRwczovL2Rldi5vYm96Lm9ubGluZS9hdXRoL3JlYWxtcy9tYXN0ZXIiLCJhdWQiOiJlbHAiLCJzdWIiOiJmOmZjYzAzMzZjLWU2ZjItNGVlNy1iOWViLWMyNTY0NjczYjAzNjo0MjYyNyIsInR5cCI6IkJlYXJlciIsImF6cCI6ImVscCIsImF1dGhfdGltZSI6MCwic2Vzc2lvbl9zdGF0ZSI6IjBjM2U4MzI4LTYxNjctNDlhYi05MWI2LWJjNGMyM2ZhZjljZSIsImFjciI6IjEiLCJjbGllbnRfc2Vzc2lvbiI6IjBmYTc4NjVmLTVmN2QtNDFhOC05NjU2LTE0MWQ0YjU3ODI1OSIsImFsbG93ZWQtb3JpZ2lucyI6WyIqIl0sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJjcmVhdGUtcmVhbG0iLCJjYXJyaWVyIiwiZWxwLWFkbWluIiwic2VuZGVyIiwiYWRtaW4iLCJ1bWFfYXV0aG9yaXphdGlvbiIsImRpc3BhdGNoZXIiXX0sInJlc291cmNlX2FjY2VzcyI6eyJkcml2ZXItYXBwLXJlYWxtIjp7InJvbGVzIjpbInZpZXctcmVhbG0iLCJ2aWV3LWlkZW50aXR5LXByb3ZpZGVycyIsIm1hbmFnZS1pZGVudGl0eS1wcm92aWRlcnMiLCJpbXBlcnNvbmF0aW9uIiwiY3JlYXRlLWNsaWVudCIsIm1hbmFnZS11c2VycyIsInZpZXctYXV0aG9yaXphdGlvbiIsIm1hbmFnZS1ldmVudHMiLCJtYW5hZ2UtcmVhbG0iLCJ2aWV3LWV2ZW50cyIsInZpZXctdXNlcnMiLCJ2aWV3LWNsaWVudHMiLCJtYW5hZ2UtYXV0aG9yaXphdGlvbiIsIm1hbmFnZS1jbGllbnRzIl19LCJkcml2ZXItYXBwLXJlZ2lzdHJhdGlvbi1yZWFsbSI6eyJyb2xlcyI6WyJ2aWV3LXJlYWxtIiwidmlldy1pZGVudGl0eS1wcm92aWRlcnMiLCJtYW5hZ2UtaWRlbnRpdHktcHJvdmlkZXJzIiwiaW1wZXJzb25hdGlvbiIsImNyZWF0ZS1jbGllbnQiLCJtYW5hZ2UtdXNlcnMiLCJ2aWV3LWF1dGhvcml6YXRpb24iLCJtYW5hZ2UtZXZlbnRzIiwibWFuYWdlLXJlYWxtIiwidmlldy1ldmVudHMiLCJ2aWV3LXVzZXJzIiwidmlldy1jbGllbnRzIiwibWFuYWdlLWF1dGhvcml6YXRpb24iLCJtYW5hZ2UtY2xpZW50cyJdfSwiZG1zLXJlYWxtIjp7InJvbGVzIjpbInZpZXctcmVhbG0iLCJ2aWV3LWlkZW50aXR5LXByb3ZpZGVycyIsIm1hbmFnZS1pZGVudGl0eS1wcm92aWRlcnMiLCJpbXBlcnNvbmF0aW9uIiwiY3JlYXRlLWNsaWVudCIsIm1hbmFnZS11c2VycyIsInZpZXctYXV0aG9yaXphdGlvbiIsIm1hbmFnZS1ldmVudHMiLCJtYW5hZ2UtcmVhbG0iLCJ2aWV3LWV2ZW50cyIsInZpZXctdXNlcnMiLCJ2aWV3LWNsaWVudHMiLCJtYW5hZ2UtYXV0aG9yaXphdGlvbiIsIm1hbmFnZS1jbGllbnRzIl19LCJwdWJsaWMtcmVhbG0iOnsicm9sZXMiOlsidmlldy1yZWFsbSIsInZpZXctaWRlbnRpdHktcHJvdmlkZXJzIiwibWFuYWdlLWlkZW50aXR5LXByb3ZpZGVycyIsImltcGVyc29uYXRpb24iLCJjcmVhdGUtY2xpZW50IiwibWFuYWdlLXVzZXJzIiwidmlldy1hdXRob3JpemF0aW9uIiwibWFuYWdlLWV2ZW50cyIsIm1hbmFnZS1yZWFsbSIsInZpZXctZXZlbnRzIiwidmlldy11c2VycyIsInZpZXctY2xpZW50cyIsIm1hbmFnZS1hdXRob3JpemF0aW9uIiwibWFuYWdlLWNsaWVudHMiXX0sIm1hc3Rlci1yZWFsbSI6eyJyb2xlcyI6WyJ2aWV3LXJlYWxtIiwidmlldy1pZGVudGl0eS1wcm92aWRlcnMiLCJtYW5hZ2UtaWRlbnRpdHktcHJvdmlkZXJzIiwiaW1wZXJzb25hdGlvbiIsImNyZWF0ZS1jbGllbnQiLCJtYW5hZ2UtdXNlcnMiLCJ2aWV3LWF1dGhvcml6YXRpb24iLCJtYW5hZ2UtZXZlbnRzIiwibWFuYWdlLXJlYWxtIiwidmlldy1ldmVudHMiLCJ2aWV3LXVzZXJzIiwidmlldy1jbGllbnRzIiwibWFuYWdlLWF1dGhvcml6YXRpb24iLCJtYW5hZ2UtY2xpZW50cyJdfSwiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwiY29udHJhY3RvciI6eyJpZCI6IjYwOSJ9LCJwZXJzb24iOnsiaWQiOiI0MjYyNyJ9LCJuYW1lIjoi0JPQtdC90L3QsNC00LjQuSDQnNCw0LrQsNGA0L7QsiIsInByZWZlcnJlZF91c2VybmFtZSI6IjAwMDAwMDEwMDUiLCJnaXZlbl9uYW1lIjoi0JPQtdC90L3QsNC00LjQuSIsImZhbWlseV9uYW1lIjoi0JzQsNC60LDRgNC-0LIiLCJlbWFpbCI6ImdtYWthcm92QG9ib3ouY29tIn0.DDmESVFTbTeZVViL_6C5PQidsbmNJ7MVgtEPHpkchF7E00gJ0lYNhvtknFK8M7S-d6_8j2_4_QQjN5VcyPY0tzIUBJgYTaIT-LgGu6NF94-G1qrWIqDxVe4btKEijMKKYcBfLNzp9v59bRDoWMpFzF78yHqmBKeSBzxBPllfMvwxbUEHtiQxqFAB7-DEu48-PRy91C1I3StemW8qyLSoDnOzkDpwawaO_5K2fK6tnOd6h4Di4S9oWYNQ9JHypyRMrstYTHMp1z9vAcUWbjFbQrUTmp-qpkYGi_eIKf__fAJdcGUrVZEboQFQZxA0mzYpCkVjHWu_P0mFnd13_AqYNw");
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
    public ResponseEntity<Void> deleteRequestAdvance(MultipartFile filename, String tripNum) {
        return null;
    }

    @Override
    public ResponseEntity<Void> uploadRequestAdvance(MultipartFile filename, String tripNum) {
        Trip trip = tripRepository.getTripByNum(tripNum).get();
        Contractor contractor = contractorRepository.findById(trip.getContractorId()).orElseThrow(() -> {
                Error error = new Error();
                error.setErrorMessage("Contractor not found for tripId: " + trip.getId());
                return new BusinessLogicException(HttpStatus.UNPROCESSABLE_ENTITY, error);
            }
        );
        TripRequestAdvancePayment tripRequestAdvancePayment = tripRequestAdvancePaymentRepository
            .findRequestAdvancePayment(trip.getId(), trip.getDriverId(), trip.getContractorId());
        String url = applicationProperties.getBStoreUrl() + "pdf/";
//        ResponseEntity<Resource> response;
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "multipart/form-data;");
        final String bearer = "Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJtZWwxMDZWemFlZGpmU3ctUVRtblVLa3ZuQUlfdXB3TGdRZHY4Vm85SEk4In0.eyJqdGkiOiI3MmRjZDA2NS1mMDhmLTRkZjAtYjMwMi0zNzNiYzUwZDAzNjYiLCJleHAiOjE2MTU2NDk0NjAsIm5iZiI6MCwiaWF0IjoxNTg0MTEzNDYwLCJpc3MiOiJodHRwczovL2Rldi5vYm96Lm9ubGluZS9hdXRoL3JlYWxtcy9tYXN0ZXIiLCJhdWQiOiJlbHAiLCJzdWIiOiJmOmZjYzAzMzZjLWU2ZjItNGVlNy1iOWViLWMyNTY0NjczYjAzNjo0MjYyNyIsInR5cCI6IkJlYXJlciIsImF6cCI6ImVscCIsImF1dGhfdGltZSI6MCwic2Vzc2lvbl9zdGF0ZSI6IjBjM2U4MzI4LTYxNjctNDlhYi05MWI2LWJjNGMyM2ZhZjljZSIsImFjciI6IjEiLCJjbGllbnRfc2Vzc2lvbiI6IjBmYTc4NjVmLTVmN2QtNDFhOC05NjU2LTE0MWQ0YjU3ODI1OSIsImFsbG93ZWQtb3JpZ2lucyI6WyIqIl0sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJjcmVhdGUtcmVhbG0iLCJjYXJyaWVyIiwiZWxwLWFkbWluIiwic2VuZGVyIiwiYWRtaW4iLCJ1bWFfYXV0aG9yaXphdGlvbiIsImRpc3BhdGNoZXIiXX0sInJlc291cmNlX2FjY2VzcyI6eyJkcml2ZXItYXBwLXJlYWxtIjp7InJvbGVzIjpbInZpZXctcmVhbG0iLCJ2aWV3LWlkZW50aXR5LXByb3ZpZGVycyIsIm1hbmFnZS1pZGVudGl0eS1wcm92aWRlcnMiLCJpbXBlcnNvbmF0aW9uIiwiY3JlYXRlLWNsaWVudCIsIm1hbmFnZS11c2VycyIsInZpZXctYXV0aG9yaXphdGlvbiIsIm1hbmFnZS1ldmVudHMiLCJtYW5hZ2UtcmVhbG0iLCJ2aWV3LWV2ZW50cyIsInZpZXctdXNlcnMiLCJ2aWV3LWNsaWVudHMiLCJtYW5hZ2UtYXV0aG9yaXphdGlvbiIsIm1hbmFnZS1jbGllbnRzIl19LCJkcml2ZXItYXBwLXJlZ2lzdHJhdGlvbi1yZWFsbSI6eyJyb2xlcyI6WyJ2aWV3LXJlYWxtIiwidmlldy1pZGVudGl0eS1wcm92aWRlcnMiLCJtYW5hZ2UtaWRlbnRpdHktcHJvdmlkZXJzIiwiaW1wZXJzb25hdGlvbiIsImNyZWF0ZS1jbGllbnQiLCJtYW5hZ2UtdXNlcnMiLCJ2aWV3LWF1dGhvcml6YXRpb24iLCJtYW5hZ2UtZXZlbnRzIiwibWFuYWdlLXJlYWxtIiwidmlldy1ldmVudHMiLCJ2aWV3LXVzZXJzIiwidmlldy1jbGllbnRzIiwibWFuYWdlLWF1dGhvcml6YXRpb24iLCJtYW5hZ2UtY2xpZW50cyJdfSwiZG1zLXJlYWxtIjp7InJvbGVzIjpbInZpZXctcmVhbG0iLCJ2aWV3LWlkZW50aXR5LXByb3ZpZGVycyIsIm1hbmFnZS1pZGVudGl0eS1wcm92aWRlcnMiLCJpbXBlcnNvbmF0aW9uIiwiY3JlYXRlLWNsaWVudCIsIm1hbmFnZS11c2VycyIsInZpZXctYXV0aG9yaXphdGlvbiIsIm1hbmFnZS1ldmVudHMiLCJtYW5hZ2UtcmVhbG0iLCJ2aWV3LWV2ZW50cyIsInZpZXctdXNlcnMiLCJ2aWV3LWNsaWVudHMiLCJtYW5hZ2UtYXV0aG9yaXphdGlvbiIsIm1hbmFnZS1jbGllbnRzIl19LCJwdWJsaWMtcmVhbG0iOnsicm9sZXMiOlsidmlldy1yZWFsbSIsInZpZXctaWRlbnRpdHktcHJvdmlkZXJzIiwibWFuYWdlLWlkZW50aXR5LXByb3ZpZGVycyIsImltcGVyc29uYXRpb24iLCJjcmVhdGUtY2xpZW50IiwibWFuYWdlLXVzZXJzIiwidmlldy1hdXRob3JpemF0aW9uIiwibWFuYWdlLWV2ZW50cyIsIm1hbmFnZS1yZWFsbSIsInZpZXctZXZlbnRzIiwidmlldy11c2VycyIsInZpZXctY2xpZW50cyIsIm1hbmFnZS1hdXRob3JpemF0aW9uIiwibWFuYWdlLWNsaWVudHMiXX0sIm1hc3Rlci1yZWFsbSI6eyJyb2xlcyI6WyJ2aWV3LXJlYWxtIiwidmlldy1pZGVudGl0eS1wcm92aWRlcnMiLCJtYW5hZ2UtaWRlbnRpdHktcHJvdmlkZXJzIiwiaW1wZXJzb25hdGlvbiIsImNyZWF0ZS1jbGllbnQiLCJtYW5hZ2UtdXNlcnMiLCJ2aWV3LWF1dGhvcml6YXRpb24iLCJtYW5hZ2UtZXZlbnRzIiwibWFuYWdlLXJlYWxtIiwidmlldy1ldmVudHMiLCJ2aWV3LXVzZXJzIiwidmlldy1jbGllbnRzIiwibWFuYWdlLWF1dGhvcml6YXRpb24iLCJtYW5hZ2UtY2xpZW50cyJdfSwiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwiY29udHJhY3RvciI6eyJpZCI6IjYwOSJ9LCJwZXJzb24iOnsiaWQiOiI0MjYyNyJ9LCJuYW1lIjoi0JPQtdC90L3QsNC00LjQuSDQnNCw0LrQsNGA0L7QsiIsInByZWZlcnJlZF91c2VybmFtZSI6IjAwMDAwMDEwMDUiLCJnaXZlbl9uYW1lIjoi0JPQtdC90L3QsNC00LjQuSIsImZhbWlseV9uYW1lIjoi0JzQsNC60LDRgNC-0LIiLCJlbWFpbCI6ImdtYWthcm92QG9ib3ouY29tIn0.DDmESVFTbTeZVViL_6C5PQidsbmNJ7MVgtEPHpkchF7E00gJ0lYNhvtknFK8M7S-d6_8j2_4_QQjN5VcyPY0tzIUBJgYTaIT-LgGu6NF94-G1qrWIqDxVe4btKEijMKKYcBfLNzp9v59bRDoWMpFzF78yHqmBKeSBzxBPllfMvwxbUEHtiQxqFAB7-DEu48-PRy91C1I3StemW8qyLSoDnOzkDpwawaO_5K2fK6tnOd6h4Di4S9oWYNQ9JHypyRMrstYTHMp1z9vAcUWbjFbQrUTmp-qpkYGi_eIKf__fAJdcGUrVZEboQFQZxA0mzYpCkVjHWu_P0mFnd13_AqYNw";
        headers.add(HttpHeaders.AUTHORIZATION, bearer);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", filename.getResource());
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = new RestTemplate().exchange(url, HttpMethod.POST, request, String.class);
        if (response.getStatusCode().value() == 200) {
            try {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                String fileUuid = jsonNode.get("file_uuid").asText();
                return saveTripDocuments(tripNum, fileUuid, bearer, tripRequestAdvancePayment);
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
        uploadRequestAdvance(filename, trip_num);
        return new ResponseEntity<>(HttpStatus.OK);
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
    public ResponseEntity<Void> requestGiveAdvancePaymentForCarrier(UUID uuid) {

        TripRequestAdvancePayment t = getTripRequestAdvancePayment(uuid);
        if (!t.getPageCarrierUrlIsAccess()) {
            getVoidResponseEntity("PageCarrierUrlIsAccess is false");
        }
        if (t.getPushButtonAt() == null) {
            t.setPushButtonAt(OffsetDateTime.now());
            tripRequestAdvancePaymentRepository.save(t);
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return getVoidResponseEntity("Button already pushed");
        }
    }

    @Override
    public ResponseEntity<FrontAdvancePaymentResponse> searchAdvancePaymentRequestByUuid(UUID uuid) {
        TripRequestAdvancePayment t = getTripRequestAdvancePayment(uuid);

        if (!t.getPageCarrierUrlIsAccess()) {
            getVoidResponseEntity("PageCarrierUrlIsAccess is false");
        }

        List<Tuple> tripPointDtos = tripRepository.getTripPointAddress(t.getTripId());
        final int size = tripPointDtos.size();
        if (size > 1) {
            FrontAdvancePaymentResponse frontAdvancePaymentResponse = new FrontAdvancePaymentResponse();
            final Tuple tripPointDto = tripPointDtos.get(0);
            frontAdvancePaymentResponse.setTripNum(tripPointDto.get(3).toString());
            frontAdvancePaymentResponse.setFirstLoadingAddress(tripPointDto.get(1).toString());
            frontAdvancePaymentResponse.setPushButtonAt(t.getPushButtonAt());
            frontAdvancePaymentResponse.setUrlAdvanceApplication(t.getUuidAdvanceApplicationFile());
            frontAdvancePaymentResponse.setLastUnloadingAddress(tripPointDtos.get(size - 1).get(1).toString());
            frontAdvancePaymentResponse.setTripCostWithVat(t.getTripCost());
            frontAdvancePaymentResponse.setRegistrationFee(t.getRegistrationFee());
            return new ResponseEntity<>(frontAdvancePaymentResponse, HttpStatus.OK);
        }
        Error error = new Error();
        error.setErrorMessage("TripPointAddress not found");
        throw new BusinessLogicException(HttpStatus.UNPROCESSABLE_ENTITY, error);
    }

    private TripRequestAdvancePayment getTripRequestAdvancePayment(UUID uuid) {
        return tripRequestAdvancePaymentRepository.findTripRequestAdvancePayment(uuid).orElseThrow(() -> {
                Error error = new Error();
                error.setErrorMessage("AdvancePaymentRequest not found");
                return new BusinessLogicException(HttpStatus.UNPROCESSABLE_ENTITY, error);
            }
        );
    }

    private ResponseEntity<Void> getVoidResponseEntity(String s) {
        Error error = new Error();
        error.setErrorMessage(s);
        throw new BusinessLogicException(HttpStatus.UNPROCESSABLE_ENTITY, error);
    }

    public MessageDto getMessageDto(TripRequestAdvancePayment tripRequestAdvancePayment,
                                    ContractorAdvancePaymentContact contact,
                                    String paymentContractorFullName) {
        MessageDto messageDto = new MessageDto();
        messageDto.setAdvancePaymentSum(tripRequestAdvancePayment.getAdvancePaymentSum());
        messageDto.setContractorName(paymentContractorFullName);
        messageDto.setEmail(contact.getEmail());
        messageDto.setPhone(contact.getPhone());
        messageDto.setLKLink(applicationProperties.getLkUrl() + tripRequestAdvancePayment.getUuidRequest());
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

    private ResponseEntity<Void> saveTripDocuments(String tripNum, String fileUuid, String bearer, TripRequestAdvancePayment tripRequestAdvancePayment) {
        String url = applicationProperties.getOrdersApiUrl();
        Trip trip = tripRepository.getTripByNum(tripNum).orElseThrow(() -> {
                Error error = new Error();
                error.setErrorMessage("trip not found");
                return new BusinessLogicException(HttpStatus.UNPROCESSABLE_ENTITY, error);
            }
        );
        url = String.format(url, trip.getOrderId(), trip.getId());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.AUTHORIZATION, bearer);
        final String s = "{\"trip_document\":{\"id\":null,\"file_id\":\"%s\",\"document_type_code\":\"assignment_advance_request\",\"name\":\"Заявка на авансирование\"}}";
        HttpEntity<String> request = new HttpEntity<>(String.format(s, fileUuid), headers);
        ResponseEntity<Void> response = new RestTemplate().exchange(url, HttpMethod.POST, request, Void.class);
        if (response.getStatusCode().value() == 200) {
            log.info("saveTripDocuments ok");
        } else {
            log.error("saveTripDocuments fail. http code {}", response.getStatusCode().value());
        }
        tripRequestAdvancePayment.setUuidAdvanceApplicationFile(fileUuid);
        tripRequestAdvancePaymentRepository.save(tripRequestAdvancePayment);
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
        final boolean isAllDocsUpload = requestFileUuid != null && advanceRequestFileUuid != null;
        if (!isAllDocsUpload) {
            log.info("Не загружены документы заявка / договор заявка и договор заявка на авансирование");
        }
        return isAllDocsUpload;
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
        tripRequestAdvancePayment.setUuidRequest(UUID.randomUUID());

        return tripRequestAdvancePayment;
    }
//    TODO заполнение таблицы исключений тип заказа клиента  берем из текущего заказа

    Boolean confirmRequestToUnf() {
//        TODO : отправить в Rabbit вызов метода Паши
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
//    TODO gettoken
//    TODO сделать выполнение без авторизации методов лк перевозчика

}
