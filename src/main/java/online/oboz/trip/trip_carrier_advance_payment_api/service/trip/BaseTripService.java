package online.oboz.trip.trip_carrier_advance_payment_api.service.trip;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.Trip;

import java.util.List;

public interface BaseTripService {

    Trip findTripById(Long tripId);

    List<Trip> getAutoAdvanceTrips();


}
