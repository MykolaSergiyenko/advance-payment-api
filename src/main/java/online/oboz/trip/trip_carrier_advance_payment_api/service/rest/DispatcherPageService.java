package online.oboz.trip.trip_carrier_advance_payment_api.service.rest;

import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
//import online.oboz.trip.trip_carrier_advance_payment_api.domain.base.trip.people.Person;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.Trip;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.*;
import online.oboz.trip.trip_carrier_advance_payment_api.service.AdvanceService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.contacts.AdvanceContactService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.NotificationService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.RestService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.fileapps.ReportsTemplateService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.integration.BStoreService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.integration.OrdersApiService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.NewNotificationService;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.CarrierContactDTO;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.Error;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.IsTripAdvanced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DispatcherPageService {
    private static final Logger log = LoggerFactory.getLogger(DispatcherPageService.class);
    //private static final String COMMENT = "Данному поставщику отправлен запрос на аванс в автоматическом режиме";
    private final AdvanceService advanceService;

    private final AdvanceRepository advanceRepository;
    private final AdvanceContactService contactService;


    private final ReportsTemplateService reportsService;


    public DispatcherPageService(
        AdvanceService advanceService, TripRepository tripRepository,
        AdvanceRepository advanceRepository,
        AdvanceContactService contactService, OrdersApiService ordersApiService,
        ReportsTemplateService reportsService, BStoreService bStoreService,
        OrderRepository orderRepository,
        ContractorRepository contractorRepository,
        //AdvanceExclusionDictRepository advanceExclusionDictRepository,
        ApplicationProperties applicationProperties,
        PersonRepository personRepository,
        RestService restService,
        NotificationService notificationService,
        NewNotificationService newNotificationService,
        AdvanceCostDictRepository advanceCostDictRepository,
        AdvanceContactsBookRepository advanceContactsBookRepository
    ) {
        this.advanceService = advanceService;

        this.advanceRepository = advanceRepository;
        this.contactService = contactService;
        this.reportsService = reportsService;

    }

    public ResponseEntity<IsTripAdvanced> isAdvanced(Long tripId) {
        Advance advance = advanceService.findByTripId(tripId);
        IsTripAdvanced isTripAdvanced = new IsTripAdvanced();
        if (advance != null) isTripAdvanced.setIsButtonActive(false);
        return new ResponseEntity<>(isTripAdvanced, HttpStatus.OK);
    }
//        Trip trip = tripRepository.getMotorTrip(tripId).orElseGet(Trip::new);
//        if (trip.getId() == null) {
//            log.info("Trip not found for tripId - " + tripId);
//            return getIsAdvancedRequestResponseResponseEntity();
//        }
//        Order order = orderRepository.findById(trip.getTripFields().getOrderId()).orElseGet(Order::new);
//        if (order.getId() == null) {
//            log.info("Order not found for orderId - " + order.getId());
//            return getIsAdvancedRequestResponseResponseEntity();
//        }
//        AdvanceContractor advanceContractor = contractorRepository.findById(trip.getContractorId()).orElseGet(AdvanceContractor::new);
//        if (advanceContractor.getId() == null) {
//            log.info("Contractor not found for tripId - " + trip.getContractorId());
//            return getIsAdvancedRequestResponseResponseEntity();
//        }
//        Advance advanceEntity = advanceRepository.findByParam(
//            tripId, trip.getDriverId(), trip.getContractorId()
//        );
//

//
//        Map<String, String> downloadedDocuments = ordersApiService.findTripRequestDocs(trip);
//        boolean isDocsLoaded = !downloadedDocuments.isEmpty();
//        isAdvancedRequestResponse.setIsDocsUploaded(isDocsLoaded);
//
//        boolean isButtonActive;
//        //isAdvancedRequestResponse.setIsAutoRequested(tripAdvanceContractor.getAutoAdvance());
//
//        if (advanceEntity != null) {
//            isButtonActive = false;
////            AdvanceExclusionDict advanceExclusionDict = advanceExclusionDictRepository
////                .find(trip.getContractorId(), tripOrder.getOrderTypeId())
////                .orElse(new AdvanceExclusionDict());
////            boolean isContractorLock = advanceExclusionDict.getIsConfirmAdvance() != null &&
////                !advanceExclusionDict.getIsConfirmAdvance();
////            isAdvancedRequestResponse.setTripTypeCode(tripAdvance.getTripTypeCode());
////            isAdvancedRequestResponse.setIsContractorLock(isContractorLock);
////            isAdvancedRequestResponse.setTripTypeCode(tripAdvance.getTripTypeCode());
////            isAdvancedRequestResponse.setIsPaid(tripAdvance.getPaid());
//
//            Long authorId = advanceEntity.getAuthorId();
//            if (authorId != null) {
//                setPersonInfo(isAdvancedRequestResponse, authorId);
//            }
//
////            if (tripAdvance.getIsCancelled()) {
////                isButtonActive = true;
////            }
//
//            if ( advanceEntity.getPushedUnfButton()) { //isContractorLock ||
//                isButtonActive = false;
//            }
//
//            if (advanceEntity.getAuto()) {
//                isButtonActive = false;
//                isAdvancedRequestResponse.setComment(COMMENT);
//            }
//
//            isAdvancedRequestResponse.setCreatedAt(advanceEntity.getCreatedAt());
//            log.info("isAdvancedRequestResponse for tripId: {} , DriverId: {} , ContractorId {} is: {}",
//                tripId, trip.getDriverId(), trip.getContractorId(), isAdvancedRequestResponse
//            );
//        } else {
//            if (contractor.getIsAutoAdvancePayment()) {
//                isButtonActive = false;
//            } else {
//                isButtonActive = isDocsLoaded || !applicationProperties.getRequiredDownloadDocs();
//            }
//            log.info("tripAdvance not found for tripId: {} , DriverId: {} , ContractorId {}",
//                tripId, trip.getDriverId(), trip.getContractorId()
//            );
//        }

        //isAdvancedRequestResponse.setIsButtonActive(isButtonActive);


    public ResponseEntity<Void> giveAdvanceForTrip(Long tripId) {
        Advance advance = advanceService.createAdvanceForTrip(tripId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public ResponseEntity<Resource> downloadAdvanceRequestTemplate(String tripNum) {
        return advanceService.downloadAvanceRequestTemplate(tripNum);
    }

    public ResponseEntity<CarrierContactDTO> getContactCarrier(Long contractorId) {
        return contactService.getContactCarrier(contractorId);
    }

    public ResponseEntity<Void> addContactCarrier(CarrierContactDTO carrierContactDTO) {
        return contactService.addContactCarrier(carrierContactDTO);
    }

    public ResponseEntity<Void> updateContactCarrier(CarrierContactDTO carrierContactDTO) {
        return contactService.updateContactCarrier(carrierContactDTO);
    }


    private void setPersonInfo(IsTripAdvanced isTripAdvanced, Long authorId) {
//        Person author = personRepository.findById(authorId).orElse(new Person());
//        isTripAdvanced.setFirstName(author.getContacts().getNames().getFirstName());
//        isTripAdvanced.setLastName(author.getLastName());
//        isTripAdvanced.setMiddleName(author.getMiddleName());
//        isTripAdvanced.setAuthorId(authorId);
    }


    private ResponseEntity<IsTripAdvanced> getIsAdvancedRequestResponseResponseEntity() {
        return new ResponseEntity<>(new IsTripAdvanced(), HttpStatus.OK);
    }

    private BusinessLogicException getDispatcherPageError(String s) {
        return  advanceService.getServiceInternalError(s, DispatcherPageService.class);
        //return new BusinessLogicException(HttpStatus.UNPROCESSABLE_ENTITY, error);
    }
}
