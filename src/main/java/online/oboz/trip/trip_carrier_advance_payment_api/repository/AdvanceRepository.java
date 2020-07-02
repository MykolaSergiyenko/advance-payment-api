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

//    @Query(" select ap from Advance ap " +
//        " where  ap.tripId = :trip_id " +
//        "   and ap.driverId = :driver_id " +
//        "   and ap.contractorId = :contractor_id ")
//    Advance findByParam(@Param("trip_id") Long tripId,
//                        @Param("driver_id") Long driverId,
//                        @Param("contractor_id") Long contractorId);

    @Query("select pc " +
        " from Advance pc " +
        " where pc.advanceTripFields.tripId = :trip_id ")
    Optional<Advance> findByTripId(@Param("trip_id") Long tripId);

    @Query("select pc " +
        " from Advance pc " +
        " where pc.uuid = :uuid ")
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


    @Query(nativeQuery = true,
        value = "select * from trip_request_advance_payment t where " +
            "t.is_email_read = false and t.is_sms_sent = false " +
            "and now() - interval '1 hour' > t.created_at")
        // or (email_read_at is null?)
    List<Advance> findForNotification();

}
