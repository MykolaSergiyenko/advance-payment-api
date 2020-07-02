package online.oboz.trip.trip_carrier_advance_payment_api.repository;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.info.CommonLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CommonLocationRepository  { //extends JpaRepository<CommonLocation, Long>
//    @Query("select l from CommonLocation l where  l.id = :locationId")
//    Optional<CommonLocation> find(@Param("locationId") String locationId);
}
