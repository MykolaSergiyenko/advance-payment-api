package online.oboz.trip.trip_carrier_advance_payment_api.repository;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.TripInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface TripInfoRepository extends JpaRepository<TripInfo, Long> {

    @Query("select t from TripInfo t where  t.tripId = :tripId")
    Optional<TripInfo> getTripInfo(@Param("tripId") Long tripId);
}
