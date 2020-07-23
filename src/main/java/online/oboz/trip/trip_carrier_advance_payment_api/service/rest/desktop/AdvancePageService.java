package online.oboz.trip.trip_carrier_advance_payment_api.service.rest.desktop;

import online.oboz.trip.trip_carrier_advance_payment_api.service.advance.AdvanceService;
import online.oboz.trip.trip_carrier_advance_payment_api.util.SecurityUtils;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.AdvanceCommentDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;


@Service
public class AdvancePageService implements AdvanceManager {
    private static final Logger log = LoggerFactory.getLogger(AdvancePageService.class);
    private final AdvanceService advanceService;

    @Autowired
    public AdvancePageService(
        AdvanceService advanceService
    ) {
        this.advanceService = advanceService;
    }

    @Transactional
    public ResponseEntity<Void> confirmAdvancePayment(Long advanceId) {
        return advanceService.confirmAdvance(advanceId);
    }

    public ResponseEntity<Void> cancelAdvancePayment(Long tripId, String cancelComment) {
        return advanceService.cancelAdvancePayment(tripId, cancelComment);
    }

    public ResponseEntity<Void> updateLoadingComplete(Long advanceId, Boolean loadingComplete) {
        return advanceService.setLoadingComplete(advanceId, loadingComplete);
    }

    public ResponseEntity<Void> changeAdvancePaymentComment(AdvanceCommentDTO comment) {
        return advanceService.changeAdvanceComment(comment);
    }


    public void checkAccess() {
        if (SecurityUtils.hasNotAccess()) {
            log.info("User hasn't access.");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User permission denied");
        }
    }

}
