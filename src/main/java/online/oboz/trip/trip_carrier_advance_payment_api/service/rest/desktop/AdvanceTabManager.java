package online.oboz.trip.trip_carrier_advance_payment_api.service.rest.desktop;

import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.AdvanceDesktopDTO;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

public interface AdvanceTabManager {
    Logger log = LoggerFactory.getLogger(AdvanceTabManager.class);

    ResponseEntity<AdvanceDesktopDTO> searchInWorkRequests(Filter filter);

    ResponseEntity<AdvanceDesktopDTO> searchProblemRequests(Filter filter);

    ResponseEntity<AdvanceDesktopDTO> searchPaidRequests(Filter filter);

    ResponseEntity<AdvanceDesktopDTO> searchNotPaidRequests(Filter filter);

    ResponseEntity<AdvanceDesktopDTO> searchCanceledRequests(Filter filter);
}
