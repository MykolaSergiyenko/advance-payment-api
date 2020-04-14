package online.oboz.trip.trip_carrier_advance_payment_api.service.rest;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.*;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.*;
import online.oboz.trip.trip_carrier_advance_payment_api.service.AdvanceFilterService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.integration.Integration1cService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.integration.OrdersApiService;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.AdvancePaymentCommentDTO;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.Error;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.Filter;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.FrontAdvancePaymentResponse;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.ResponseAdvancePayment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdvancePageService {
    private static final Logger log = LoggerFactory.getLogger(AdvancePageService.class);

    private final TripRepository tripRepository;
    private final OrderRepository orderRepository;
    private final Integration1cService integration1cService;
    private final AdvanceRequestRepository advanceRequestRepository;
    private final ContractorExclusionRepository contractorExclusionRepository;
    private final ContractorRepository contractorRepository;
    private final AdvanceFilterService advanceFilterService;
    private final ContractorContactRepository contractorContactRepository;
    private final OrdersApiService ordersApiService;
    private final LocationRepository locationRepository;

    public AdvancePageService(
        TripRepository tripRepository,
        OrderRepository orderRepository,
        Integration1cService integration1cService,
        AdvanceRequestRepository advanceRequestRepository,
        ContractorExclusionRepository contractorExclusionRepository,
        ContractorRepository contractorRepository,
        AdvanceFilterService advanceFilterService,
        ContractorContactRepository contractorContactRepository,
        OrdersApiService ordersApiService,
        LocationRepository locationRepository
    ) {
        this.tripRepository = tripRepository;
        this.orderRepository = orderRepository;
        this.integration1cService = integration1cService;
        this.advanceRequestRepository = advanceRequestRepository;
        this.contractorExclusionRepository = contractorExclusionRepository;
        this.contractorRepository = contractorRepository;
        this.advanceFilterService = advanceFilterService;
        this.contractorContactRepository = contractorContactRepository;
        this.ordersApiService = ordersApiService;
        this.locationRepository = locationRepository;
    }

    @Transactional
    public ResponseEntity<Void> confirmAdvancePayment(Long requestAdvansePaymentId) {
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

    public ResponseEntity<ResponseAdvancePayment> searchAdvancePaymentRequest(Filter filter) {
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

    public ResponseEntity<Void> cancelAdvancePayment(Long id, String cancelAdvanceComment) {
        TripRequestAdvancePayment entity = getTripRequestAdvancePaymentById(id);
        if (!entity.getIsCancelled()) {
            entity.setCancelledComment(cancelAdvanceComment);
            entity.setIsCancelled(true);
            advanceRequestRepository.save(entity);
            log.error(" was cancel AdvancePayment with id: {} and cancel comment: {}", id, cancelAdvanceComment);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public ResponseEntity<Void> updateLoadingComplete(Long id, Boolean loadingComplete) {
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

    public ResponseEntity<Void> changeAdvancePaymentComment(AdvancePaymentCommentDTO advancePaymentCommentDTO) {
        final TripRequestAdvancePayment entity = getTripRequestAdvancePaymentById(advancePaymentCommentDTO.getId());
        entity.setComment(advancePaymentCommentDTO.getAdvanceComment());
        advanceRequestRepository.save(entity);
        return new ResponseEntity<>(HttpStatus.OK);
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

    private TripRequestAdvancePayment getTripRequestAdvancePaymentById(Long id) {
        Optional<TripRequestAdvancePayment> tripRequestAdvancePayment = advanceRequestRepository.find(id);
        return tripRequestAdvancePayment.orElseThrow(() ->
            getBusinessLogicException("TripRequestAdvancePayment not found")
        );
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

    private BusinessLogicException getBusinessLogicException(String s) {
        online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.Error error = new Error();
        error.setErrorMessage(s);
        return new BusinessLogicException(HttpStatus.UNPROCESSABLE_ENTITY, error);
    }
}
