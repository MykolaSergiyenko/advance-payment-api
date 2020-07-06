package online.oboz.trip.trip_carrier_advance_payment_api.service.trip;

import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.Trip;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.TripRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.Error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Service
public class TripService implements BaseTripService {
    private static final Logger log = LoggerFactory.getLogger(TripService.class);

    private final TripRepository tripRepository;
    private final ApplicationProperties applicationProperties;

    public TripService(TripRepository tripRepository, ApplicationProperties applicationProperties) {
        this.tripRepository = tripRepository;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public Trip findTripById(Long tripId) {
        log.info("--- findTripById: "+tripId);
        return tripRepository.findById(tripId).
            orElseThrow(() ->
                getTripsInternalError("Trip not found by id: " + tripId));
    }

    @Override
    public List<Trip> getAutoAdvanceTrips() {
        Double minCost = applicationProperties.getMinTripCost();
        log.info("--- getAutoAdvanceTrips for minCost: "+minCost);
        return tripRepository.getTripsForAutoAdvance(minCost);
    }


    private BusinessLogicException getTripsInternalError(String message) {
        log.error(message);
        return getInternalBusinessError(getServiceError(message), INTERNAL_SERVER_ERROR);
    }

    private Error getServiceError(String errorMessage) {
        Error error = new Error();
        error.setErrorMessage("TripsService - Business Error: " + errorMessage);
        return error;
    }

    private BusinessLogicException getInternalBusinessError(Error error, HttpStatus state) {
        log.error(state.name() + " : " + error.getErrorMessage());
        return new BusinessLogicException(state, error);
    }


}
