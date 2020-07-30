package online.oboz.trip.trip_carrier_advance_payment_api.service.pages.desktop;

import online.oboz.trip.trip_carrier_advance_payment_api.service.AdvanceService;
import online.oboz.trip.trip_carrier_advance_payment_api.util.SecurityUtils;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.AdvanceDesktopDTO;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;


@Service
public class AdvanceDesktopService implements AdvanceManager {
    private static final Logger log = LoggerFactory.getLogger(AdvanceDesktopService.class);
    private final AdvanceService advanceService;


    @Autowired
    public AdvanceDesktopService(
        AdvanceService advanceService
    ) {
        this.advanceService = advanceService;
    }

    public ResponseEntity<AdvanceDesktopDTO> search(String tab, Filter filter) {
        return new ResponseEntity<>(advanceService.getAdvances(tab, filter), HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<Void> sendToUnfAdvance(Long advanceId) {
        return advanceService.sendToUnfAdvance(advanceId);
    }

    public ResponseEntity<Void> cancelAdvance(Long id, String comment) {
        return advanceService.cancelAdvance(id, comment);
    }

    public ResponseEntity<Void> setLoadingComplete(Long advanceId, Boolean loadingComplete) {
        return advanceService.setLoadingComplete(advanceId, loadingComplete);
    }

    public ResponseEntity<Void> changeComment(Long id, String comment) {
        return advanceService.changeComment(id, comment);
    }


    public void checkAccess() {
        if (SecurityUtils.hasNotAccess()) {
            log.info("User hasn't access.");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User permission denied");
        }
    }

}
