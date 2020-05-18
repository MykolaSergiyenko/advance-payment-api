package online.oboz.trip.trip_carrier_advance_payment_api.service.rest;

import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.*;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.*;
import online.oboz.trip.trip_carrier_advance_payment_api.service.NotificationService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.RestService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.integration.BStoreService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.integration.OrdersApiService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.NewNotificationService;
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

@Service
public class DispatcherPageService {
    private static final Logger log = LoggerFactory.getLogger(DispatcherPageService.class);
    private static final String COMMENT = "Данному поставщику отправлен запрос на аванс в автоматическом режиме";
    private final TripRepository tripRepository;
    private final TripAdvanceRepository advanceRepository;
    private final OrdersApiService ordersApiService;
    private final BStoreService bStoreService;
    private final OrderRepository orderRepository;
    private final ContractorRepository contractorRepository;
    private final ContractorExclusionRepository contractorExclusionRepository;
    private final ApplicationProperties applicationProperties;
    private final PersonRepository personRepository;
    private final RestService restService;
    private final NotificationService notificationService;
    private final NewNotificationService newNotificationService;
    private final AdvancePaymentCostRepository advancePaymentCostRepository;
    private final AdvanceContactRepository advanceContactRepository;

    public DispatcherPageService(
        TripRepository tripRepository,
        TripAdvanceRepository advanceRepository,
        OrdersApiService ordersApiService,
        BStoreService bStoreService,
        OrderRepository orderRepository,
        ContractorRepository contractorRepository,
        ContractorExclusionRepository contractorExclusionRepository,
        ApplicationProperties applicationProperties,
        PersonRepository personRepository,
        RestService restService,
        NotificationService notificationService,
        NewNotificationService newNotificationService, AdvancePaymentCostRepository advancePaymentCostRepository,
        AdvanceContactRepository advanceContactRepository
    ) {
        this.tripRepository = tripRepository;
        this.advanceRepository = advanceRepository;
        this.ordersApiService = ordersApiService;
        this.bStoreService = bStoreService;
        this.orderRepository = orderRepository;
        this.contractorRepository = contractorRepository;
        this.contractorExclusionRepository = contractorExclusionRepository;
        this.applicationProperties = applicationProperties;
        this.personRepository = personRepository;
        this.restService = restService;
        this.notificationService = notificationService;
        this.newNotificationService = newNotificationService;
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
        TripAdvance tripAdvance = advanceRepository.find(
            tripId, trip.getDriverId(), trip.getContractorId()
        );

        IsAdvancedRequestResponse isAdvancedRequestResponse = new IsAdvancedRequestResponse();

        Map<String, String> downloadedDocuments = ordersApiService.findTripRequestDocs(trip);
        boolean isDocsLoaded = !downloadedDocuments.isEmpty();
        isAdvancedRequestResponse.setIsDocsUploaded(isDocsLoaded);

        boolean isButtonActive;
        isAdvancedRequestResponse.setIsAutoRequested(contractor.getIsAutoAdvancePayment());

        if (tripAdvance != null) {
            isButtonActive = false;
            ContractorAdvanceExclusion contractorAdvanceExclusion = contractorExclusionRepository
                .find(trip.getContractorId(), order.getOrderTypeId())
                .orElse(new ContractorAdvanceExclusion());
            boolean isContractorLock = contractorAdvanceExclusion.getIsConfirmAdvance() != null &&
                !contractorAdvanceExclusion.getIsConfirmAdvance();
            isAdvancedRequestResponse.setTripTypeCode(tripAdvance.getTripTypeCode());
            isAdvancedRequestResponse.setIsContractorLock(isContractorLock);
            isAdvancedRequestResponse.setTripTypeCode(tripAdvance.getTripTypeCode());
            isAdvancedRequestResponse.setIsPaid(tripAdvance.getIsPaid());

            Long authorId = tripAdvance.getAuthorId();
            if (authorId != null) {
                setPersonInfo(isAdvancedRequestResponse, authorId);
            }

            if (tripAdvance.getIsCancelled()) {
                isButtonActive = true;
            }

            if (isContractorLock || tripAdvance.getIsPushedUnfButton()) {
                isButtonActive = false;
            }

            if (tripAdvance.getIsAuto()) {
                isButtonActive = false;
                isAdvancedRequestResponse.setComment(COMMENT);
            }

            isAdvancedRequestResponse.setCreatedAt(tripAdvance.getCreatedAt());
            log.info("isAdvancedRequestResponse for tripId: {} , DriverId: {} , ContractorId {} is: {}",
                tripId, trip.getDriverId(), trip.getContractorId(), isAdvancedRequestResponse
            );
        } else {
            if (contractor.getIsAutoAdvancePayment()) {
                isButtonActive = false;
            } else {
                isButtonActive = isDocsLoaded || !applicationProperties.getRequiredDownloadDocs();
            }
            log.info("tripAdvance not found for tripId: {} , DriverId: {} , ContractorId {}",
                tripId, trip.getDriverId(), trip.getContractorId()
            );
        }

        isAdvancedRequestResponse.setIsButtonActive(isButtonActive);
        return new ResponseEntity<>(isAdvancedRequestResponse, HttpStatus.OK);
    }

    public ResponseEntity<Void> requestGiveAdvancePayment(Long tripId) {

        // Request motor, assigned Trip, its Contacts, Orders...
        //        if (tripAdvance != null) {
        //  throw getBusinessLogicException("tripAdvance is present");
        //}
        // create new TripAdvance() entity? by tripId
        //
        // set it's fields
        // Count vats?
        // save it

        // check docs downloaded via openApi

        //



        TripAdvance tripAdvance = advanceRepository.find(tripId).orElse(new TripAdvance());


        Map<String, String> tripRequestDocs = ordersApiService.findTripRequestDocs(tripAdvance.getTrip());
        if (tripRequestDocs.isEmpty()) {
            throw getBusinessLogicException("Не загружены Договор заявка / заявка ");
        }




        //Order trip-type?
        // getTripTypeCode() of order?
        // what is exclusion?
//        ContractorAdvanceExclusion contractorAdvanceExclusion = contractorExclusionRepository.find(
//            tripAdvance.getContractorId(),
//            tripAdvance.getTrip().getOrder().getOrderTypeId()
//        ).orElseGet(ContractorAdvanceExclusion::new);
//
//        if (contractorAdvanceExclusion.getId() == null) {
//             contractorAdvanceExclusion =
//                new ContractorAdvanceExclusion();
//                contractorExclusionRepository.save(contractorAdvanceExclusion);
//        }

        // Notificate about new advance created
        newNotificationService.notificate(tripAdvance);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public ResponseEntity<Void> uploadRequestAdvance(MultipartFile filename, String tripNum) {
//        TODO :  catch   big size file and response
        Trip trip = tripRepository.getTripByNum(tripNum).orElseThrow(() -> getBusinessLogicException("trip not found"));
        Long tripId = trip.getId();
        TripAdvance tripAdvance = advanceRepository.find(
            tripId, trip.getDriverId(), trip.getContractorId()
        );
        if (tripAdvance == null) {
            throw getBusinessLogicException("tripAdvance not found");
        }

        if (tripAdvance.getPushButtonAt() == null) {
            throw getBusinessLogicException("PushButtonAt need is first");
        }

        if (tripAdvance.getIsPushedUnfButton()) {
            throw getBusinessLogicException("uploadRequestAdvance forbidden");
        }

        log.info("Save RequestAdvance file to bstore for trip " + tripNum);
        String fileUuid = bStoreService.getFileUuid(filename);
        if (fileUuid != null) {
            if (ordersApiService.saveTripDocuments(trip.getOrderId(), trip.getId(), fileUuid)) {
                tripAdvance.setUuidAdvanceApplicationFile(fileUuid);
                tripAdvance.setIsDownloadedAdvanceApplication(true);
                advanceRepository.save(tripAdvance);
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

        //TODO: check it
        // сумма и пеня в урле?
        TripAdvance tripAdvance = advanceRepository.find(tripNum);
        if (tripAdvance != null) {
            url.append(tripNum)
                .append("&p_avance_sum=")
                .append(tripAdvance.getAdvancePaymentSum().toString())
                .append("&p_avance_comission=")
                .append(tripAdvance.getRegistrationFee().toString())
                .append("&format=PDF");
            response = restService.requestResource(url.toString(), new HttpHeaders());
            if (response != null) {
                log.info("report server response  Headers is: {}", response.getHeaders().entrySet().toString());
                return response;
            }
        }
        log.error("server {} returned bad response, tripAdvance is: {}", url, tripAdvance);
        return null;
    }

    public ResponseEntity<Void> addContactCarrier(CarrierContactDTO carrierContactDTO) {
        Optional<AdvanceContact> contractorAdvancePaymentContact = advanceContactRepository.find(
            carrierContactDTO.getContractorId()
        );
        if (contractorAdvancePaymentContact.isPresent()) {
            throw getBusinessLogicException("ContractorAdvancePaymentContact is present");
        }
        final AdvanceContact entity = new AdvanceContact();
        entity.setFullName(carrierContactDTO.getFullName());
        entity.setContractorId(carrierContactDTO.getContractorId());
        entity.setPhone(carrierContactDTO.getPhoneNumber());
        entity.setEmail(carrierContactDTO.getEmail());
        advanceContactRepository.save(entity);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public ResponseEntity<Void> updateContactCarrier(CarrierContactDTO carrierContactDTO) {
        Optional<AdvanceContact> contractorAdvancePaymentContact = advanceContactRepository.find(
            carrierContactDTO.getContractorId()
        );
        AdvanceContact entity = contractorAdvancePaymentContact.orElseThrow(() ->
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
        AdvanceContact contact = advanceContactRepository
            .find(contractorId)
            .orElseGet(AdvanceContact::new);
        CarrierContactDTO carrierContactDTO = getCarrierContactDTO(contact);
        return new ResponseEntity<>(carrierContactDTO, HttpStatus.OK);
    }

    private CarrierContactDTO getCarrierContactDTO(AdvanceContact advanceContact) {
        CarrierContactDTO carrierContactDTO = new CarrierContactDTO();
        carrierContactDTO.setContractorId(advanceContact.getContractorId());
        carrierContactDTO.setEmail(advanceContact.getEmail());
        carrierContactDTO.setFullName(advanceContact.getFullName());
        carrierContactDTO.setPhoneNumber(advanceContact.getPhone());
        return carrierContactDTO;
    }

    private TripAdvance createTripAdvance(
        Boolean isAuto,
        Double tripCostWithNds,
        AdvancePaymentCostDict advancePaymentCostDict,
        Trip trip,
        String tripRequestDocsUUID
    ) {
        TripAdvance tripAdvance = new TripAdvance()
            .setAuthorId(SecurityUtils.getAuthPersonId().longValue())
            .setTripId(advancePaymentCostDict.getId())
            .setIsPushedUnfButton(false)
            .setTripCost(tripCostWithNds)
            .setAdvancePaymentSum(advancePaymentCostDict.getAdvancePaymentSum())
            .setRegistrationFee(advancePaymentCostDict.getRegistrationFee())
            .setIsCancelled(false)
            .setContractorId(trip.getContractorId())
            .setDriverId(trip.getDriverId())
            .setCreatedAt(OffsetDateTime.now())
            .setPushButtonAt(OffsetDateTime.now())
            .setTripId(trip.getId())
            .setUnfSend(false)
            .setIsAuto(isAuto)
            .setAdvanceUuid(UUID.randomUUID())
            .setIs1CSendAllowed(false)
            .setIsSmsSent(false)
            .setIsEmailRead(false)
            .setCancelledComment("")
            .setIsDownloadedContractApplication(true)
            .setUuidContractApplicationFile(tripRequestDocsUUID);
           // .setTripTypeCode(trip.getTripTypeCode())
           // .setLoadingComplete(false)
//            .setPaymentContractorId(trip.getPaymentContractorId())
//            .setPageCarrierUrlIsAccess(true)
//            .setIsDownloadedAdvanceApplication(false)
//            .setIsDownloadedContractApplication(true)
//            .setIsPaid(false)
        return tripAdvance;
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

    private BusinessLogicException getBusinessLogicException(String s) {
        Error error = new Error();
        error.setErrorMessage(s);
        return new BusinessLogicException(HttpStatus.UNPROCESSABLE_ENTITY, error);
    }
}
