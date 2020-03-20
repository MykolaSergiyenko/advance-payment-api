package online.oboz.trip.trip_carrier_advance_payment_api.repository;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.TripRequestAdvancePayment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TripRequestAdvancePaymentRepository extends JpaRepository<TripRequestAdvancePayment, Long> {
    @Query(" select ap from TripRequestAdvancePayment ap ")
    List<TripRequestAdvancePayment> findTripRequestAdvancePayment(Pageable pageable);

    @Query(" select ap from TripRequestAdvancePayment ap " +
        " where  ap.tripId = :trip_id " +
        "   and ap.driverId = :driver_id " +
        "   and ap.contractorId = :contractor_id ")
    TripRequestAdvancePayment findRequestAdvancePayment(@Param("trip_id") Long tripId,
                                                        @Param("driver_id") Long driverId,
                                                        @Param("contractor_id") Long contractorId);

    @Query("select pc " +
        " from TripRequestAdvancePayment pc " +
        " where pc.id = :id ")
    Optional<TripRequestAdvancePayment> findTripRequestAdvancePayment(@Param("id") Long id);

    @Query("select pc " +
        " from TripRequestAdvancePayment pc " +
        " where pc.uuidRequest = :uuid ")
    Optional<TripRequestAdvancePayment> findTripRequestAdvancePayment(@Param("uuid") UUID uuid);

    @Query("select pc " +
        " from TripRequestAdvancePayment pc " +
        " where pc.uuidAdvanceApplicationFile is null " +
        "   or pc.uuidContractApplicationFile is null ")
    List<TripRequestAdvancePayment> findRequestAdvancePaymentWithOutUuidFiles();

    @Query(nativeQuery = true, value = "select * " +
        "from trip_request_advance_payment pc " +
        "inner join orders.trips t on pc.trip_id = t.id " +
        "where t.num = :tripNum ")
    TripRequestAdvancePayment findRequestAdvancePaymentByTripNum(@Param("tripNum") String tripNum);

}
