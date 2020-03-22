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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
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

import static online.oboz.trip.trip_carrier_advance_payment_api.util.DtoUtils.getMessageDto;

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
    private final NotificationService notificationService;
    private final PersonRepository personRepository;
    private final ApplicationProperties applicationProperties;
    private final ObjectMapper objectMapper;
    private final OrderRepository orderRepository;
    private final RestService restService;

    @Autowired
    public AdvancePaymentDelegateImpl(AdvancePaymentCostRepository advancePaymentCostRepository,
                                      TripRequestAdvancePaymentRepository tripRequestAdvancePaymentRepository,
                                      ContractorAdvancePaymentContactRepository contractorAdvancePaymentContactRepository,
                                      ContractorAdvanceExclusionRepository contractorAdvanceExclusionRepository,
                                      TripRepository tripRepository,
                                      ContractorRepository contractorRepository,
                                      NotificationService notificationService,
                                      PersonRepository personRepository,
                                      ApplicationProperties applicationProperties,
                                      ObjectMapper objectMapper,
                                      OrderRepository orderRepository,
                                      RestService restService) {
        this.advancePaymentCostRepository = advancePaymentCostRepository;
        this.tripRequestAdvancePaymentRepository = tripRequestAdvancePaymentRepository;
        this.contractorAdvancePaymentContactRepository = contractorAdvancePaymentContactRepository;
        this.contractorAdvanceExclusionRepository = contractorAdvanceExclusionRepository;
        this.tripRepository = tripRepository;
        this.contractorRepository = contractorRepository;
        this.notificationService = notificationService;
        this.personRepository = personRepository;
        this.restService = restService;
        this.applicationProperties = applicationProperties;
        this.objectMapper = objectMapper;
        this.orderRepository = orderRepository;
    }

    @Override
    public ResponseEntity<ResponseAdvancePayment> searchAdvancePaymentRequest(Filter filter) {
        Pageable pageable = PageRequest.of(filter.getPage() - 1, filter.getPerPage());
        List<FrontAdvancePaymentResponse> responseList;
        responseList = tripRequestAdvancePaymentRepository.findTripRequestAdvancePayment(pageable).stream()
            .map(rec -> {
                FrontAdvancePaymentResponse frontAdvancePaymentResponse = new FrontAdvancePaymentResponse();
                ContractorAdvancePaymentContact contractorAdvancePaymentContact =
                    contractorAdvancePaymentContactRepository.findContractorAdvancePaymentContact(rec.getContractorId())
                        .orElse(new ContractorAdvancePaymentContact());
                Contractor contractor = contractorRepository.findById(rec.getContractorId()).orElse(new Contractor());
                String contractorPaymentName = contractorRepository.getContractor(rec.getPaymentContractorId());
                Trip trip = tripRepository.findById(rec.getTripId()).orElse(new Trip());
                AdvancePaymentDelegateImpl.this.getFrontAdvancePaymentResponse(rec, frontAdvancePaymentResponse, contractorAdvancePaymentContact, contractor, contractorPaymentName, trip);
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
        Trip trip = tripRepository.findById(tripRequestAdvancePayment.getTripId()).orElseThrow(() ->
            getBusinessLogicException("trip not found")
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
            if (orderTypeId.equals(contractorAdvanceExclusion.getOrderTypeId())) {
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
            throw getBusinessLogicException("no download All Documents");
        } else if (cancelAdvance) {
            throw getBusinessLogicException("cancelAdvance is true");
        } else {
            throw getBusinessLogicException("unf already send");
        }
    }

    @Override
    public ResponseEntity<IsAdvancedRequestResponse> isAdvanced(Long tripId) {
        Trip trip = tripRepository.getMotorTrip(tripId).orElseThrow(() -> getBusinessLogicException("trip not found"));
        Contractor contractor = contractorRepository.findById(trip.getContractorId()).orElseThrow(() ->
            getBusinessLogicException("Contractor not found for tripId: " + tripId)
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
            throw getBusinessLogicException("Не назначены необходимы поля: advancePaymentCost DriverId ContractorId");
        }
        if (restService.findTripRequestDocs(trip).isEmpty()) {
            throw getBusinessLogicException("Не загружены Договор заявка / заявка ");
        }
        TripRequestAdvancePayment tripRequestAdvancePayment = tripRequestAdvancePaymentRepository
            .findRequestAdvancePayment(tripId,
                trip.getDriverId(),
                trip.getContractorId());
        if (tripRequestAdvancePayment != null) {
            throw getBusinessLogicException("tripRequestAdvancePayment is present");
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
            MessageDto messageDto = getMessageDto(tripRequestAdvancePayment, contact, paymentContractor,
                applicationProperties.getLkUrl(),
                trip.getNum()
            );
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
            response = restService.getResourceResponseEntity(url.toString(), new HttpHeaders());
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
        return restService.getResourceBStore(uuidFile);
    }

    @Override
    public ResponseEntity<Resource> downloadRequest(String tripNum) {
        String uuidFile = tripRequestAdvancePaymentRepository.findRequestAdvancePaymentByTripNum(tripNum).getUuidContractApplicationFile();
        return restService.getResourceBStore(uuidFile);
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
        Trip trip = tripRepository.getTripByNum(tripNum).orElseThrow(() -> getBusinessLogicException("trip not found"));
        final Long tripId = trip.getId();
        TripRequestAdvancePayment tripRequestAdvancePayment = tripRequestAdvancePaymentRepository
            .findRequestAdvancePayment(tripId, trip.getDriverId(), trip.getContractorId());
        String url = applicationProperties.getBStoreUrl() + "pdf/";
        final String bearer = "Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJtZWwxMDZWemFlZGpmU3ctUVRtblVLa3ZuQUlfdXB3TGdRZHY4Vm85SEk4In0.eyJqdGkiOiI3MmRjZDA2NS1mMDhmLTRkZjAtYjMwMi0zNzNiYzUwZDAzNjYiLCJleHAiOjE2MTU2NDk0NjAsIm5iZiI6MCwiaWF0IjoxNTg0MTEzNDYwLCJpc3MiOiJodHRwczovL2Rldi5vYm96Lm9ubGluZS9hdXRoL3JlYWxtcy9tYXN0ZXIiLCJhdWQiOiJlbHAiLCJzdWIiOiJmOmZjYzAzMzZjLWU2ZjItNGVlNy1iOWViLWMyNTY0NjczYjAzNjo0MjYyNyIsInR5cCI6IkJlYXJlciIsImF6cCI6ImVscCIsImF1dGhfdGltZSI6MCwic2Vzc2lvbl9zdGF0ZSI6IjBjM2U4MzI4LTYxNjctNDlhYi05MWI2LWJjNGMyM2ZhZjljZSIsImFjciI6IjEiLCJjbGllbnRfc2Vzc2lvbiI6IjBmYTc4NjVmLTVmN2QtNDFhOC05NjU2LTE0MWQ0YjU3ODI1OSIsImFsbG93ZWQtb3JpZ2lucyI6WyIqIl0sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJjcmVhdGUtcmVhbG0iLCJjYXJyaWVyIiwiZWxwLWFkbWluIiwic2VuZGVyIiwiYWRtaW4iLCJ1bWFfYXV0aG9yaXphdGlvbiIsImRpc3BhdGNoZXIiXX0sInJlc291cmNlX2FjY2VzcyI6eyJkcml2ZXItYXBwLXJlYWxtIjp7InJvbGVzIjpbInZpZXctcmVhbG0iLCJ2aWV3LWlkZW50aXR5LXByb3ZpZGVycyIsIm1hbmFnZS1pZGVudGl0eS1wcm92aWRlcnMiLCJpbXBlcnNvbmF0aW9uIiwiY3JlYXRlLWNsaWVudCIsIm1hbmFnZS11c2VycyIsInZpZXctYXV0aG9yaXphdGlvbiIsIm1hbmFnZS1ldmVudHMiLCJtYW5hZ2UtcmVhbG0iLCJ2aWV3LWV2ZW50cyIsInZpZXctdXNlcnMiLCJ2aWV3LWNsaWVudHMiLCJtYW5hZ2UtYXV0aG9yaXphdGlvbiIsIm1hbmFnZS1jbGllbnRzIl19LCJkcml2ZXItYXBwLXJlZ2lzdHJhdGlvbi1yZWFsbSI6eyJyb2xlcyI6WyJ2aWV3LXJlYWxtIiwidmlldy1pZGVudGl0eS1wcm92aWRlcnMiLCJtYW5hZ2UtaWRlbnRpdHktcHJvdmlkZXJzIiwiaW1wZXJzb25hdGlvbiIsImNyZWF0ZS1jbGllbnQiLCJtYW5hZ2UtdXNlcnMiLCJ2aWV3LWF1dGhvcml6YXRpb24iLCJtYW5hZ2UtZXZlbnRzIiwibWFuYWdlLXJlYWxtIiwidmlldy1ldmVudHMiLCJ2aWV3LXVzZXJzIiwidmlldy1jbGllbnRzIiwibWFuYWdlLWF1dGhvcml6YXRpb24iLCJtYW5hZ2UtY2xpZW50cyJdfSwiZG1zLXJlYWxtIjp7InJvbGVzIjpbInZpZXctcmVhbG0iLCJ2aWV3LWlkZW50aXR5LXByb3ZpZGVycyIsIm1hbmFnZS1pZGVudGl0eS1wcm92aWRlcnMiLCJpbXBlcnNvbmF0aW9uIiwiY3JlYXRlLWNsaWVudCIsIm1hbmFnZS11c2VycyIsInZpZXctYXV0aG9yaXphdGlvbiIsIm1hbmFnZS1ldmVudHMiLCJtYW5hZ2UtcmVhbG0iLCJ2aWV3LWV2ZW50cyIsInZpZXctdXNlcnMiLCJ2aWV3LWNsaWVudHMiLCJtYW5hZ2UtYXV0aG9yaXphdGlvbiIsIm1hbmFnZS1jbGllbnRzIl19LCJwdWJsaWMtcmVhbG0iOnsicm9sZXMiOlsidmlldy1yZWFsbSIsInZpZXctaWRlbnRpdHktcHJvdmlkZXJzIiwibWFuYWdlLWlkZW50aXR5LXByb3ZpZGVycyIsImltcGVyc29uYXRpb24iLCJjcmVhdGUtY2xpZW50IiwibWFuYWdlLXVzZXJzIiwidmlldy1hdXRob3JpemF0aW9uIiwibWFuYWdlLWV2ZW50cyIsIm1hbmFnZS1yZWFsbSIsInZpZXctZXZlbnRzIiwidmlldy11c2VycyIsInZpZXctY2xpZW50cyIsIm1hbmFnZS1hdXRob3JpemF0aW9uIiwibWFuYWdlLWNsaWVudHMiXX0sIm1hc3Rlci1yZWFsbSI6eyJyb2xlcyI6WyJ2aWV3LXJlYWxtIiwidmlldy1pZGVudGl0eS1wcm92aWRlcnMiLCJtYW5hZ2UtaWRlbnRpdHktcHJvdmlkZXJzIiwiaW1wZXJzb25hdGlvbiIsImNyZWF0ZS1jbGllbnQiLCJtYW5hZ2UtdXNlcnMiLCJ2aWV3LWF1dGhvcml6YXRpb24iLCJtYW5hZ2UtZXZlbnRzIiwibWFuYWdlLXJlYWxtIiwidmlldy1ldmVudHMiLCJ2aWV3LXVzZXJzIiwidmlldy1jbGllbnRzIiwibWFuYWdlLWF1dGhvcml6YXRpb24iLCJtYW5hZ2UtY2xpZW50cyJdfSwiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwiY29udHJhY3RvciI6eyJpZCI6IjYwOSJ9LCJwZXJzb24iOnsiaWQiOiI0MjYyNyJ9LCJuYW1lIjoi0JPQtdC90L3QsNC00LjQuSDQnNCw0LrQsNGA0L7QsiIsInByZWZlcnJlZF91c2VybmFtZSI6IjAwMDAwMDEwMDUiLCJnaXZlbl9uYW1lIjoi0JPQtdC90L3QsNC00LjQuSIsImZhbWlseV9uYW1lIjoi0JzQsNC60LDRgNC-0LIiLCJlbWFpbCI6ImdtYWthcm92QG9ib3ouY29tIn0.DDmESVFTbTeZVViL_6C5PQidsbmNJ7MVgtEPHpkchF7E00gJ0lYNhvtknFK8M7S-d6_8j2_4_QQjN5VcyPY0tzIUBJgYTaIT-LgGu6NF94-G1qrWIqDxVe4btKEijMKKYcBfLNzp9v59bRDoWMpFzF78yHqmBKeSBzxBPllfMvwxbUEHtiQxqFAB7-DEu48-PRy91C1I3StemW8qyLSoDnOzkDpwawaO_5K2fK6tnOd6h4Di4S9oWYNQ9JHypyRMrstYTHMp1z9vAcUWbjFbQrUTmp-qpkYGi_eIKf__fAJdcGUrVZEboQFQZxA0mzYpCkVjHWu_P0mFnd13_AqYNw";
        ResponseEntity<String> response = restService.getFileUuid(filename, url, bearer);
        if (response.getStatusCode().value() == 200) {
            try {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                String fileUuid = jsonNode.get("file_uuid").asText();
                String urlOrders = String.format(applicationProperties.getOrdersApiUrl(), trip.getOrderId(), tripId);
                ResponseEntity<Void> ordersApiResponse = restService.saveTripDocuments(urlOrders, fileUuid, bearer);
                tripRequestAdvancePayment.setUuidAdvanceApplicationFile(fileUuid);
                tripRequestAdvancePaymentRepository.save(tripRequestAdvancePayment);
                return ordersApiResponse;
            } catch (IOException e) {
                log.error("", e);
            }
        }
        log.error("uploadRequestAvance fail. http code {}", response.getStatusCode().value());
        throw getBusinessLogicException("uploadRequestAvance fail. http code " + response.getStatusCode().value());
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
            throw getBusinessLogicException("ContractorAdvancePaymentContact is present");
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
        final ContractorAdvancePaymentContact entity = contractorAdvancePaymentContact.orElseThrow(() ->
            getBusinessLogicException("ContractorAdvancePaymentContact not found")
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
            throw getBusinessLogicException("PageCarrierUrlIsAccess is false");
        }
        if (t.getPushButtonAt() == null) {
            t.setPushButtonAt(OffsetDateTime.now());
            tripRequestAdvancePaymentRepository.save(t);
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            throw getBusinessLogicException("Button already pushed");
        }
    }

    @Override
    public ResponseEntity<FrontAdvancePaymentResponse> searchAdvancePaymentRequestByUuid(UUID uuid) {
        TripRequestAdvancePayment t = getTripRequestAdvancePayment(uuid);
        if (!t.getPageCarrierUrlIsAccess()) {
            throw getBusinessLogicException("PageCarrierUrlIsAccess is false");
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
        throw getBusinessLogicException("TripPointAddress not found");
    }

    private TripRequestAdvancePayment getTripRequestAdvancePayment(UUID uuid) {
        return tripRequestAdvancePaymentRepository.findTripRequestAdvancePayment(uuid).orElseThrow(() ->
            getBusinessLogicException("AdvancePaymentRequest not found")
        );
    }

    private BusinessLogicException getBusinessLogicException(String s) {
        Error error = new Error();
        error.setErrorMessage(s);
        return new BusinessLogicException(HttpStatus.UNPROCESSABLE_ENTITY, error);
    }

    private void setButtonAccessGetAdvance(Trip trip,
                                           Contractor contractor,
                                           TripRequestAdvancePayment tripRequestAdvancePayment,
                                           IsAdvancedRequestResponse isAdvancedRequestResponse) {
        final Boolean isAutoAdvancePayment = contractor.getIsAutoAdvancePayment();
        Map<String, String> downloadedDocuments = restService.findTripRequestDocs(trip);
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

    private void getFrontAdvancePaymentResponse(TripRequestAdvancePayment rec,
                                                FrontAdvancePaymentResponse frontAdvancePaymentResponse,
                                                ContractorAdvancePaymentContact contractorAdvancePaymentContact,
                                                Contractor contractor,
                                                String contractorPaymentName,
                                                Trip trip) {
        frontAdvancePaymentResponse
            .id(rec.getId())
            .tripId(rec.getTripId())
            .tripNum(trip.getNum())
            .tripTypeCode(rec.getTripTypeCode())
            .createdAt(trip.getCreatedAt())
            .reqCreatedAt(rec.getCreatedAt())
            .contractorId(rec.getContractorId())
            .contractorName(contractor.getFullName())
            .contactFio(contractorAdvancePaymentContact.getFullName())
            .contactPhone(contractorAdvancePaymentContact.getPhone())
            .contactEmail(contractorAdvancePaymentContact.getEmail())
            .paymentContractor(contractorPaymentName)
            .isAutomationRequest(rec.getIsAutomationRequest())
            .tripCostWithVat(rec.getTripCost())
            .advancePaymentSum(rec.getAdvancePaymentSum())
            .registrationFee(rec.getRegistrationFee())
            //проставляется вручную сотрудниками авансирования
            .loadingComplete(rec.getLoadingComplete())
            .urlContractApplication(rec.getUuidContractApplicationFile())
            .urlAdvanceApplication(rec.getUuidAdvanceApplicationFile())
            .is1CSendAllowed(rec.getIs1CSendAllowed())
            .isUnfSend(rec.getIsUnfSend())
            .isPaid(rec.getIsPaid())
            .paidAt(rec.getPaidAt())
            .comment(rec.getComment())
            .cancelAdvance(rec.getCancelAdvance())
            .cancelAdvanceComment(rec.getCancelAdvanceComment())
            .authorId(rec.getAuthorId())
            .pageCarrierUrlIsAccess(rec.getPageCarrierUrlIsAccess());
//        TODO  просетить  значения:
//            .firstLoadingAddress(getFirstLoadingAddress)
//            .lastUnloadingAddress(getLastUnLoadingAddress);

    }

    private Boolean isDownloadAllDocuments(Trip trip) {
//        использовать только в confirm
        Map<String, String> fileRequestUuidMap = restService.findTripRequestDocs(trip);
        Map<String, String> fileAdvanceRequestUuidMap = restService.findAdvanceRequestDocs(trip);
        String requestFileUuid = Optional.ofNullable(fileRequestUuidMap.get("request")).orElse(fileRequestUuidMap.get("trip_request"));
        String advanceRequestFileUuid = fileAdvanceRequestUuidMap.get("assignment_advance_request");
        final boolean isAllDocsUpload = requestFileUuid != null && advanceRequestFileUuid != null;
        if (!isAllDocsUpload) {
            log.info("Не загружены документы заявка / договор заявка и договор заявка на авансирование");
        }
        return isAllDocsUpload;
    }

    private TripRequestAdvancePayment getTripRequestAdvancePayment(Long tripId,
                                                                   Contractor contractor,
                                                                   Double tripCostWithNds,
                                                                   AdvancePaymentCost advancePaymentCost,
                                                                   boolean isUnfSend,
                                                                   Trip trip) {
        TripRequestAdvancePayment tripRequestAdvancePayment = new TripRequestAdvancePayment();
        tripRequestAdvancePayment.setAuthorId(SecurityUtils.getAuthPersonId()).
            setTripId(advancePaymentCost.getId()).
            setIsUnfSend(isUnfSend).
            setTripCost(tripCostWithNds).
            setAdvancePaymentSum(advancePaymentCost.getAdvancePaymentSum()).
            setRegistrationFee(advancePaymentCost.getRegistrationFee()).
            setCancelAdvance(false).
            setContractorId(trip.getContractorId()).
            setDriverId(trip.getDriverId()).
            setCreatedAt(OffsetDateTime.now()).
            setTripId(tripId).
            setTripTypeCode(trip.getTripTypeCode()).
            setLoadingComplete(false).
            setPaymentContractorId(trip.getPaymentContractorId()).
            setPageCarrierUrlIsAccess(true).
            setIsPaid(false).
            setPaidAt(OffsetDateTime.now()).
            setCancelAdvanceComment("").
            setIsAutomationRequest(contractor.getIsAutoAdvancePayment()).
            setUuidRequest(UUID.randomUUID());
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
        return tripRequestAdvancePayment.orElseThrow(() -> getBusinessLogicException("TripRequestAdvancePayment not found"));
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
