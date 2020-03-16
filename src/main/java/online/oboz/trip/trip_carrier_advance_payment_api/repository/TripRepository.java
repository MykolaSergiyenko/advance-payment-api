package online.oboz.trip.trip_carrier_advance_payment_api.repository;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface TripRepository extends JpaRepository<Trip, Long> {

    @Query(nativeQuery = true, value = " select cast(case " +
        "                when v.value > 0 " +
        "                    then ot.cost + ot.cost * v.value / 100 " +
        "                else ot.cost end as decimal(10, 2)) as trip_cost " +
        " from orders.trips ot " +
        "      inner join dictionary.vats v on v.code = ot.vat_code " +
        " and ot.id = :tripId")
    Double getTripCostWithVat(@Param("tripId") Long tripId);

    @Query(nativeQuery = true, value = " select t.id, " +
        "   t.contractor_id, " +
        "   t.driver_id, " +
        "   t.num, " +
        "   t.order_id, " +
        "   cast(case " +
        "        when v.value > 0 " +
        "            then t.cost + t.cost * v.value / 100 " +
        "        else t.cost end as decimal(10, 2)) as cost, " +
        "   t.payment_contractor_id, " +
        "   t.vat_code, " +
        "   t.trip_type_code, " +
        "   t.resource_type_code, " +
        "   t.is_advanced_payment, " +
        "   t.created_at " +
        "    from orders.trips t " +
//        TODO:add tripsatus filter
        "         inner join common.contractors cc on t.contractor_id = cc.id " +
        "         inner join dictionary.vats v on v.code = t.vat_code " +
        "         left join common.contractor_advance_exclusion ce on cc.id = ce.carrier_id " +
        "         left join orders.trip_request_advance_payment trap on t.id = trap.trip_id " +
        "where cc.is_auto_advance_payment = true " +
        "  and (((t.driver_id notnull and trap.trip_id is null) or t.driver_id != trap.driver_id) " +
        "    and (ce.carrier_id is null or ce.is_confirm_advance != false))")
    List<Trip> getAutoApprovedTrips();


    @Query("select t from Trip t where t.tripTypeCode = 'motor' and t.tripStatusCode = 'assigned' and t.id = :tripId ")
    Optional<Trip> getMotorTrip(@Param("tripId") Long tripId);
}
