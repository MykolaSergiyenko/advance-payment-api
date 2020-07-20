package online.oboz.trip.trip_carrier_advance_payment_api.service.trip;

import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.Trip;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.TripRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.service.costs.advancedict.CostDictService;
import online.oboz.trip.trip_carrier_advance_payment_api.util.ErrorUtils;

import online.oboz.trip.trip_carrier_advance_payment_api.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class MainTripService implements TripService {
    private static final Logger log = LoggerFactory.getLogger(MainTripService.class);

    private final TripRepository tripRepository;
    private final CostDictService costDictService;
    private final ApplicationProperties applicationProperties;

    @Autowired
    public MainTripService(
        TripRepository tripRepository,
        CostDictService costDictService,
        ApplicationProperties applicationProperties
    ) {
        this.tripRepository = tripRepository;
        this.costDictService = costDictService;
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
        Double minCost = costDictService.findMinCost();
        Double maxCost = costDictService.findMaxCost();
        Long tripInterval = applicationProperties.getNewTripsInterval();
        OffsetDateTime minDate = OffsetDateTime.now().minusMinutes(tripInterval);
        log.info("--- getAutoAdvanceTrips for minCost = {}, maxCost = {} and minDate = {}",
            format(minCost), format(maxCost), minDate);
        return tripRepository.getTripsForAutoAdvance(minCost, maxCost, minDate);
    }

    private String format(Double d){
        return StringUtils.formatNum(d);
    }


    private BusinessLogicException getTripsInternalError(String message) {
        return ErrorUtils.getInternalError("Trip-service internal error: " + message);
    }
}
