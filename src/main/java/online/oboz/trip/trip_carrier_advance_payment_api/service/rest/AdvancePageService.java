package online.oboz.trip.trip_carrier_advance_payment_api.service.rest;

import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.ContractorAdvanceExclusion;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.Order;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.Trip;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.TripAdvance;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.*;
import online.oboz.trip.trip_carrier_advance_payment_api.service.integration.Integration1cService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.integration.OrdersApiService;
import online.oboz.trip.trip_carrier_advance_payment_api.util.SecurityUtils;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.AdvancePaymentCommentDTO;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.Error;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.util.Map;
import java.util.Optional;

@Service
public class AdvancePageService {
    private static final Logger log = LoggerFactory.getLogger(AdvancePageService.class);

    private final TripRepository tripRepository;
    private final OrderRepository orderRepository;
    private final Integration1cService integration1cService;
    private final TripAdvanceRepository advanceRepository;
    private final ContractorExclusionRepository contractorExclusionRepository;
    private final ContractorRepository contractorRepository;
    private final OrdersApiService ordersApiService;
    private final ApplicationProperties applicationProperties;

    public AdvancePageService(
        TripRepository tripRepository,
        OrderRepository orderRepository,
        Integration1cService integration1cService,
        TripAdvanceRepository advanceRepository,
        ContractorExclusionRepository contractorExclusionRepository,
        ContractorRepository contractorRepository,
        OrdersApiService ordersApiService,
        ApplicationProperties applicationProperties) {
        this.tripRepository = tripRepository;
        this.orderRepository = orderRepository;
        this.integration1cService = integration1cService;
        this.advanceRepository = advanceRepository;
        this.contractorExclusionRepository = contractorExclusionRepository;
        this.contractorRepository = contractorRepository;
        this.ordersApiService = ordersApiService;
        this.applicationProperties = applicationProperties;
    }

    @Transactional
    public ResponseEntity<Void> confirmAdvancePayment(Long tripAdvanceId) {
        TripAdvance tripAdvance = advanceRepository.find(tripAdvanceId).orElseThrow(() ->
            getBusinessLogicException("Trip advance not found")
        );

        // TODO: Test can be nullable?
        Trip trip = tripRepository.findById(tripAdvance.getTripId()).orElseThrow(() ->
            getBusinessLogicException("trip not found")
        );

        // TODO: Test can be nullable?
        Order order = orderRepository.findById(trip.getOrderId()).orElseThrow(() ->
            getBusinessLogicException("order not found")
        );

        boolean downloadAllDocuments = isDownloadAllDocuments(trip);
        Boolean isCancelled = tripAdvance.getIsCancelled();
        if (downloadAllDocuments && !tripAdvance.getIsPushedUnfButton() && !isCancelled) {

            //TODO: Интеграция с 1с-УНФ?
            integration1cService.send1cNotification(tripAdvanceId);

            //Where is it set at default
            tripAdvance.setIsPushedUnfButton(true);
            tripAdvance.setIs1CSendAllowed(false);
            tripAdvance.setPageCarrierUrlIsAccess(false);

            //what we update in trip-db?
            tripRepository.save(trip);
            advanceRepository.save(tripAdvance);


            //why orders?
            final Long orderTypeId = orderRepository.findById(
                trip.getOrderId()
            ).get().getOrderTypeId();


            // exclusion-constraint check always?
            //проверяем наличие записи контрактора в таблице исключений по типу заказа при  отсутствии  добавляем запись

            ContractorAdvanceExclusion contractorAdvanceExclusion = contractorExclusionRepository
                .find(trip.getContractorId(), order.getOrderTypeId())
                .orElse(new ContractorAdvanceExclusion());

            if (contractorAdvanceExclusion.getId() == null) {
                contractorAdvanceExclusion = new ContractorAdvanceExclusion();
                contractorAdvanceExclusion.setCarrierId(trip.getContractorId());
                contractorAdvanceExclusion.setIsConfirmAdvance(true);
                contractorAdvanceExclusion.setOrderTypeId(orderTypeId);
                contractorAdvanceExclusion.setCarrierFullName(
                    contractorRepository.findById(trip.getContractorId()).get().getFullName()
                );

                //why update?
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
        TripAdvance entity = advanceRepository.find(id).orElseThrow(() ->
            getBusinessLogicException("Trip-advance not found.")
        );
        if (!entity.getIsCancelled()) {
            entity.setCancelledComment(cancelAdvanceComment);
            entity.setIsCancelled(true);
            advanceRepository.save(entity);
            log.error(" was cancel AdvancePayment with id: {} and cancel comment: {}", id, cancelAdvanceComment);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public ResponseEntity<Void> updateLoadingComplete(Long id, Boolean loadingComplete) {
        TripAdvance entity = advanceRepository.find(id).orElseThrow(() ->
            getBusinessLogicException("Trip-advance not found.")
        );
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
        advanceRepository.save(entity);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public ResponseEntity<Void> changeAdvancePaymentComment(AdvancePaymentCommentDTO advancePaymentCommentDTO) {
        final TripAdvance entity = advanceRepository.find(advancePaymentCommentDTO.getId()).orElseThrow(() ->
            getBusinessLogicException("Trip-advance not found.")
        );
        ;
        entity.setComment(advancePaymentCommentDTO.getAdvanceComment());
        advanceRepository.save(entity);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private Boolean isDownloadAllDocuments(Trip trip) {
        //использовать только в confirm
        //TODO: зачем это?
        Map<String, String> fileRequestUuidMap = ordersApiService.
            findTripRequestDocs(trip);
        Map<String, String> fileAdvanceRequestUuidMap = ordersApiService.
            findAdvanceRequestDocs(trip);

        String requestFileUuid = Optional
            .ofNullable(fileRequestUuidMap.get("request"))
            .orElse(fileRequestUuidMap.get("trip_request"));
        String advanceRequestFileUuid = fileAdvanceRequestUuidMap.get("assignment_advance_request");
        boolean isAllDocsUpload = requestFileUuid != null && advanceRequestFileUuid != null;
        if (!isAllDocsUpload) {
            log.info("Не загружены документы. " + trip.getId());
        }
        // TODO?
        return isAllDocsUpload;
    }

    public void checkAccess() {
        if (SecurityUtils.hasNotAccess(applicationProperties)) {
            log.info("User hasn't access.");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User permission denied");
        }
    }

    private BusinessLogicException getBusinessLogicException(String s) {
        online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.Error error = new Error();
        error.setErrorMessage(s);
        return new BusinessLogicException(HttpStatus.UNPROCESSABLE_ENTITY, error);
    }
}
