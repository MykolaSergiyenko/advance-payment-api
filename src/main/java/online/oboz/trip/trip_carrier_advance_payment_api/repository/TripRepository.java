package online.oboz.trip.trip_carrier_advance_payment_api.repository;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.Trip;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.TripPointDto;
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
        "       t.contractor_id, " +
        "       t.driver_id, " +
        "       t.num, " +
        "       t.order_id, " +
        "       cast(case " +
        "                when v.value > 0 " +
        "                    then t.cost + t.cost * v.value / 100 " +
        "                else t.cost end as decimal(10, 2)) as cost, " +
        "       t.payment_contractor_id, " +
        "       t.vat_code, " +
        "       t.trip_type_code, " +
        "       t.resource_type_code, " +
        "       t.is_advanced_payment, " +
        "       t.created_at " +
        "        from orders.orders o " +
        "         inner join dictionary.order_types ot on o.order_type_id = ot.id " +
        "         inner join orders.trips t on o.id = t.order_id " +
        "         inner join common.contractors cc on t.contractor_id = cc.id " +
        "    and t.driver_id notnull " +
        "    and cc.is_auto_advance_payment = true " +
        "    and t.trip_status_code = 'assigned' " +
        "         inner join dictionary.vats v on v.code = t.vat_code " +
        "         inner join common.contractor_advance_exclusion ce on cc.id = ce.carrier_id " +
        "         left join orders.trip_request_advance_payment trap on t.id = trap.trip_id " +
        "where  (trap.trip_id is null or t.driver_id != trap.driver_id) " +
        "       and ot.id = ce.order_type_id")
    List<Trip> getAutoApprovedTrips();

    @Query("select t from Trip t where t.tripTypeCode = 'motor' and t.tripStatusCode = 'assigned' and t.id = :tripId ")
    Optional<Trip> getMotorTrip(@Param("tripId") Long tripId);

    @Query("select t from Trip t where t.num = :num")
    Optional<Trip> getTripByNum(@Param("num") String num);

    @Query(nativeQuery = true, value = " select tp.trip_id , position, tpas.additional_service_code " +
        "from orders.trip_points tp " +
        "   inner join orders.trip_point_services tps on tp.id = tps.trip_point_id " +
        "   inner join  orders.order_additional_services tpas on tps.order_additional_service_id = tpas.id " +
        "    and tpas.additional_service_code in ('loading', 'unloading') " +
        "order by tp.trip_id, position")
    List<TripPointDto> getTripPointAddress();

}
