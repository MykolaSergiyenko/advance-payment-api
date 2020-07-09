package online.oboz.trip.trip_carrier_advance_payment_api.service.trip;

import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.Trip;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.TripRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.util.ErrorUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MainTripService implements TripService {
    private static final Logger log = LoggerFactory.getLogger(MainTripService.class);

    private final TripRepository tripRepository;
    private final ApplicationProperties applicationProperties;

    @Autowired
    public MainTripService(
        TripRepository tripRepository,
        ApplicationProperties applicationProperties
    ) {
        this.tripRepository = tripRepository;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public Trip findTripById(Long tripId) {
        log.info("--- findTripById: " + tripId);
        return tripRepository.findById(tripId).
            orElseThrow(() ->
                getTripsInternalError("Trip not found by id: " + tripId));
    }

    @Override
    public List<Trip> getAutoAdvanceTrips() {
        Double minCost = applicationProperties.getMinTripCost();
        log.info("--- getAutoAdvanceTrips for minCost: " + minCost);
        return tripRepository.getTripsForAutoAdvance(minCost);
    }


    private BusinessLogicException getTripsInternalError(String message) {
        return ErrorUtils.getInternalError("Trip-service internal error: " + message);
    }
}
