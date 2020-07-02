package online.oboz.trip.trip_carrier_advance_payment_api.service;

import online.oboz.trip.trip_carrier_advance_payment_api.repository.AdvanceRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.service.fileapps.FileAttachmentsService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.integration.BStoreService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.rest.AdvancePageService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.rest.AdvancePageTabsService;
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

    private final AdvanceService advanceService;
    private final DispatcherPageService dispatcherPageService;
    private final CarrierPageService carrierPageService;
    private final AdvancePageService advancePageService;
    private final AdvancePageTabsService advancePageTabsService;

    @Autowired
    public AdvancePaymentDelegateImpl(
        AdvanceService advanceService, CarrierPageService carrierPageService,
        DispatcherPageService dispatcherPageService,
        AdvancePageService advancePageService,
        AdvancePageTabsService advancePageTabsService
    ) {
        this.advanceService = advanceService;
        this.dispatcherPageService = dispatcherPageService;
        this.advancePageService = advancePageService;
        this.carrierPageService = carrierPageService;
        this.advancePageTabsService = advancePageTabsService;
    }

    @Override
    public ResponseEntity<IsTripAdvanced> isAdvanced(Long tripId) {
        log.info("Got isAdvanced request tripId - " + tripId);
        return dispatcherPageService.isAdvanced(tripId);
    }

    @Override
    public ResponseEntity<AdvanceDesktopDTO> searchInWorkRequests(Filter filter) {
        log.info("Got searchInWorkRequests " + filter);
        return advancePageTabsService.searchInWorkRequests(filter);
    }

    @Override
    public ResponseEntity<AdvanceDesktopDTO> searchProblemRequests(Filter filter) {
        log.info("Got searchProblemRequests " + filter);
        return advancePageTabsService.searchProblemRequests(filter);
    }

    @Override
    public ResponseEntity<AdvanceDesktopDTO> searchPaidRequests(Filter filter) {
        log.info("Got searchPaidRequests " + filter);
        return advancePageTabsService.searchPaidRequests(filter);
    }

    @Override
    public ResponseEntity<AdvanceDesktopDTO> searchNotPaidRequests(Filter filter) {
        log.info("Got searchNotPaidRequests " + filter);
        return advancePageTabsService.searchNotPaidRequests(filter);
    }

    @Override
    public ResponseEntity<AdvanceDesktopDTO> searchCanceledRequests(Filter filter) {
        log.info("Got searchCanceledRequests " + filter);
        return advancePageTabsService.searchCanceledRequests(filter);
    }

    @Override
    public ResponseEntity<CarrierPage> searchAdvancePaymentRequestByUuid(UUID uuid) {
        log.info("Got searchAdvancePaymentRequestByUuid request uuid - " + uuid);
        return carrierPageService.searchAdvancePaymentRequestByUuid(uuid);
    }

    @Override
    public ResponseEntity<Void> confirmAdvancePayment(Long requestAdvansePaymentId) {
        log.info("Got confirmAdvancePayment request " + requestAdvansePaymentId);
        return advancePageService.confirmAdvancePayment(requestAdvansePaymentId);
    }

    @Override
    public ResponseEntity<Void> giveAdvanceForTrip(Long tripId) {
        log.info("Got requestGiveAdvancePayment request tripId - " + tripId);
        return dispatcherPageService.giveAdvanceForTrip(tripId);
    }

    @Override
    public ResponseEntity<Void> carrierWantsAdvance(UUID uuid) {
        log.info("Got carrierWantsAdvance request uuid - " + uuid);
        return carrierPageService.carrierWantsAdvance(uuid);
    }

    @Override
    public ResponseEntity<Void> changeAdvancePaymentComment(AdvanceCommentDTO commentDTO) {
        log.info("Got changeAdvancePaymentComment request " + commentDTO);
        return advancePageService.changeAdvancePaymentComment(commentDTO);
    }

    @Override
    public ResponseEntity downloadAvanceRequestTemplateForCarrier(String tripNum) {
        log.info("Got downloadAvanceRequestTemplateForCarrier request tripNum - " + tripNum);
        return advanceService.downloadAvanceRequestTemplateForCarrier(tripNum);
    }

    @Override
    public ResponseEntity downloadAvanceRequestTemplate(String tripNum) {
        log.info("Got downloadAvanceRequestTemplate request tripNum - " + tripNum);
        return advanceService.downloadAvanceRequestTemplate(tripNum);
    }


    @Override
    public ResponseEntity<Resource> downloadAdvanceRequest(String tripNum) {
        log.info("Got downloadAvanseRequest request tripNum - " + tripNum);
        return advanceService.downloadAdvanceRequest(tripNum);
    }

    @Override
    public ResponseEntity<Resource> downloadRequest(String tripNum) {
        log.info("Got downloadRequest request tripNum - " + tripNum);
        return advanceService.downloadRequest(tripNum);
    }



    @Override
    public ResponseEntity<Void> uploadRequestAdvance(MultipartFile filename, String tripNum) {
        log.info("Got uploadRequestAdvance request tripNum - " + tripNum);
        return advanceService.uploadRequestAdvance(filename, tripNum);
    }

    @Override
    public ResponseEntity<Void> uploadRequestAvanceForCarrier(MultipartFile filename, String tripNum) {
        log.info("Got uploadRequestAvanceForCarrier request tripNum - " + tripNum);
        return advanceService.uploadRequestAdvance(filename, tripNum);
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
