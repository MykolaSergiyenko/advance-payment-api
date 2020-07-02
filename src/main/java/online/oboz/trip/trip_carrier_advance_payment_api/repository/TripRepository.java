package online.oboz.trip.trip_carrier_advance_payment_api.repository;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip, Long> {
    @Query(" select t from Trip t "+
    " inner join AdvanceContractor ac on (t.contractorId = ac.id "+
    " and ac.isAutoContractor = true ) "+
    " where (t.tripFields.tripStatusCode = 'assigned' and "+
    " t.tripFields.tripTypeCode = 'motor' and t.tripCostInfo.cost > :minCost) ")
    List<Trip> getTripsForAutoAdvance(@Param("minCost") Double minCost);

//    @Query("select t from Trip t "+
//        "inner join TripAdvance adv on adv.tripId = t.id " +
//        "inner join TripAdvanceContractor cont on cont.id = t.contractorId "+
//        "where  cont.isAutoAdvance = true or " +
//        " adv.isCancelled = true and " +
//        "(t.driverId is not null and " +
//        "t.orderId is not null and " +
//        "t.contractorId is not null and " +
//        "t.tripTypeCode = 'motor' and " +
//        "t.tripStatusCode = 'assigned') ")
//    List<Trip> getTripsForAutoAdvance();

//    @Query(nativeQuery = true, value = " select t.id, " +
//        "       t.contractor_id, " +
//        "       t.driver_id, " +
//        "       t.num, " +
//        "       t.order_id, " +
//        "       cast(case " +
//        "                when v.value > 0 " +
//        "                    then t.cost + t.cost * v.value / 100 " +
//        "                else t.cost end as decimal(10, 2)) as cost, " +
//        "       t.payment_contractor_id, " +
//        "       t.vat_code, " +
//        "       t.trip_type_code, " +
//        "       t.trip_status_code, " +
//        "       t.resource_type_code, " +
//        "       t.created_at " +
//        "        from orders.trips t " +
//        "         inner join orders.orders o on t.order_id = o.id" +
//        "         inner join dictionary.order_types ot on o.order_type_id = ot.id " +
//        "         inner join common.contractors cc on (t.contractor_id = cc.id " +
//        "    and cc.is_auto_advance_payment = true) " +
//        "         inner join dictionary.vats v on v.code = t.vat_code " +
//        "         inner join common.contractor_advance_exclusion ce on cc.id = ce.carrier_id " +
//        "         left join orders.trip_request_advance_payment trap on t.id = trap.trip_id " +
//        "where  (trap.trip_id is null or t.driver_id != trap.driver_id) " +
//        "       and ot.id = ce.order_type_id" +
//        "    and t.driver_id notnull " +
//        "    and t.trip_status_code = 'assigned' " +
//        "    and t.trip_type_code = 'motor' ")
//    List<Trip> getTripsForAutoAdvance();

    @Query(nativeQuery = true, value = " select cast(case " +
        "                when v.value > 0 " +
        "                    then ot.cost + ot.cost * v.value / 100 " +
        "                else ot.cost end as decimal(10, 2)) as trip_cost " +
        " from orders.trips ot " +
        "      inner join dictionary.vats v on v.code = ot.vat_code " +
        " and ot.id = :tripId")
    Double getTripCostWithVat(@Param("tripId") Long tripId);

//    @Query(nativeQuery = true, value = " select t.id, " +
//        "       t.contractor_id, " +
//        "       t.driver_id, " +
//        "       t.num, " +
//        "       t.order_id, " +
//        "       cast(case " +
//        "                when v.value > 0 " +
//        "                    then t.cost + t.cost * v.value / 100 " +
//        "                else t.cost end as decimal(10, 2)) as cost, " +
//        "       t.payment_contractor_id, " +
//        "       t.vat_code, " +
//        "       t.trip_type_code, " +
//        "       t.trip_status_code, " +
//        "       t.resource_type_code, " +
//        "       t.created_at " +
//        "        from orders.trips t " +
//        "         inner join orders.orders o on t.order_id = o.id" +
//        "         inner join dictionary.order_types ot on o.order_type_id = ot.id " +
//        "         inner join common.contractors cc on (t.contractor_id = cc.id " +
//        "    and cc.is_auto_advance_payment = true) " +
//        "         inner join dictionary.vats v on v.code = t.vat_code " +
//        "         inner join common.contractor_advance_exclusion ce on cc.id = ce.carrier_id " +
//        "         left join orders.trip_request_advance_payment trap on t.id = trap.trip_id " +
//        "where  (trap.trip_id is null or t.driver_id != trap.driver_id) " +
//        "       and ot.id = ce.order_type_id" +
//        "    and t.driver_id notnull " +
//        "    and t.trip_status_code = 'assigned' " +
//        "    and t.trip_type_code = 'motor' ")
//    List<Trip> getAutoApprovedMotorTrips();

    @Query("select t from Trip t where t.tripFields.tripTypeCode = 'motor' and t.tripFields.tripStatusCode = 'assigned' and t.id = :tripId ")
    Optional<Trip> getMotorTrip(@Param("tripId") Long tripId);

    @Query("select t from Trip t " +
        "where t.tripFields.tripTypeCode in ('removed', 'cancelled', 'confirmed', 'driver_confirmation', 'refused') " +
        "                 and t.id = :tripId ")
    Optional<Trip> getNotApproveTrip(@Param("tripId") Long tripId);

    @Query("select t from Trip t where t.tripFields.num = :num")
    Optional<Trip> getTripByNum(@Param("num") String num);

}
