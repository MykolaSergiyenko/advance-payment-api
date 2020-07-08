package online.oboz.trip.trip_carrier_advance_payment_api.repository;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdvanceRepository extends JpaRepository<Advance, Long> {

    @Query("select pc " +
        " from Advance pc " +
        " where pc.advanceTripFields.tripId = :trip_id ")
    Optional<Advance> findByTripId(@Param("trip_id") Long tripId);

    @Query("select pc " +
        "from Advance pc " +
        "where pc.advanceUuid = :uuid ")
    Optional<Advance> findByUuid(@Param("uuid") UUID uuid);

    @Query("select pc " +
        " from Advance pc " +
        " where pc.uuidAdvanceApplicationFile is null " +
        "   or pc.uuidContractApplicationFile is null ")
    List<Advance> findRequestsWithoutFiles();

    @Query("select pc " +
        " from Advance pc " +
        " where pc.advanceTripFields.num = :trip_num ")
    Optional<Advance> findByTripNum(@Param("trip_num") String tripNum);


//    @Query(nativeQuery = true,
//        value = "select * from trip_request_advance_payment t where " +
//            "t.read_at is null and t.sms_sent_at = null " +
//            "and t.created_at < (now() - make_interval(minutes => :minutes))")
//    List<Advance> findUnreadAdvances(@Param("minutes") int minutes);


    @Query(nativeQuery = true,
        value = "select * from orders.trip_request_advance_payment t where " +
            "t.read_at is null and t.sms_sent_at is null " +
            "and t.created_at < (now() - (:minutes || ' minutes ') \\:\\:interval )")
    List<Advance> findUnreadAdvances(@Param("minutes") int minutes);


    @Query("select case when count(pc)> 0 then true else false end from Advance pc where " +
        "pc.advanceTripFields.tripId = :trip_id and pc.contractorId = :contractor_id and " +
        "pc.advanceTripFields.orderId = :order_id and pc.advanceTripFields.driverId = :driver_id and " +
        "pc.advanceTripFields.num = :trip_num and pc.isCancelled = false ")
    Boolean existsActualByIds(@Param("trip_id") Long tripId, @Param("contractor_id") Long contractorId,
                              @Param("driver_id") Long driverId, @Param("order_id") Long orderId,
                              @Param("trip_num") String tripNum);

}
