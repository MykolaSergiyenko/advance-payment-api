package online.oboz.trip.trip_carrier_advance_payment_api.service.rest;

import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;

import online.oboz.trip.trip_carrier_advance_payment_api.service.AdvanceService;
import online.oboz.trip.trip_carrier_advance_payment_api.util.SecurityUtils;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.AdvanceCommentDTO;
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
    private final AdvanceService advanceService;
    private final ApplicationProperties applicationProperties;

    public AdvancePageService(
        AdvanceService advanceService,
        ApplicationProperties applicationProperties) {
        this.advanceService = advanceService;

        this.applicationProperties = applicationProperties;
    }

    @Transactional
    public ResponseEntity<Void> confirmAdvancePayment(Long tripAdvanceId) {
        return advanceService.confirmAdvance(tripAdvanceId);
    }

    public ResponseEntity<Void> cancelAdvancePayment(Long tripId, String cancelComment) {
        return advanceService.cancelAdvancePayment(tripId, cancelComment);
    }

    public ResponseEntity<Void> updateLoadingComplete(Long id, Boolean loadingComplete) {
        return advanceService.setLoadingComplete(id, loadingComplete);
    }

    public ResponseEntity<Void> changeAdvancePaymentComment(AdvanceCommentDTO comment) {
        return advanceService.changeAdvanceComment(comment);
    }


    public void checkAccess() {
        if (SecurityUtils.hasNotAccess(applicationProperties)) {
            log.info("User hasn't access.");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User permission denied");
        }
    }

}
