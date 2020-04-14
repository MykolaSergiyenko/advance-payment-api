package online.oboz.trip.trip_carrier_advance_payment_api.service;

import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.*;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.*;
import online.oboz.trip.trip_carrier_advance_payment_api.service.dto.MessageDto;
import online.oboz.trip.trip_carrier_advance_payment_api.service.integration.BStoreService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.integration.Integration1cService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.integration.OrdersApiService;
import online.oboz.trip.trip_carrier_advance_payment_api.util.SecurityUtils;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.controller.AdvancePaymentApiDelegate;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.Error;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static online.oboz.trip.trip_carrier_advance_payment_api.util.DtoUtils.getMessageDto;

@Service
public class AdvancePaymentDelegateImpl implements AdvancePaymentApiDelegate {

    private static final String COMMENT = "Данному поставщику отправлен запрос на аванс в автоматическом режиме";
    private static final Logger log = LoggerFactory.getLogger(AdvancePaymentDelegateImpl.class);
    private final AdvancePaymentCostRepository advancePaymentCostRepository;
    private final AdvanceRequestRepository advanceRequestRepository;
    private final ContractorContactRepository contractorContactRepository;
    private final ContractorExclusionRepository contractorExclusionRepository;
    private final TripRepository tripRepository;
    private final LocationRepository locationRepository;
    private final ContractorRepository contractorRepository;
    private final NotificationService notificationService;
    private final PersonRepository personRepository;
    private final ApplicationProperties applicationProperties;
    private final OrderRepository orderRepository;
    private final RestService restService;
    private final OrdersApiService ordersApiService;
    private final BStoreService bStoreService;
    private final AdvanceFilterService advanceFilterService;
    private final Integration1cService integration1cService;

    @Autowired
    public AdvancePaymentDelegateImpl(
        AdvancePaymentCostRepository advancePaymentCostRepository,
        AdvanceRequestRepository advanceRequestRepository,
        ContractorContactRepository contractorContactRepository,
        ContractorExclusionRepository contractorExclusionRepository,
        TripRepository tripRepository,
        LocationRepository locationRepository,
        ContractorRepository contractorRepository,
        NotificationService notificationService,
        PersonRepository personRepository,
        ApplicationProperties applicationProperties,
        OrderRepository orderRepository,
        RestService restService,
        BStoreService bStoreService,
        OrdersApiService ordersApiService,
        AdvanceFilterService advanceFilterService,
        Integration1cService integration1cService
    ) {
        this.advancePaymentCostRepository = advancePaymentCostRepository;
        this.advanceRequestRepository = advanceRequestRepository;
        this.contractorContactRepository = contractorContactRepository;
        this.contractorExclusionRepository = contractorExclusionRepository;
        this.tripRepository = tripRepository;
        this.locationRepository = locationRepository;
        this.contractorRepository = contractorRepository;
        this.notificationService = notificationService;
        this.personRepository = personRepository;
        this.restService = restService;
        this.applicationProperties = applicationProperties;
        this.orderRepository = orderRepository;
        this.advanceFilterService = advanceFilterService;
        this.bStoreService = bStoreService;
        this.ordersApiService = ordersApiService;
        this.integration1cService = integration1cService;
    }

    @Override
    public ResponseEntity<ResponseAdvancePayment> searchAdvancePaymentRequest(Filter filter) {
        log.info("Got searchAdvancePaymentRequest " + filter);
        Page<TripRequestAdvancePayment> tripRequestAdvancePayments = advanceFilterService.advancePayments(filter);
        List<FrontAdvancePaymentResponse> responseList = tripRequestAdvancePayments.stream().map(rec -> {
            ContractorAdvancePaymentContact contractorAdvancePaymentContact =
                contractorContactRepository.find(rec.getContractorId())
                    .orElse(new ContractorAdvancePaymentContact());
            Contractor contractor = contractorRepository.findById(rec.getContractorId()).orElse(new Contractor());
            String fullName = contractorRepository.getFullName(rec.getPaymentContractorId());
            Trip trip = tripRepository.findById(rec.getTripId()).orElse(new Trip());
            return getFrontAdvancePaymentResponse(rec, contractorAdvancePaymentContact, contractor, fullName, trip);
        }).collect(Collectors.toList());

        ResponseAdvancePayment responseAdvancePayment = new ResponseAdvancePayment();
        responseAdvancePayment.setRequestAdvancePayment(responseList);
        responseAdvancePayment.setTotal((int) tripRequestAdvancePayments.getTotalElements());
        return new ResponseEntity<>(responseAdvancePayment, HttpStatus.OK);
    }

    @Override
    @Transactional
    public ResponseEntity<Void> confirmAdvancePayment(Long requestAdvansePaymentId) {
        log.info("Got confirmAdvancePayment request " + requestAdvansePaymentId);
        TripRequestAdvancePayment tripRequestAdvancePayment = getTripRequestAdvancePaymentById(requestAdvansePaymentId);
        Trip trip = tripRepository.findById(tripRequestAdvancePayment.getTripId()).orElseThrow(() ->
            getBusinessLogicException("trip not found")
        );
        Order order = orderRepository.findById(trip.getOrderId()).orElseThrow(() ->
            getBusinessLogicException("order not found")
        );
        boolean downloadAllDocuments = isDownloadAllDocuments(trip);
        Boolean isCancelled = tripRequestAdvancePayment.getIsCancelled();
        if (downloadAllDocuments && !tripRequestAdvancePayment.getIsPushedUnfButton() && !isCancelled) {
            integration1cService.send1cNotification(requestAdvansePaymentId);
            tripRequestAdvancePayment.setIsPushedUnfButton(true);
            tripRequestAdvancePayment.setIs1CSendAllowed(false);
            tripRequestAdvancePayment.setPageCarrierUrlIsAccess(false);
            tripRepository.save(trip);
            advanceRequestRepository.save(tripRequestAdvancePayment);
            final Long orderTypeId = orderRepository.findById(
                trip.getOrderId()
            ).get().getOrderTypeId();

            ContractorAdvanceExclusion contractorAdvanceExclusion = contractorExclusionRepository
                .find(trip.getContractorId(), order.getOrderTypeId())
                .orElse(new ContractorAdvanceExclusion());
            //проверяем наличие записи контрактора в таблице исключений по типу заказа при  отсутствии  добавляем запись
            if (contractorAdvanceExclusion.getId() == null) {
                contractorAdvanceExclusion = new ContractorAdvanceExclusion();
                contractorAdvanceExclusion.setCarrierId(trip.getContractorId());
                contractorAdvanceExclusion.setIsConfirmAdvance(true);
                contractorAdvanceExclusion.setOrderTypeId(orderTypeId);
                contractorAdvanceExclusion.setCarrierFullName(
                    contractorRepository.findById(trip.getContractorId()).get().getFullName()
                );
                contractorExclusionRepository.save(contractorAdvanceExclusion);
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
        log.info("Got isAdvanced request tripId - " + tripId);
        Trip trip = tripRepository.getMotorTrip(tripId).orElseGet(Trip::new);
        if (trip.getId() == null) {
            log.info("Trip not found for tripId - " + tripId);
            return getIsAdvancedRequestResponseResponseEntity();
        }
        Order order = orderRepository.findById(trip.getOrderId()).orElseGet(Order::new);
        if (order.getId() == null) {
            log.info("Order not found for orderId - " + order.getId());
            return getIsAdvancedRequestResponseResponseEntity();
        }
        Contractor contractor = contractorRepository.findById(trip.getContractorId()).orElseGet(Contractor::new);
        if (contractor.getId() == null) {
            log.info("Contractor not found for tripId - " + trip.getContractorId());
            return getIsAdvancedRequestResponseResponseEntity();
        }
        TripRequestAdvancePayment tripRequestAdvancePayment = advanceRequestRepository.find(
            tripId, trip.getDriverId(), trip.getContractorId()
        );

        IsAdvancedRequestResponse isAdvancedRequestResponse = new IsAdvancedRequestResponse();

        Map<String, String> downloadedDocuments = ordersApiService.findTripRequestDocs(trip);
        boolean isDocsLoaded = !downloadedDocuments.isEmpty();
        isAdvancedRequestResponse.setIsDocsUploaded(isDocsLoaded);

        boolean isButtonActive = !contractor.getIsAutoAdvancePayment();
        isAdvancedRequestResponse.setIsAutoRequested(!isButtonActive);

        if (tripRequestAdvancePayment != null) {
            if (isButtonActive) {
                isButtonActive = !(tripRequestAdvancePayment.getIsCancelled() ||
                                    tripRequestAdvancePayment.getIsPushedUnfButton());
            } else {
                isAdvancedRequestResponse.setComment(COMMENT);
            }
            ContractorAdvanceExclusion contractorAdvanceExclusion = contractorExclusionRepository
                .find(trip.getContractorId(), order.getOrderTypeId())
                .orElse(new ContractorAdvanceExclusion());
            boolean isContractorLock = contractorAdvanceExclusion.getIsConfirmAdvance() != null &&
                !contractorAdvanceExclusion.getIsConfirmAdvance();
            isAdvancedRequestResponse.setTripTypeCode(tripRequestAdvancePayment.getTripTypeCode());
            isAdvancedRequestResponse.setIsContractorLock(isContractorLock);
            isAdvancedRequestResponse.setTripTypeCode(tripRequestAdvancePayment.getTripTypeCode());
            isAdvancedRequestResponse.setIsPaid(tripRequestAdvancePayment.getIsPaid());

            Long authorId = tripRequestAdvancePayment.getAuthorId();
            if (authorId != null) {
                setPersonInfo(isAdvancedRequestResponse, authorId);
            }

            isAdvancedRequestResponse.setCreatedAt(tripRequestAdvancePayment.getCreatedAt());
            log.info("isAdvancedRequestResponse for tripId: {} , DriverId: {} , ContractorId {} is: {}",
                tripId, trip.getDriverId(), trip.getContractorId(), isAdvancedRequestResponse
            );
        } else {
            if (isButtonActive) {
                isButtonActive = isDocsLoaded || !applicationProperties.getRequiredDownloadDocs();
            }
            log.info("TripRequestAdvancePayment not found for tripId: {} , DriverId: {} , ContractorId {}",
                tripId, trip.getDriverId(), trip.getContractorId()
            );
        }

        isAdvancedRequestResponse.setIsButtonActive(isButtonActive);
        return new ResponseEntity<>(isAdvancedRequestResponse, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> requestGiveAdvancePayment(Long tripId) {
        log.info("Got requestGiveAdvancePayment request tripId - " + tripId);
        tripRepository.findById(tripId).orElseThrow(() -> getBusinessLogicException("trip not found"));
        Double tripCostWithNds = tripRepository.getTripCostWithVat(tripId);
        AdvancePaymentCost advancePaymentCost = advancePaymentCostRepository.getAdvancePaymentCost(tripCostWithNds);
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
        Map<String, String> tripRequestDocs = ordersApiService.findTripRequestDocs(trip);
        if (tripRequestDocs.isEmpty()) {
            throw getBusinessLogicException("Не загружены Договор заявка / заявка ");
        }
        TripRequestAdvancePayment tripRequestAdvancePayment = advanceRequestRepository.find(
            tripId, trip.getDriverId(), contractorId
        );
        if (tripRequestAdvancePayment != null) {
            throw getBusinessLogicException("tripRequestAdvancePayment is present");
        }
        Contractor contractor = contractorRepository.findById(contractorId).orElse(new Contractor());
        String paymentContractor = contractorRepository.getFullName(trip.getPaymentContractorId());
        String tripRequestDocsUUID = tripRequestDocs.entrySet().iterator().next().getValue();
        tripRequestAdvancePayment = createTripRequestAdvancePayment(
            contractor,
            tripCostWithNds,
            advancePaymentCost,
            trip,
            tripRequestDocsUUID
        );
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
            advanceRequestRepository.save(tripRequestAdvancePayment);
            ContractorAdvanceExclusion contractorAdvanceExclusion = contractorExclusionRepository.find(
                contractorId, order.getOrderTypeId()
            ).orElseGet(ContractorAdvanceExclusion::new);

            if (contractorAdvanceExclusion.getId() == null) {
                ContractorAdvanceExclusion entity = new ContractorAdvanceExclusion();
                entity.setCarrierFullName(contractor.getFullName());
                entity.setOrderTypeId(order.getOrderTypeId());
                entity.setCarrierId(contractorId);
                entity.setIsConfirmAdvance(true);
                contractorExclusionRepository.save(entity);
            }
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> changeAdvancePaymentComment(AdvancePaymentCommentDTO advancePaymentCommentDTO) {
        log.info("Got changeAdvancePaymentComment request " + advancePaymentCommentDTO);
        final TripRequestAdvancePayment entity = getTripRequestAdvancePaymentById(advancePaymentCommentDTO.getId());
        entity.setComment(advancePaymentCommentDTO.getAdvanceComment());
        advanceRequestRepository.save(entity);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity downloadAvanceRequestTemplate(String tripNum) {
        log.info("Got downloadAvanceRequestTemplate request tripNum - " + tripNum);
        StringBuilder url = new StringBuilder();
        ResponseEntity<Resource> response;
        url.append("https://reports.oboz.com/reportserver/reportserver/httpauthexport?" +
            "key=avance_request&user=bertathar&apikey=nzybc16h&p_trip_num="
        );
        TripRequestAdvancePayment tripRequestAdvancePayment = advanceRequestRepository.find(tripNum);
        if (tripRequestAdvancePayment != null) {
            url.append(tripNum)
                .append("&p_avance_sum=")
                .append(tripRequestAdvancePayment.getAdvancePaymentSum().toString())
                .append("&p_avance_comission=")
                .append(tripRequestAdvancePayment.getRegistrationFee().toString())
                .append("&format=PDF");
            response = restService.requestResource(url.toString(), new HttpHeaders());
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
        log.info("Got downloadAvanseRequest request tripNum - " + tripNum);
        String uuidFile = advanceRequestRepository
            .find(tripNum)
            .getUuidAdvanceApplicationFile();
        return bStoreService.requestResourceFromBStore(uuidFile);
    }

    @Override
    public ResponseEntity<Resource> downloadRequest(String tripNum) {
        log.info("Got downloadRequest request tripNum - " + tripNum);
        String uuidFile = advanceRequestRepository
            .find(tripNum)
            .getUuidContractApplicationFile();
        return bStoreService.requestResourceFromBStore(uuidFile);
    }

    @Override
    public ResponseEntity downloadAvanceRequestTemplateForCarrier(String tripNum) {
        log.info("Got downloadAvanceRequestTemplateForCarrier request tripNum - " + tripNum);
        return downloadAvanceRequestTemplate(tripNum);
    }

    @Override
    public ResponseEntity<Void> uploadRequestAdvance(MultipartFile filename, String tripNum) {
        log.info("Got uploadRequestAdvance request tripNum - " + tripNum);
//        TODO :  catch   big size file and response
        Trip trip = tripRepository.getTripByNum(tripNum).orElseThrow(() -> getBusinessLogicException("trip not found"));
        Long tripId = trip.getId();
        TripRequestAdvancePayment tripRequestAdvancePayment = advanceRequestRepository.find(
            tripId, trip.getDriverId(), trip.getContractorId()
        );
        if (tripRequestAdvancePayment == null) {
            throw getBusinessLogicException("tripRequestAdvancePayment not found");
        }

        if (tripRequestAdvancePayment.getPushButtonAt() == null) {
            throw getBusinessLogicException("PushButtonAt need is first");
        }

        if (tripRequestAdvancePayment.getIsPushedUnfButton()) {
            throw getBusinessLogicException("uploadRequestAdvance forbidden");
        }


        String fileUuid = bStoreService.getFileUuid(filename);
        if (fileUuid != null) {
            if (ordersApiService.saveTripDocuments(trip.getOrderId(), trip.getId(), fileUuid)) {
                tripRequestAdvancePayment.setUuidAdvanceApplicationFile(fileUuid);
                tripRequestAdvancePayment.setIsDownloadedAdvanceApplication(true);
                advanceRequestRepository.save(tripRequestAdvancePayment);
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        log.error("uploadRequestAvance fail. ");
        throw getBusinessLogicException("uploadRequestAvance fail. ");
    }

    @Override
    public ResponseEntity<Void> uploadRequestAvanceForCarrier(MultipartFile filename, String trip_num) {
        log.info("Got uploadRequestAvanceForCarrier request tripNum - " + trip_num);
        uploadRequestAdvance(filename, trip_num);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> addContactCarrier(CarrierContactDTO carrierContactDTO) {
        log.info("Got addContactCarrier request " + carrierContactDTO);
        Optional<ContractorAdvancePaymentContact> contractorAdvancePaymentContact = contractorContactRepository.find(
            carrierContactDTO.getContractorId()
        );
        if (contractorAdvancePaymentContact.isPresent()) {
            throw getBusinessLogicException("ContractorAdvancePaymentContact is present");
        }
        final ContractorAdvancePaymentContact entity = new ContractorAdvancePaymentContact();
        entity.setFullName(carrierContactDTO.getFullName());
        entity.setContractorId(carrierContactDTO.getContractorId());
        entity.setPhone(carrierContactDTO.getPhoneNumber());
        entity.setEmail(carrierContactDTO.getEmail());
        contractorContactRepository.save(entity);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> updateLoadingComplete(Long id, Boolean loadingComplete) {
        log.info("Got updateLoadingComplete request AdvanceRequestId - {} loadingComplete - {}", id, loadingComplete);
        TripRequestAdvancePayment entity = getTripRequestAdvancePaymentById(id);
        Trip tripIgnore = tripRepository.getNotApproveTrip(entity.getTripId()).orElseGet(Trip::new);
        if (!loadingComplete) {
            entity.setIs1CSendAllowed(false);
            log.error("setIs1CSendAllowed is false");

        } else if (!entity.getIsPushedUnfButton() &&
            tripIgnore.getTripStatusCode() == null &&
            entity.getIsDownloadedAdvanceApplication() &&
            entity.getIsDownloadedContractApplication()
        ) {
            entity.setIs1CSendAllowed(true);
            log.error("setIs1CSendAllowed is true ");
        }
        entity.setLoadingComplete(loadingComplete);
        advanceRequestRepository.save(entity);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> updateContactCarrier(CarrierContactDTO carrierContactDTO) {
        log.info("Got updateContactCarrier request " + carrierContactDTO);
        Optional<ContractorAdvancePaymentContact> contractorAdvancePaymentContact = contractorContactRepository.find(
            carrierContactDTO.getContractorId()
        );
        ContractorAdvancePaymentContact entity = contractorAdvancePaymentContact.orElseThrow(() ->
            getBusinessLogicException("ContractorAdvancePaymentContact not found")
        );
        entity.setFullName(carrierContactDTO.getFullName());
        entity.setContractorId(carrierContactDTO.getContractorId());
        entity.setPhone(carrierContactDTO.getPhoneNumber());
        entity.setEmail(carrierContactDTO.getEmail());
        contractorContactRepository.save(entity);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> cancelAdvancePayment(Long id, String cancelAdvanceComment) {
        log.info("Got cancelAdvancePayment request AdvanceRequestId - {} cancelAdvanceComment - {}",
            id, cancelAdvanceComment
        );
        TripRequestAdvancePayment entity = getTripRequestAdvancePaymentById(id);
        if (!entity.getIsCancelled()) {
            entity.setCancelledComment(cancelAdvanceComment);
            entity.setIsCancelled(true);
            advanceRequestRepository.save(entity);
            log.error(" was cancel AdvancePayment with id: {} and cancel comment: {}", id, cancelAdvanceComment);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CarrierContactDTO> getContactCarrier(Long contractorId) {
        log.info("Got getContactCarrier request contractorId -" + contractorId);
        ContractorAdvancePaymentContact contact = contractorContactRepository
            .find(contractorId)
            .orElseGet(ContractorAdvancePaymentContact::new);
        CarrierContactDTO carrierContactDTO = getCarrierContactDTO(contact);
        return new ResponseEntity<>(carrierContactDTO, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> requestGiveAdvancePaymentForCarrier(UUID uuid) {
        log.info("Got requestGiveAdvancePaymentForCarrier request uuid - " + uuid);
        TripRequestAdvancePayment t = getTripRequestAdvancePaymentByUUID(uuid);
        if (!t.getPageCarrierUrlIsAccess()) {
            log.error("PageCarrierUrlIsAccess is false");
            throw getBusinessLogicException("PageCarrierUrlIsAccess is false");
        }
        if (t.getPushButtonAt() == null && !t.getIsCancelled()) {
            t.setPushButtonAt(OffsetDateTime.now());
            advanceRequestRepository.save(t);
            log.error("save ok for advance request with uuid: {} ,PushButtonAt: {}, CancelAdvance: {} ",
                uuid, t.getPushButtonAt(), t.getIsCancelled()
            );
        } else {
            log.error("Button already pushed or request was cancel");
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<FrontAdvancePaymentResponse> searchAdvancePaymentRequestByUuid(UUID uuid) {
        log.info("Got searchAdvancePaymentRequestByUuid request uuid - " + uuid);
        FrontAdvancePaymentResponse frontAdvancePaymentResponse = new FrontAdvancePaymentResponse();
        TripRequestAdvancePayment t = getTripRequestAdvancePaymentByUUID(uuid);
        if (t.getPageCarrierUrlIsAccess()) {
            frontAdvancePaymentResponse.setPushButtonAt(t.getPushButtonAt());
            frontAdvancePaymentResponse.setUrlAdvanceApplication(t.getUuidAdvanceApplicationFile());
            frontAdvancePaymentResponse.setTripCostWithVat(t.getTripCost());
            frontAdvancePaymentResponse.setAdvancePaymentSum(t.getAdvancePaymentSum());
            frontAdvancePaymentResponse.setRegistrationFee(t.getRegistrationFee());
            frontAdvancePaymentResponse.setIsCancelled(t.getIsCancelled());
            frontAdvancePaymentResponse.setIsPushedUnfButton(t.getIsPushedUnfButton());
            setTripInfo(frontAdvancePaymentResponse, t.getTripId());
        } else {
            frontAdvancePaymentResponse.setPageCarrierUrlIsAccess(t.getPageCarrierUrlIsAccess());
            new ResponseEntity<>(frontAdvancePaymentResponse, HttpStatus.OK);
            log.info("PageCarrierUrlIsAccess is false");
        }
        return new ResponseEntity<>(frontAdvancePaymentResponse, HttpStatus.OK);
    }

    private void setTripInfo(FrontAdvancePaymentResponse frontAdvancePaymentResponse, Long tripId) {
        Trip trip = tripRepository.findById(tripId).orElse(new Trip());
        setTripInfo(frontAdvancePaymentResponse, trip);
    }

    private void setTripInfo(FrontAdvancePaymentResponse frontAdvancePaymentResponse, Trip trip) {
        frontAdvancePaymentResponse.setTripNum(trip.getNum());
        TripInfo tripInfo = trip.getTripInfo();

        Location locOrigin = locationRepository.find(tripInfo.getOriginPlaceId()).orElse(new Location());
        frontAdvancePaymentResponse.setLoadingDate(tripInfo.getStartDate());
        frontAdvancePaymentResponse.setLoadingTz(locOrigin.getLocationTz());
        frontAdvancePaymentResponse.setFirstLoadingAddress(locOrigin.getAddress());

        Location locDest = locationRepository.find(tripInfo.getDestinationPlaceId()).orElse(new Location());
        frontAdvancePaymentResponse.setUnloadingDate(tripInfo.getEndDate());
        frontAdvancePaymentResponse.setUnloadingTz(locDest.getLocationTz());
        frontAdvancePaymentResponse.setLastUnloadingAddress(locDest.getAddress());
    }

    private void setPersonInfo(IsAdvancedRequestResponse isAdvancedRequestResponse, Long authorId) {
        Person author = personRepository.findById(authorId).orElse(new Person());
        isAdvancedRequestResponse.setFirstName(author.getFirstName());
        isAdvancedRequestResponse.setLastName(author.getLastName());
        isAdvancedRequestResponse.setMiddleName(author.getMiddleName());
        isAdvancedRequestResponse.setAuthorId(authorId);
    }

    private ResponseEntity<IsAdvancedRequestResponse> getIsAdvancedRequestResponseResponseEntity() {
        return new ResponseEntity<>(new IsAdvancedRequestResponse(), HttpStatus.OK);
    }

    private TripRequestAdvancePayment getTripRequestAdvancePaymentByUUID(UUID uuid) {
        return advanceRequestRepository.find(uuid).orElseThrow(() ->
            getBusinessLogicException("AdvancePaymentRequest not found")
        );
    }

    private BusinessLogicException getBusinessLogicException(String s) {
        Error error = new Error();
        error.setErrorMessage(s);
        return new BusinessLogicException(HttpStatus.UNPROCESSABLE_ENTITY, error);
    }

    private FrontAdvancePaymentResponse getFrontAdvancePaymentResponse(
        TripRequestAdvancePayment rec,
        ContractorAdvancePaymentContact contractorAdvancePaymentContact,
        Contractor contractor,
        String contractorPaymentName,
        Trip trip
    ) {
        FrontAdvancePaymentResponse frontAdvancePaymentResponse = new FrontAdvancePaymentResponse();
        frontAdvancePaymentResponse
            .id(rec.getId())
            .tripId(rec.getTripId())
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
            .isUnfSend(rec.getIsUnfSend())
            .pushButtonAt(rec.getPushButtonAt())
            .isPaid(rec.getIsPaid())
            .paidAt(rec.getPaidAt())
            .comment(rec.getComment())
            .isCancelled(rec.getIsCancelled())
            .cancelledComment(rec.getCancelledComment())
            .authorId(rec.getAuthorId())
            .pageCarrierUrlIsAccess(rec.getPageCarrierUrlIsAccess());
        setTripInfo(frontAdvancePaymentResponse, trip);
        return frontAdvancePaymentResponse;
    }

    private Boolean isDownloadAllDocuments(Trip trip) {
//        использовать только в confirm
        Map<String, String> fileRequestUuidMap = ordersApiService.findTripRequestDocs(trip);
        Map<String, String> fileAdvanceRequestUuidMap = ordersApiService.findAdvanceRequestDocs(trip);
        String requestFileUuid = Optional
            .ofNullable(fileRequestUuidMap.get("request"))
            .orElse(fileRequestUuidMap.get("trip_request"));
        String advanceRequestFileUuid = fileAdvanceRequestUuidMap.get("assignment_advance_request");
        boolean isAllDocsUpload = requestFileUuid != null && advanceRequestFileUuid != null;
        if (!isAllDocsUpload) {
            log.info("Не загружены документы. " + trip.getId());
        }
        return isAllDocsUpload;
    }

    private TripRequestAdvancePayment createTripRequestAdvancePayment(
        Contractor contractor,
        Double tripCostWithNds,
        AdvancePaymentCost advancePaymentCost,
        Trip trip,
        String tripRequestDocsUUID
    ) {
        TripRequestAdvancePayment tripRequestAdvancePayment = new TripRequestAdvancePayment();
        tripRequestAdvancePayment.setAuthorId(SecurityUtils.getAuthPersonId())
            .setTripId(advancePaymentCost.getId())
            .setIsPushedUnfButton(false)
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
        Optional<TripRequestAdvancePayment> tripRequestAdvancePayment = advanceRequestRepository.find(id);
        return tripRequestAdvancePayment.orElseThrow(() ->
            getBusinessLogicException("TripRequestAdvancePayment not found")
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
        return contractorContactRepository.find(contractorId).orElse(new ContractorAdvancePaymentContact());
    }
}
