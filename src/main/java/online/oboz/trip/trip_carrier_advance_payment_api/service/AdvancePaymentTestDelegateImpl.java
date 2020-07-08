package online.oboz.trip.trip_carrier_advance_payment_api.service;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import online.oboz.trip.trip_carrier_advance_payment_api.service.advance.BaseAdvanceService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.Notificator;
import online.oboz.trip.trip_carrier_advance_payment_api.service.urleditor.UrlService;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.controller.AdvancePaymentTestApiDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Песочница для тестирования сервисов "Авансирования"
 */
@Service
public class AdvancePaymentTestDelegateImpl implements AdvancePaymentTestApiDelegate {
    private static final Logger log = LoggerFactory.getLogger(AdvancePaymentDelegateImpl.class);

    private final UrlService shortenerService;
    private final Notificator notificationService;
    private final BaseAdvanceService service;

    @Autowired
    public AdvancePaymentTestDelegateImpl(
        UrlService shortenerService,
        Notificator notificationService,
        BaseAdvanceService service
    ) {
        this.shortenerService = shortenerService;
        this.notificationService = notificationService;
        this.service = service;
    }

    @Override
    public ResponseEntity<Void> cutUrl(String stringUrl) {
        shortenerService.editUrl(stringUrl);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @Override
    public ResponseEntity<String> createMessage(Long advanceId) {
        log.info("Make notifications for advance - " + advanceId);
        Advance advance = service.findById(advanceId);
        service.notifyAboutAdvance(advance);
        log.info("Out of notifications.");
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
