package online.oboz.trip.trip_carrier_advance_payment_api.service.pages.dispatcher;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.Trip;
import online.oboz.trip.trip_carrier_advance_payment_api.service.AdvanceService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.trip.TripService;
import online.oboz.trip.trip_carrier_advance_payment_api.util.SecurityUtils;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.TripAdvanceState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


@Service
public class TripPageService implements TripAdvanceService {
    private static final Logger log = LoggerFactory.getLogger(TripPageService.class);

    private final AdvanceService advanceService;
    private final TripService tripService;

    @Autowired
    public TripPageService(
        AdvanceService advanceService,
        TripService tripService
    ) {
        this.advanceService = advanceService;
        this.tripService = tripService;
    }

    @Override
    public ResponseEntity<Void> giveAdvanceForTrip(Long tripId) {
        Long authorId = SecurityUtils.getAuthPersonId();
        log.info("Give advance request for Trip: {} and Author: {} ", tripId, authorId);
        Advance advance = advanceService.createAdvanceForTripAndAuthorId(tripId, authorId);
        log.info("Advance for Trip: {} was created: {}", tripId, advance.getUuid());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<TripAdvanceState> getAdvanceState(Long tripId) {
        log.info("Advance-state request for Trip: {}", tripId);
        Trip trip = advanceService.findTrip(tripId);
        Boolean advanceNotExists = advanceService.advancesNotExistsForTrip(trip);
        return new ResponseEntity<>(advanceNotExists ?
            // Можем ли выдать аванс по Трипу?
            tripService.checkTripAdvanceState(trip) :
            // Или "состояние аванса", если он есть
            advanceService.checkAdvanceState(advanceService.findByTripId(tripId)),
            HttpStatus.OK);
    }

}
