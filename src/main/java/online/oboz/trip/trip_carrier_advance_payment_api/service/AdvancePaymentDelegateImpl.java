package online.oboz.trip.trip_carrier_advance_payment_api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.*;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.rabbit.Message;
import online.oboz.trip.trip_carrier_advance_payment_api.rabbit.RabbitMessageProducer;
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
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.Tuple;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
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
    private final RabbitMessageProducer rabbitMessageProducer;

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
                                      RestService restService,
                                      RabbitMessageProducer rabbitMessageProducer) {
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
        this.rabbitMessageProducer = rabbitMessageProducer;
    }

    @Override
    public ResponseEntity<ResponseAdvancePayment> searchAdvancePaymentRequest(Filter filter) {
        Pageable pageable = PageRequest.of(filter.getPage() - 1, filter.getPerPage(), constructSorting(filter));
        List<FrontAdvancePaymentResponse> responseList = tripRequestAdvancePaymentRepository.findTripRequestAdvancePayment(
            filter.getIsPushedUnfButton(),
            filter.getIsPaid(),
            filter.getIsCancelAdvance(),
            filter.getIsDownloadedContractApplication(),
            filter.getIsDownloadedAdvanceApplication(),
            filter.getIsUnfSend(),
            Optional.ofNullable(filter.getHasComment()).orElse(false),
            pageable).stream()
            .map(rec -> {
                ContractorAdvancePaymentContact contractorAdvancePaymentContact =
                    contractorAdvancePaymentContactRepository.findContractorAdvancePaymentContact(rec.getContractorId())
                        .orElse(new ContractorAdvancePaymentContact());
                Contractor contractor = contractorRepository.findById(rec.getContractorId()).orElse(new Contractor());
                String fullName = contractorRepository.getFullNameByPaymentContractorId(rec.getPaymentContractorId());
                Trip trip = tripRepository.findById(rec.getTripId()).orElse(new Trip());
                return getFrontAdvancePaymentResponse(rec, contractorAdvancePaymentContact, contractor, fullName, trip);
            })
            .collect(Collectors.toList());
        final ResponseAdvancePayment responseAdvancePayment = new ResponseAdvancePayment();
        responseAdvancePayment.setTotal(tripRequestAdvancePaymentRepository.totalTripRequestAdvancePayment(
            filter.getIsPushedUnfButton(),
            filter.getIsPaid(),
            filter.getIsCancelAdvance(),
            filter.getIsDownloadedContractApplication(),
            filter.getIsDownloadedAdvanceApplication(),
            filter.getIsUnfSend(),
            Optional.ofNullable(filter.getHasComment()).orElse(false)
            )
        );
        responseAdvancePayment.setRequestAdvancePayment(responseList);
        return new ResponseEntity<>(responseAdvancePayment, HttpStatus.OK);
    }

    private Sort constructSorting(Filter filter) {
        List<Sort.Order> sortingOrders = filter.getSort()
            .stream()
            .map(sort -> new Sort.Order(Sort.Direction.fromString(sort.getDir().name()), getProperty(sort.getKey())))
            .collect(Collectors.toList());
        if (sortingOrders.isEmpty()) {
            sortingOrders.add(new Sort.Order(Sort.Direction.fromString("asc"), "tripId"));
        }
        return Sort.by(sortingOrders);
    }

    private String getProperty(@NotNull @Valid SortByField key) {
        switch (key) {
            case PUSH_BUTTON_AT:
                return "pushButtonAt";
            case IS_AUTOMATION_REQUEST:
                return "isAutomationRequest";
            case LOADING_COMPLETE:
                return "loadingComplete";
            case IS_DOWNLOADED_CONTRACT_APPLICATION:
                return "isDownloadedContractApplication";
            case IS_DOWNLOADED_ADVANCE_APPLICATION:
                return "isDownloadedAdvanceApplication";
            case IS_PUSHED_UNF_BUTTON:
                return "isPushedUnfButton";
            case TRIP_NUM:
            default:
                return "tripId";
        }
    }

    @Override
    @Transactional
    public ResponseEntity<Void> confirmAdvancePayment(Long requestAdvansePaymentId) {
        TripRequestAdvancePayment tripRequestAdvancePayment = getTripRequestAdvancePaymentById(requestAdvansePaymentId);
        Trip trip = tripRepository.findById(tripRequestAdvancePayment.getTripId()).orElseThrow(() ->
            getBusinessLogicException("trip not found")
        );
        Order order = orderRepository.findById(trip.getOrderId()).orElseThrow(() ->
            getBusinessLogicException("order not found")
        );
        final boolean downloadAllDocuments = isDownloadAllDocuments(trip);
        final Boolean isCancelled = tripRequestAdvancePayment.getIsCancelled();
        if (downloadAllDocuments && !tripRequestAdvancePayment.getIsPushedUnfButton() && !isCancelled) {
            rabbitMessageProducer.sendMessage(new Message(tripRequestAdvancePayment.getId().toString()));
            log.info("send message to Rabbit complete");
            tripRequestAdvancePayment.setIsPushedUnfButton(true);
            tripRequestAdvancePayment.setIs1CSendAllowed(false);
            tripRequestAdvancePayment.setPageCarrierUrlIsAccess(false);
            tripRepository.save(trip);
            tripRequestAdvancePaymentRepository.save(tripRequestAdvancePayment);
            final Long orderTypeId = orderRepository.findById(trip.getOrderId()).get().getOrderTypeId();
            ContractorAdvanceExclusion contractorAdvanceExclusion = contractorAdvanceExclusionRepository
                .findByContractorId(trip.getContractorId(), order.getOrderTypeId()).orElse(new ContractorAdvanceExclusion());
            //проверяем наличие записи контрактора в таблице исключений по типу заказа при  отсутствии  добавляем запись
            if (contractorAdvanceExclusion.getId() == null) {
                contractorAdvanceExclusion = new ContractorAdvanceExclusion();
                contractorAdvanceExclusion.setCarrierId(trip.getContractorId());
                contractorAdvanceExclusion.setIsConfirmAdvance(true);
                contractorAdvanceExclusion.setOrderTypeId(orderTypeId);
                contractorAdvanceExclusion.setCarrierFullName(contractorRepository.findById(trip.getContractorId()).get().getFullName());
                contractorAdvanceExclusionRepository.save(contractorAdvanceExclusion);
            }
            return new ResponseEntity<>(HttpStatus.OK);
        }
        if (!downloadAllDocuments) {
            throw getBusinessLogicException("no download All Documents");
        } else if (isCancelled) {
            throw getBusinessLogicException("isCancelled is true");
        } else {
            throw getBusinessLogicException("unf already send");
        }
    }

    @Override
    public ResponseEntity<IsAdvancedRequestResponse> isAdvanced(Long tripId) {
        Trip trip = tripRepository.getMotorTrip(tripId).orElseGet(Trip::new);
        if (trip.getId() == null) {
            log.info("Trip not found for tripId: " + tripId);
            return getIsAdvancedRequestResponseResponseEntity();
        }
        Order order = orderRepository.findById(trip.getOrderId()).orElseGet(Order::new);
        if (order.getId() == null) {
            log.info("Order not found for orderId: " + order.getId());
            return getIsAdvancedRequestResponseResponseEntity();
        }
        Contractor contractor = contractorRepository.findById(trip.getContractorId()).orElseGet(Contractor::new);
        if (contractor.getId() == null) {
            log.info("Contractor not found for tripId: " + trip.getContractorId());
            return getIsAdvancedRequestResponseResponseEntity();
        }
        TripRequestAdvancePayment tripRequestAdvancePayment = tripRequestAdvancePaymentRepository
            .findRequestAdvancePayment(tripId, trip.getDriverId(), trip.getContractorId());
        IsAdvancedRequestResponse isAdvancedRequestResponse = getIsAdvancedRequestResponse();
        setButtonAccessGetAdvance(trip, contractor, tripRequestAdvancePayment, isAdvancedRequestResponse);
        ContractorAdvanceExclusion contractorAdvanceExclusion = contractorAdvanceExclusionRepository
            .findByContractorId(trip.getContractorId(), order.getOrderTypeId()).orElse(new ContractorAdvanceExclusion());
        if (tripRequestAdvancePayment != null) {
            final boolean isContractorLock = contractorAdvanceExclusion.getIsConfirmAdvance() != null &&
                !contractorAdvanceExclusion.getIsConfirmAdvance();
            isAdvancedRequestResponse.setTripTypeCode(tripRequestAdvancePayment.getTripTypeCode());
            isAdvancedRequestResponse.setIsContractorLock(isContractorLock);
            isAdvancedRequestResponse.setTripTypeCode(tripRequestAdvancePayment.getTripTypeCode());
            isAdvancedRequestResponse.setIsPaid(tripRequestAdvancePayment.getIsPaid());
            final Boolean isAutomationRequest = tripRequestAdvancePayment.getIsAutomationRequest();
            if (isAutomationRequest) {
                isAdvancedRequestResponse.setIsAutoRequested(true);
                isAdvancedRequestResponse.setComment(COMMENT);
            } else isAdvancedRequestResponse.setIsAutoRequested(false);
            final Long authorId = tripRequestAdvancePayment.getAuthorId();
            if (authorId != null) {
                setPersonInfo(isAdvancedRequestResponse, authorId);
            }

            isAdvancedRequestResponse.setCreatedAt(tripRequestAdvancePayment.getCreatedAt());
            log.info("isAdvancedRequestResponse for tripId: {} , DriverId: {} , ContractorId {} is: {}", tripId, trip.getDriverId(), trip.getContractorId(), isAdvancedRequestResponse);
        } else {
            log.info("isAdvancedRequestResponse not found for tripId: {} , DriverId: {} , ContractorId {}", tripId, trip.getDriverId(), trip.getContractorId());
        }
        return new ResponseEntity<>(isAdvancedRequestResponse, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> requestGiveAdvancePayment(Long tripId) {
        tripRepository.findById(tripId).orElseThrow(() -> getBusinessLogicException("trip not found"));
        Double tripCostWithNds = tripRepository.getTripCostWithVat(tripId);
        AdvancePaymentCost advancePaymentCost = advancePaymentCostRepository.searchAdvancePaymentCost(tripCostWithNds);
        Trip trip = tripRepository.getMotorTrip(tripId).orElseGet(Trip::new);
        Order order = orderRepository.findById(trip.getOrderId()).orElseGet(Order::new);
        if (trip.getId() == null) {
            log.error("tripTypeCode не 'motor' или  tripStatusCode не assigned для trip_id: {}", tripId);
            throw getBusinessLogicException("trip type code не 'motor' или  trip status code не 'assigned'");
        }
        final Long contractorId = trip.getContractorId();
        if (advancePaymentCost == null || trip.getDriverId() == null || contractorId == null) {
            throw getBusinessLogicException("Не назначены необходимы поля: advancePaymentCost DriverId ContractorId");
        }
        Map<String, String> tripRequestDocs = restService.findTripRequestDocs(trip);
        if (tripRequestDocs.isEmpty()) {
            throw getBusinessLogicException("Не загружены Договор заявка / заявка ");
        }
        TripRequestAdvancePayment tripRequestAdvancePayment = tripRequestAdvancePaymentRepository
            .findRequestAdvancePayment(tripId,
                trip.getDriverId(),
                contractorId);
        if (tripRequestAdvancePayment != null) {
            throw getBusinessLogicException("tripRequestAdvancePayment is present");
        }
        Contractor contractor = contractorRepository.findById(contractorId).orElse(new Contractor());
        String paymentContractor = contractorRepository.getFullNameByPaymentContractorId(trip.getPaymentContractorId());
        String tripRequestDocsUUID = tripRequestDocs.entrySet().iterator().next().getValue();
        tripRequestAdvancePayment = createTripRequestAdvancePayment(
            contractor,
            tripCostWithNds,
            advancePaymentCost,
            false,
            trip,
            tripRequestDocsUUID);
        ContractorAdvancePaymentContact contact = getAdvancePaymentContact(contractorId);
        if (contact != null && contact.getEmail() != null) {
            MessageDto messageDto = getMessageDto(tripRequestAdvancePayment, contact, paymentContractor,
                applicationProperties.getLkUrl(),
                trip.getNum()
            );
            if (contact.getEmail() != null) {
                notificationService.sendEmail(messageDto);
            }
            if (contact.getPhone() != null) {
                notificationService.sendSmsDelay(messageDto);
            }
            tripRequestAdvancePaymentRepository.save(tripRequestAdvancePayment);
            ContractorAdvanceExclusion contractorAdvanceExclusion = contractorAdvanceExclusionRepository.findByContractorId(
                contractorId, order.getOrderTypeId()).orElseGet(ContractorAdvanceExclusion::new);
            if (contractorAdvanceExclusion.getId() == null) {
                final ContractorAdvanceExclusion entity = new ContractorAdvanceExclusion();
                entity.setCarrierFullName(contractor.getFullName());
                entity.setOrderTypeId(order.getOrderTypeId());
                entity.setCarrierId(contractorId);
                entity.setIsConfirmAdvance(true);
                contractorAdvanceExclusionRepository.save(entity);
            }
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> changeAdvancePaymentComment(AdvancePaymentCommentDTO advancePaymentCommentDTO) {
        final TripRequestAdvancePayment entity = getTripRequestAdvancePaymentById(advancePaymentCommentDTO.getId());
        entity.setComment(advancePaymentCommentDTO.getAdvanceComment());
        tripRequestAdvancePaymentRepository.save(entity);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity downloadAvanceRequestTemplate(String tripNum) {
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
            response = restService.getResourceResponseEntity(url.toString(), new HttpHeaders());
            if (response != null) {
                log.info("report server response  Headers is: {}", response.getHeaders().entrySet().toString());
                return response;
            }
        }
        log.error("server {} returned bad response, tripRequestAdvancePayment is: {}", url, tripRequestAdvancePayment);
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
    public ResponseEntity downloadAvanceRequestTemplateForCarrier(String tripNum) {

        log.info("downloadAvanceRequestTemplate success");
        return downloadAvanceRequestTemplate(tripNum);
    }

    @Override
    public ResponseEntity<Void> uploadRequestAdvance(MultipartFile filename, String tripNum) {
//        TODO :  catch   big size file and response
        Trip trip = tripRepository.getTripByNum(tripNum).orElseThrow(() -> getBusinessLogicException("trip not found"));
        final Long tripId = trip.getId();
        TripRequestAdvancePayment tripRequestAdvancePayment = tripRequestAdvancePaymentRepository
            .findRequestAdvancePayment(tripId, trip.getDriverId(), trip.getContractorId());
        if (tripRequestAdvancePayment == null)
            throw getBusinessLogicException("tripRequestAdvancePayment not found");
        if (tripRequestAdvancePayment.getPushButtonAt() == null)
            throw getBusinessLogicException("PushButtonAt need is first");
        if (tripRequestAdvancePayment.getIsPushedUnfButton())
            throw getBusinessLogicException("uploadRequestAdvance forbidden");
        String url = applicationProperties.getBStoreUrl() + "pdf/";
        ResponseEntity<String> response = restService.getFileUuid(filename, url);
        if (response.getStatusCode().value() == 200) {
            try {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                String fileUuid = jsonNode.get("file_uuid").asText();
                String urlOrders = String.format(applicationProperties.getOrdersApiUrl(), trip.getOrderId(), tripId);
                ResponseEntity<Void> ordersApiResponse = restService.saveTripDocuments(urlOrders, fileUuid);
                tripRequestAdvancePayment.setUuidAdvanceApplicationFile(fileUuid);
                tripRequestAdvancePayment.setIsDownloadedAdvanceApplication(true);
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
        final TripRequestAdvancePayment entity = getTripRequestAdvancePaymentById(id);
        final Trip tripIgnore = tripRepository.getNotApproveTrip(entity.getTripId()).orElseGet(Trip::new);
        if (!loadingComplete) {
            entity.setIs1CSendAllowed(false);
            log.error("setIs1CSendAllowed is false");

        } else if (!entity.getIsPushedUnfButton() &&
            tripIgnore.getTripStatusCode() == null &&
            entity.getIsDownloadedAdvanceApplication() &&
            entity.getIsDownloadedContractApplication()) {
            entity.setIs1CSendAllowed(true);
            log.error("setIs1CSendAllowed is true ");

        }
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
        final TripRequestAdvancePayment entity = getTripRequestAdvancePaymentById(id);
        if (!entity.getIsCancelled()) {
            entity.setCancelledComment(cancelAdvanceComment);
            entity.setIsCancelled(true);
            tripRequestAdvancePaymentRepository.save(entity);
            log.error(" was cancel AdvancePayment with id: {} and cancel comment: {}", id, cancelAdvanceComment);
        }
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
        TripRequestAdvancePayment t = getTripRequestAdvancePaymentByUUID(uuid);
        if (!t.getPageCarrierUrlIsAccess()) {
            log.error("PageCarrierUrlIsAccess is false");
            throw getBusinessLogicException("PageCarrierUrlIsAccess is false");
        }
        if (t.getPushButtonAt() == null && !t.getIsCancelled()) {
            t.setPushButtonAt(OffsetDateTime.now());
            tripRequestAdvancePaymentRepository.save(t);
            log.error("save ok for advance request with uuid: {} ,PushButtonAt: {}, CancelAdvance: {} ", uuid, t.getPushButtonAt(), t.getIsCancelled());
        } else {
            log.error("Button already pushed or request was cancel");
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<FrontAdvancePaymentResponse> searchAdvancePaymentRequestByUuid(UUID uuid) {
        FrontAdvancePaymentResponse frontAdvancePaymentResponse = new FrontAdvancePaymentResponse();
        TripRequestAdvancePayment t = getTripRequestAdvancePaymentByUUID(uuid);
        if (t.getPageCarrierUrlIsAccess()) {
            List<Tuple> tripPointDtos = tripRepository.getTripPointAddress(t.getTripId());
            String firstLoadingAddress = getFirstLoadingAddress(tripPointDtos);
            String lastUnloadingAddress = getLastUnLoadingAddress(tripPointDtos);
            String tripNum = tripPointDtos.get(0).get(3).toString();
            frontAdvancePaymentResponse.setTripNum(tripNum);
            frontAdvancePaymentResponse.setFirstLoadingAddress(firstLoadingAddress);
            frontAdvancePaymentResponse.setPushButtonAt(t.getPushButtonAt());
            frontAdvancePaymentResponse.setUrlAdvanceApplication(t.getUuidAdvanceApplicationFile());
            frontAdvancePaymentResponse.setLastUnloadingAddress(lastUnloadingAddress);
            frontAdvancePaymentResponse.setTripCostWithVat(t.getTripCost());
            frontAdvancePaymentResponse.setAdvancePaymentSum(t.getAdvancePaymentSum());
            frontAdvancePaymentResponse.setRegistrationFee(t.getRegistrationFee());
            frontAdvancePaymentResponse.setIsCancelled(t.getIsCancelled());
            frontAdvancePaymentResponse.setIsPushedUnfButton(t.getIsPushedUnfButton());
        } else {
            frontAdvancePaymentResponse.setPageCarrierUrlIsAccess(t.getPageCarrierUrlIsAccess());
            new ResponseEntity<>(frontAdvancePaymentResponse, HttpStatus.OK);
            log.info("PageCarrierUrlIsAccess is false");
        }
        return new ResponseEntity<>(frontAdvancePaymentResponse, HttpStatus.OK);
    }

    private void setPersonInfo(IsAdvancedRequestResponse isAdvancedRequestResponse, Long authorId) {
        Person author = personRepository.findById(authorId).orElse(new Person());
        isAdvancedRequestResponse.setFirstName(author.getFirstName());
        isAdvancedRequestResponse.setLastName(author.getLastName());
        isAdvancedRequestResponse.setMiddleName(author.getMiddleName());
        isAdvancedRequestResponse.setAuthorId(authorId);
    }

    private ResponseEntity<IsAdvancedRequestResponse> getIsAdvancedRequestResponseResponseEntity() {
        return new ResponseEntity<>(getIsAdvancedRequestResponse(), HttpStatus.OK);
    }

    private IsAdvancedRequestResponse getIsAdvancedRequestResponse() {
        return new IsAdvancedRequestResponse();
    }

    private String getFirstLoadingAddress(List<Tuple> tripPointDtos) {
        if (!tripPointDtos.isEmpty()) {
            return tripPointDtos.get(0).get(1).toString();
        }
        throw getBusinessLogicException("FirstLoadingAddress not found");
    }

    private String getLastUnLoadingAddress(List<Tuple> tripPointDtos) {
        final int size = tripPointDtos.size();
        if (size > 1) {
            return tripPointDtos.get(size - 1).get(1).toString();
        }
        throw getBusinessLogicException("LastUnLoadingAddress not found");
    }

    private TripRequestAdvancePayment getTripRequestAdvancePaymentByUUID(UUID uuid) {
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
        if (tripRequestAdvancePayment == null && !isAutoAdvancePayment &&
            (!downloadedDocuments.isEmpty() || !applicationProperties.getRequiredDownloadDocs())
        ) {
            isAdvancedRequestResponse.setIsButtonActive(true);
        } else if (tripRequestAdvancePayment != null && (
            !tripRequestAdvancePayment.getIsCancelled() &&
                !isAutoAdvancePayment &&
                !tripRequestAdvancePayment.getIsPushedUnfButton() &&
                !trip.getDriverId().equals(tripRequestAdvancePayment.getDriverId()))) {
            isAdvancedRequestResponse.setIsButtonActive(true);
        } else if (tripRequestAdvancePayment != null && (
            tripRequestAdvancePayment.getIsCancelled() ||
                isAutoAdvancePayment ||
                tripRequestAdvancePayment.getIsPushedUnfButton() ||
                trip.getDriverId().equals(tripRequestAdvancePayment.getDriverId()))) {
            isAdvancedRequestResponse.setIsButtonActive(false);
        }
    }

    private FrontAdvancePaymentResponse getFrontAdvancePaymentResponse(TripRequestAdvancePayment rec,
                                                                       ContractorAdvancePaymentContact contractorAdvancePaymentContact,
                                                                       Contractor contractor,
                                                                       String contractorPaymentName,
                                                                       Trip trip) {
        FrontAdvancePaymentResponse frontAdvancePaymentResponse = new FrontAdvancePaymentResponse();
        List<Tuple> tripPointDtos = tripRepository.getTripPointAddress(trip.getId());
        String firstLoadingAddress = getFirstLoadingAddress(tripPointDtos);
        String lastUnloadingAddress = getLastUnLoadingAddress(tripPointDtos);
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
            .isPushedUnfButton(rec.getIsPushedUnfButton())
            .pushButtonAt(rec.getPushButtonAt())
            .isPaid(rec.getIsPaid())
            .paidAt(rec.getPaidAt())
            .comment(rec.getComment())
            .isCancelled(rec.getIsCancelled())
            .cancelledComment(rec.getCancelledComment())
            .authorId(rec.getAuthorId())
            .pageCarrierUrlIsAccess(rec.getPageCarrierUrlIsAccess())
            .firstLoadingAddress(firstLoadingAddress)
            .lastUnloadingAddress(lastUnloadingAddress);
        return frontAdvancePaymentResponse;
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

    private TripRequestAdvancePayment createTripRequestAdvancePayment(Contractor contractor,
                                                                      Double tripCostWithNds,
                                                                      AdvancePaymentCost advancePaymentCost,
                                                                      boolean isPushedUnfButton,
                                                                      Trip trip,
                                                                      String tripRequestDocsUUID) {
        TripRequestAdvancePayment tripRequestAdvancePayment = new TripRequestAdvancePayment();
        tripRequestAdvancePayment.setAuthorId(SecurityUtils.getAuthPersonId())
            .setTripId(advancePaymentCost.getId())
            .setIsPushedUnfButton(isPushedUnfButton)
            .setTripCost(tripCostWithNds)
            .setAdvancePaymentSum(advancePaymentCost.getAdvancePaymentSum())
            .setRegistrationFee(advancePaymentCost.getRegistrationFee())
            .setIsCancelled(false)
            .setContractorId(trip.getContractorId())
            .setDriverId(trip.getDriverId())
            .setCreatedAt(OffsetDateTime.now())
            .setPushButtonAt(OffsetDateTime.now())
            .setTripId(trip.getId())
            .setIsUnfSend(false)
            .setTripTypeCode(trip.getTripTypeCode())
            .setLoadingComplete(false)
            .setPaymentContractorId(trip.getPaymentContractorId())
            .setPageCarrierUrlIsAccess(true)
            .setIsDownloadedAdvanceApplication(false)
            .setIsDownloadedContractApplication(true)
            .setIsPaid(false)
            .setIs1CSendAllowed(false)
            .setCancelledComment("")
            .setIsDownloadedContractApplication(true)
            .setUuidContractApplicationFile(tripRequestDocsUUID)
            .setIsAutomationRequest(contractor.getIsAutoAdvancePayment())
            .setAdvanceUuid(UUID.randomUUID());
        return tripRequestAdvancePayment;
    }

    private TripRequestAdvancePayment getTripRequestAdvancePaymentById(Long id) {
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
}
