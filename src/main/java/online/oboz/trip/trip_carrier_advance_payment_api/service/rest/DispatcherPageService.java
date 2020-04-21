package online.oboz.trip.trip_carrier_advance_payment_api.service.rest;

import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.*;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.*;
import online.oboz.trip.trip_carrier_advance_payment_api.service.NotificationService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.RestService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.dto.MessageDto;
import online.oboz.trip.trip_carrier_advance_payment_api.service.integration.BStoreService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.integration.OrdersApiService;
import online.oboz.trip.trip_carrier_advance_payment_api.util.SecurityUtils;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.CarrierContactDTO;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.Error;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.IsAdvancedRequestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static online.oboz.trip.trip_carrier_advance_payment_api.util.DtoUtils.getMessageDto;

@Service
public class DispatcherPageService {
    private static final Logger log = LoggerFactory.getLogger(DispatcherPageService.class);
    private static final String COMMENT = "Данному поставщику отправлен запрос на аванс в автоматическом режиме";

    private final TripRepository tripRepository;
    private final AdvanceRequestRepository advanceRequestRepository;
    private final OrdersApiService ordersApiService;
    private final BStoreService bStoreService;
    private final OrderRepository orderRepository;
    private final ContractorRepository contractorRepository;
    private final ContractorExclusionRepository contractorExclusionRepository;
    private final ApplicationProperties applicationProperties;
    private final PersonRepository personRepository;
    private final RestService restService;
    private final NotificationService notificationService;
    private final AdvancePaymentCostRepository advancePaymentCostRepository;
    private final AdvanceContactRepository advanceContactRepository;

    public DispatcherPageService(
        TripRepository tripRepository,
        AdvanceRequestRepository advanceRequestRepository,
        OrdersApiService ordersApiService,
        BStoreService bStoreService,
        OrderRepository orderRepository,
        ContractorRepository contractorRepository,
        ContractorExclusionRepository contractorExclusionRepository,
        ApplicationProperties applicationProperties,
        PersonRepository personRepository,
        RestService restService,
        NotificationService notificationService,
        AdvancePaymentCostRepository advancePaymentCostRepository,
        AdvanceContactRepository advanceContactRepository
    ) {
        this.tripRepository = tripRepository;
        this.advanceRequestRepository = advanceRequestRepository;
        this.ordersApiService = ordersApiService;
        this.bStoreService = bStoreService;
        this.orderRepository = orderRepository;
        this.contractorRepository = contractorRepository;
        this.contractorExclusionRepository = contractorExclusionRepository;
        this.applicationProperties = applicationProperties;
        this.personRepository = personRepository;
        this.restService = restService;
        this.notificationService = notificationService;
        this.advancePaymentCostRepository = advancePaymentCostRepository;
        this.advanceContactRepository = advanceContactRepository;
    }

    public ResponseEntity<IsAdvancedRequestResponse> isAdvanced(Long tripId) {
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

        boolean isButtonActive;
        isAdvancedRequestResponse.setIsAutoRequested(contractor.getIsAutoAdvancePayment());

        if (tripRequestAdvancePayment != null) {
            isButtonActive = false;
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

            if (tripRequestAdvancePayment.getIsCancelled()) {
                isButtonActive = true;
            }

            if (isContractorLock || tripRequestAdvancePayment.getIsPushedUnfButton()) {
                isButtonActive = false;
            }

            if (tripRequestAdvancePayment.getIsAutomationRequest()) {
                isButtonActive = false;
                isAdvancedRequestResponse.setComment(COMMENT);
            }

            isAdvancedRequestResponse.setCreatedAt(tripRequestAdvancePayment.getCreatedAt());
            log.info("isAdvancedRequestResponse for tripId: {} , DriverId: {} , ContractorId {} is: {}",
                tripId, trip.getDriverId(), trip.getContractorId(), isAdvancedRequestResponse
            );
        } else {
            if (contractor.getIsAutoAdvancePayment()) {
                isButtonActive = false;
            } else {
                isButtonActive = isDocsLoaded || !applicationProperties.getRequiredDownloadDocs();
            }
            log.info("TripRequestAdvancePayment not found for tripId: {} , DriverId: {} , ContractorId {}",
                tripId, trip.getDriverId(), trip.getContractorId()
            );
        }

        isAdvancedRequestResponse.setIsButtonActive(isButtonActive);
        return new ResponseEntity<>(isAdvancedRequestResponse, HttpStatus.OK);
    }

    public ResponseEntity<Void> requestGiveAdvancePayment(Long tripId) {
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
                log.info("set email to queue " + messageDto);
                notificationService.sendEmail(messageDto);
            }
            if (contact.getPhone() != null) {
                log.info("set sms to queue " + messageDto);
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

    public ResponseEntity<Void> uploadRequestAdvance(MultipartFile filename, String tripNum) {
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

    public ResponseEntity downloadAvanceRequestTemplate(String tripNum) {
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

    public ResponseEntity<Void> addContactCarrier(CarrierContactDTO carrierContactDTO) {
        Optional<ContractorAdvancePaymentContact> contractorAdvancePaymentContact = advanceContactRepository.find(
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
        advanceContactRepository.save(entity);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public ResponseEntity<Void> updateContactCarrier(CarrierContactDTO carrierContactDTO) {
        Optional<ContractorAdvancePaymentContact> contractorAdvancePaymentContact = advanceContactRepository.find(
            carrierContactDTO.getContractorId()
        );
        ContractorAdvancePaymentContact entity = contractorAdvancePaymentContact.orElseThrow(() ->
            getBusinessLogicException("ContractorAdvancePaymentContact not found")
        );
        entity.setFullName(carrierContactDTO.getFullName());
        entity.setContractorId(carrierContactDTO.getContractorId());
        entity.setPhone(carrierContactDTO.getPhoneNumber());
        entity.setEmail(carrierContactDTO.getEmail());
        advanceContactRepository.save(entity);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public ResponseEntity<CarrierContactDTO> getContactCarrier(Long contractorId) {
        ContractorAdvancePaymentContact contact = advanceContactRepository
            .find(contractorId)
            .orElseGet(ContractorAdvancePaymentContact::new);
        CarrierContactDTO carrierContactDTO = getCarrierContactDTO(contact);
        return new ResponseEntity<>(carrierContactDTO, HttpStatus.OK);
    }

    private CarrierContactDTO getCarrierContactDTO(ContractorAdvancePaymentContact contractorAdvancePaymentContact) {
        CarrierContactDTO carrierContactDTO = new CarrierContactDTO();
        carrierContactDTO.setContractorId(contractorAdvancePaymentContact.getContractorId());
        carrierContactDTO.setEmail(contractorAdvancePaymentContact.getEmail());
        carrierContactDTO.setFullName(contractorAdvancePaymentContact.getFullName());
        carrierContactDTO.setPhoneNumber(contractorAdvancePaymentContact.getPhone());
        return carrierContactDTO;
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

    private void setPersonInfo(IsAdvancedRequestResponse isAdvancedRequestResponse, Long authorId) {
        Person author = personRepository.findById(authorId).orElse(new Person());
        isAdvancedRequestResponse.setFirstName(author.getFirstName());
        isAdvancedRequestResponse.setLastName(author.getLastName());
        isAdvancedRequestResponse.setMiddleName(author.getMiddleName());
        isAdvancedRequestResponse.setAuthorId(authorId);
    }

    private ContractorAdvancePaymentContact getAdvancePaymentContact(Long contractorId) {
        return advanceContactRepository.find(contractorId).orElse(new ContractorAdvancePaymentContact());
    }

    private ResponseEntity<IsAdvancedRequestResponse> getIsAdvancedRequestResponseResponseEntity() {
        return new ResponseEntity<>(new IsAdvancedRequestResponse(), HttpStatus.OK);
    }

    private BusinessLogicException getBusinessLogicException(String s) {
        Error error = new Error();
        error.setErrorMessage(s);
        return new BusinessLogicException(HttpStatus.UNPROCESSABLE_ENTITY, error);
    }
}
