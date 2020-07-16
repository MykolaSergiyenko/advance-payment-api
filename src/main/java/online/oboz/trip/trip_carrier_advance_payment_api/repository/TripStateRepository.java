package online.oboz.trip.trip_carrier_advance_payment_api.repository;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.info.TripStateHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripStateRepository extends JpaRepository<TripStateHistory, Long> {
}
