package online.oboz.trip.trip_carrier_advance_payment_api.repository;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.info.CommonLocation;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CommonLocationRepository extends JpaRepository<CommonLocation, Long> {

}
