package online.oboz.trip.trip_carrier_advance_payment_api.repository;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.TripRequestAdvancePayment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TripRequestAdvancePaymentRepository extends JpaRepository<TripRequestAdvancePayment, Long> {
    @Query(nativeQuery = true, value = " select ap.id, " +
        "       ap.trip_id, " +
        "       ap.trip_type, " +
        "       ap.driver_id, " +
        "       ap.contractor_id, " +
        "       ap.payment_contractor_id, " +
        "       ap.trip_cost, " +
        "       ap.vat, " +
        "       ap.advance_payment_sum, " +
        "       ap.registration_fee, " +
        "       ap.cancel_advance, " +
        "       ap.comment, " +
        "       ap.is_unf_send, " +
        "       ap.rejected_request, " +
        "       ap.created_at " +
        " from orders.trip_request_advance_payment ap " +
        "     join common.contractors_contractor_types cct on cct.contractor_id = ap.contractorId " +
        "          and cct.contractor_type_id = 2 " +
        " where c.is_verified = true " +
        "     and c.contractor_status_code != 'deleted' " +
        "     and (c.full_name ilike '%' || :search || '%' " +
        "           or inn ilike '%' || :search || '%') ")
    List<TripRequestAdvancePayment> findTripRequestAdvancePayment(@Param("search") String search, Pageable pageable);

    @Query(nativeQuery = true, value = " select count(ap.id) " +
        " from orders.trip_request_advance_payment ap " +
        " where  trip_id = :trip_id " +
        "   and driver_id = :driver_id "+
        "   and contractor_id = :contractor_id ")
    TripRequestAdvancePayment findRequestAdvancePayment(@Param("trip_id") Long trip_id,
                                                        @Param("driver_id") Long driverId,
                                                        @Param("contractor_id") Long contractorId);

}
