package online.oboz.trip.trip_carrier_advance_payment_api.service.rest;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.ContractorAdvanceExclusion;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.Order;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.Trip;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.TripRequestAdvancePayment;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.*;
import online.oboz.trip.trip_carrier_advance_payment_api.service.integration.Integration1cService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.integration.OrdersApiService;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.AdvancePaymentCommentDTO;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.Error;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Map;
import java.util.Optional;

@Service
public class AdvancePageService {
    private static final Logger log = LoggerFactory.getLogger(AdvancePageService.class);

    private final TripRepository tripRepository;
    private final OrderRepository orderRepository;
    private final Integration1cService integration1cService;
    private final AdvanceRequestRepository advanceRequestRepository;
    private final ContractorExclusionRepository contractorExclusionRepository;
    private final ContractorRepository contractorRepository;
    private final OrdersApiService ordersApiService;

    public AdvancePageService(
        TripRepository tripRepository,
        OrderRepository orderRepository,
        Integration1cService integration1cService,
        AdvanceRequestRepository advanceRequestRepository,
        ContractorExclusionRepository contractorExclusionRepository,
        ContractorRepository contractorRepository,
        OrdersApiService ordersApiService
    ) {
        this.tripRepository = tripRepository;
        this.orderRepository = orderRepository;
        this.integration1cService = integration1cService;
        this.advanceRequestRepository = advanceRequestRepository;
        this.contractorExclusionRepository = contractorExclusionRepository;
        this.contractorRepository = contractorRepository;
        this.ordersApiService = ordersApiService;
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

    private BusinessLogicException getBusinessLogicException(String s) {
        online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.Error error = new Error();
        error.setErrorMessage(s);
        return new BusinessLogicException(HttpStatus.UNPROCESSABLE_ENTITY, error);
    }
}
