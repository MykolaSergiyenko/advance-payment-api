package online.oboz.trip.trip_carrier_advance_payment_api.service.rest.desktop;

import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.AdvanceCommentDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

public interface AdvanceManager {
    Logger log = LoggerFactory.getLogger(AdvanceManager.class);

    ResponseEntity<Void> confirmAdvancePayment(Long tripAdvanceId);

    ResponseEntity<Void> cancelAdvancePayment(Long tripId, String cancelComment);

    ResponseEntity<Void> updateLoadingComplete(Long id, Boolean loadingComplete);

    ResponseEntity<Void> changeAdvancePaymentComment(AdvanceCommentDTO comment);
}
