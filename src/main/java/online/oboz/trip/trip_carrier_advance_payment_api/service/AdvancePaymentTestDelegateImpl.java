package online.oboz.trip.trip_carrier_advance_payment_api.service;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.TripAdvance;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.TripAdvanceRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.NewNotificationService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.messages.common.format.urlshorter.UrlService;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.controller.AdvancePaymentTestApiDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class AdvancePaymentTestDelegateImpl implements AdvancePaymentTestApiDelegate {
    private static final Logger log = LoggerFactory.getLogger(AdvancePaymentDelegateImpl.class);

    private final UrlService urlShortenerService;
    private final NewNotificationService notificationService;
    private final TripAdvanceRepository advanceRepository;

    @Autowired
    public AdvancePaymentTestDelegateImpl(UrlService urlShortenerService, NewNotificationService notificationService, TripAdvanceRepository advanceRepository) {
        this.urlShortenerService = urlShortenerService;
        this.notificationService = notificationService;
        this.advanceRepository = advanceRepository;
    }

    @Override
    public ResponseEntity<Void> cutUrl(String stringUrl) {
        // ShortUrlResponse in open-api
        log.info("Input url is - " + stringUrl);
        String result = urlShortenerService.editUrl(stringUrl);
        log.info("Output url is - " + result);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @Override
    public ResponseEntity<String> createMessage(Long advanceId) {
        log.info("Make notifications for advance id = " + advanceId);
        TripAdvance advance = advanceRepository.find(advanceId).orElse(null);
        notificationService.notificate(advance);
        log.info("Output of notifications.");
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
