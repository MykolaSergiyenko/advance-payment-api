package online.oboz.trip.trip_carrier_advance_payment_api.repository;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TripRepository extends JpaRepository<Trip, Long> {
    @Query(" select t from Trip t" +
        " inner join AdvanceContractor ac on (t.contractorId = ac.id" +
        " and ac.isAutoContractor = true )" +
        " where (t.tripStatusCode = 'assigned' and t.tripFields.tripTypeCode = 'motor'" +
        " and t.tripCostInfo.cost > :minCost) and not exists " +
        " (select a from Advance a where t.id = a.advanceTripFields.tripId and" +
        " t.contractorId = a.contractorId and" +
        " t.tripFields.driverId = a.advanceTripFields.driverId and" +
        " t.tripFields.orderId = a.advanceTripFields.orderId and " +
        " t.tripFields.num = a.advanceTripFields.num)")
    List<Trip> getTripsForAutoAdvance(@Param("minCost") Double minCost);

}
