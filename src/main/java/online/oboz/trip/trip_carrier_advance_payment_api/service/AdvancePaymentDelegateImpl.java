package online.oboz.trip.trip_carrier_advance_payment_api.service;

import online.oboz.trip.trip_carrier_advance_payment_api.repository.AdvanceRequestRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.service.integration.BStoreService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.rest.AdvancePageService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.rest.CarrierPageService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.rest.DispatcherPageService;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.controller.AdvancePaymentApiDelegate;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class AdvancePaymentDelegateImpl implements AdvancePaymentApiDelegate {

    private static final Logger log = LoggerFactory.getLogger(AdvancePaymentDelegateImpl.class);
    private final AdvanceRequestRepository advanceRequestRepository;
    private final BStoreService bStoreService;
    private final DispatcherPageService dispatcherPageService;
    private final CarrierPageService carrierPageService;
    private final AdvancePageService advancePageService;

    @Autowired
    public AdvancePaymentDelegateImpl(
        AdvanceRequestRepository advanceRequestRepository,
        BStoreService bStoreService,
        CarrierPageService carrierPageService,
        DispatcherPageService dispatcherPageService,
        AdvancePageService advancePageService
    ) {
        this.advanceRequestRepository = advanceRequestRepository;
        this.bStoreService = bStoreService;
        this.dispatcherPageService = dispatcherPageService;
        this.advancePageService = advancePageService;
        this.carrierPageService = carrierPageService;
    }

    @Override
    public ResponseEntity<IsAdvancedRequestResponse> isAdvanced(Long tripId) {
        log.info("Got isAdvanced request tripId - " + tripId);
        return dispatcherPageService.isAdvanced(tripId);
    }

    @Override
    public ResponseEntity<ResponseAdvancePayment> searchAdvancePaymentRequest(Filter filter) {
        log.info("Got searchAdvancePaymentRequest " + filter);
        return advancePageService.searchAdvancePaymentRequest(filter);
    }

    @Override
    public ResponseEntity<FrontAdvancePaymentResponse> searchAdvancePaymentRequestByUuid(UUID uuid) {
        log.info("Got searchAdvancePaymentRequestByUuid request uuid - " + uuid);
        return carrierPageService.searchAdvancePaymentRequestByUuid(uuid);
    }

    @Override
    public ResponseEntity<Void> confirmAdvancePayment(Long requestAdvansePaymentId) {
        log.info("Got confirmAdvancePayment request " + requestAdvansePaymentId);
        return advancePageService.confirmAdvancePayment(requestAdvansePaymentId);
    }

    @Override
    public ResponseEntity<Void> requestGiveAdvancePayment(Long tripId) {
        log.info("Got requestGiveAdvancePayment request tripId - " + tripId);
        return dispatcherPageService.requestGiveAdvancePayment(tripId);
    }

    @Override
    public ResponseEntity<Void> requestGiveAdvancePaymentForCarrier(UUID uuid) {
        log.info("Got requestGiveAdvancePaymentForCarrier request uuid - " + uuid);
        return carrierPageService.requestGiveAdvancePaymentForCarrier(uuid);
    }

    @Override
    public ResponseEntity<Void> changeAdvancePaymentComment(AdvancePaymentCommentDTO advancePaymentCommentDTO) {
        log.info("Got changeAdvancePaymentComment request " + advancePaymentCommentDTO);
        return advancePageService.changeAdvancePaymentComment(advancePaymentCommentDTO);
    }

    @Override
    public ResponseEntity downloadAvanceRequestTemplate(String tripNum) {
        log.info("Got downloadAvanceRequestTemplate request tripNum - " + tripNum);
        return dispatcherPageService.downloadAvanceRequestTemplate(tripNum);
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
        return dispatcherPageService.uploadRequestAdvance(filename, tripNum);
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
        return dispatcherPageService.addContactCarrier(carrierContactDTO);
    }

    @Override
    public ResponseEntity<Void> updateContactCarrier(CarrierContactDTO carrierContactDTO) {
        log.info("Got updateContactCarrier request " + carrierContactDTO);
        return dispatcherPageService.updateContactCarrier(carrierContactDTO);
    }

    @Override
    public ResponseEntity<Void> updateLoadingComplete(Long id, Boolean loadingComplete) {
        log.info("Got updateLoadingComplete request AdvanceRequestId - {} loadingComplete - {}", id, loadingComplete);
        return advancePageService.updateLoadingComplete(id, loadingComplete);
    }

    @Override
    public ResponseEntity<Void> cancelAdvancePayment(Long id, String cancelAdvanceComment) {
        log.info("Got cancelAdvancePayment request AdvanceRequestId - {} cancelAdvanceComment - {}",
            id, cancelAdvanceComment
        );
        return advancePageService.cancelAdvancePayment(id, cancelAdvanceComment);
    }

    @Override
    public ResponseEntity<CarrierContactDTO> getContactCarrier(Long contractorId) {
        log.info("Got getContactCarrier request contractorId -" + contractorId);
        return dispatcherPageService.getContactCarrier(contractorId);
    }
}
