package online.oboz.trip.trip_carrier_advance_payment_api.repository;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdvanceRepository extends JpaRepository<Advance, Long> {

    Page<Advance> findAll(Pageable pageable);

    @Query("select pc from Advance pc where pc.advanceTripFields.tripId = :trip_id")
    Optional<Advance> findByTripId(@Param("trip_id") Long tripId);

    @Query(nativeQuery = true,
        value = "select a from orders.trip_request_advance_payment a where (uuid = :uuid)")
    Optional<Advance> findByUuid(@Param("uuid") UUID uuid);

    @Query("select pc from Advance pc where pc.uuidAdvanceApplicationFile is null " +
        "or pc.uuidContractApplicationFile is null ")
    List<Advance> findRequestsWithoutFiles();

//    @Query("select pc from Advance pc where pc.advanceTripFields.num = :trip_num")
//    Optional<Advance> findByTripNum(@Param("trip_num") String tripNum);


    @Query(nativeQuery = true,
        value = "select * from orders.trip_request_advance_payment a where (a.comment is null or a.comment = '' or " +
            "a.comment = :auto_comment) and (a.paid_at is null) and (a.cancelled_at is null)")
    Page<Advance> findInWorkAdvances(Pageable pageable, @Param("auto_comment") String autoComment);

    @Query(nativeQuery = true,
        value = "select * from orders.trip_request_advance_payment a where (a.comment is not null and a.comment <> '' and " +
            " a.comment <> :auto_comment)")
    Page<Advance> findProblemAdvances(Pageable pageable, @Param("auto_comment") String autoComment);

    @Query(nativeQuery = true,
        value = "select * from orders.trip_request_advance_payment a where (a.paid_at is not null)")
    Page<Advance> findPaidAdvances(Pageable pageable);

    @Query(nativeQuery = true,
        value = "select * from orders.trip_request_advance_payment a where (a.paid_at is null)")
    Page<Advance> findNotPaidAdvances(Pageable pageable);

    @Query(nativeQuery = true,
        value = "select * from orders.trip_request_advance_payment a where (a.cancelled_at is not null)")
    Page<Advance> findCancelledAdvances(Pageable pageable);


    @Query(nativeQuery = true,
        value = "select * from orders.trip_request_advance_payment a inner join orders.trips t on " +
            "(t.id = a.trip_id and t.order_id = a.order_id and " +
            "t.driver_id = a.driver_id and t.contractor_id = a.contractor_id) " +
            "where t.trip_status_code = 'assigned' and t.trip_type_code ='motor' and a.is_cancelled = false and " +
            "a.read_at is null and a.sms_sent_at is null and a.email_sent_at is not null and " +
            "a.email_sent_at < (now() - (:minutes || ' minutes ') \\:\\:interval )")
    List<Advance> findUnreadAdvances(@Param("minutes") long minutes);


    @Query(nativeQuery = true, value = "select (case when count(pc)> 0 then true else false end) " +
        "from orders.trip_request_advance_payment pc where (pc.trip_id = :trip_id and " +
        "pc.contractor_id = :contractor_id and pc.order_id = :order_id and pc.driver_id = :driver_id and " +
        "pc.trip_num = :trip_num and pc.is_cancelled = false)")
    Boolean existsActualByIds(@Param("trip_id") Long tripId, @Param("contractor_id") Long contractorId,
                              @Param("driver_id") Long driverId, @Param("order_id") Long orderId,
                              @Param("trip_num") String tripNum);

}
