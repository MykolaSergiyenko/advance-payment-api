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
        " where pc.advanceUuid = :uuid ")
    Optional<TripRequestAdvancePayment> findTripRequestAdvancePayment(@Param("uuid") UUID uuid);

    @Query("select pc " +
        " from TripRequestAdvancePayment pc " +
        " where pc.uuidAdvanceApplicationFile is null " +
        "   or pc.uuidContractApplicationFile is null ")
    List<TripRequestAdvancePayment> findRequestAdvancePaymentWithOutUuidFiles();

    @Query(nativeQuery = true, value = "select pc.id, " +
        "       pc.driver_id, " +
        "       pc.trip_id, " +
        "       pc.trip_cost, " +
        "       pc.contractor_id, " +
        "       pc.advance_payment_sum, " +
        "       pc.registration_fee, " +
        "       pc.cancel_advance, " +
        "       pc.comment, " +
        "       pc.is_unf_send, " +
        "       pc.payment_contractor_id, " +
        "       pc.page_carrier_url_is_access, " +
        "       pc.trip_type_code, " +
        "       pc.created_at, " +
        "       pc.updated_at, " +
        "       pc.author_id, " +
        "       pc.cancel_advance_comment, " +
        "       pc.is_automation_request, " +
        "       pc.is_paid, " +
        "       pc.paid_at, " +
        "       pc.loading_complete, " +
        "       pc.is_1c_send_allowed, " +
        "       pc.is_downloaded_advance_application, " +
        "       pc.is_downloaded_contract_application, " +
        "       pc.uuid_advance_application_file, " +
        "       pc.uuid_contract_application_file, " +
        "       pc.push_button_at, " +
        "       pc.advance_uuid, " +
        "       pc.is_advanced_payment " +
        "from trip_request_advance_payment pc " +
        "inner join orders.trips t on pc.trip_id = t.id " +
        "where t.num = :tripNum ")
    TripRequestAdvancePayment findRequestAdvancePaymentByTripNum(@Param("tripNum") String tripNum);

    @Query(nativeQuery = true, value = "select trap.id, " +
        "       trap.driver_id, " +
        "       trap.trip_id, " +
        "       trap.trip_cost, " +
        "       trap.contractor_id, " +
        "       trap.advance_payment_sum, " +
        "       trap.registration_fee, " +
        "       trap.cancel_advance, " +
        "       trap.comment, " +
        "       trap.is_unf_send, " +
        "       trap.payment_contractor_id, " +
        "       trap.page_carrier_url_is_access, " +
        "       trap.trip_type_code, " +
        "       trap.created_at, " +
        "       trap.updated_at, " +
        "       trap.author_id, " +
        "       trap.cancel_advance_comment, " +
        "       trap.is_automation_request, " +
        "       trap.is_paid, " +
        "       trap.paid_at, " +
        "       trap.loading_complete, " +
        "       trap.is_1c_send_allowed, " +
        "       trap.is_downloaded_advance_application, " +
        "       trap.is_downloaded_contract_application, " +
        "       trap.uuid_advance_application_file, " +
        "       trap.uuid_contract_application_file, " +
        "       trap.push_button_at, " +
        "       trap.advance_uuid, " +
        "       trap.is_advanced_payment " +
        "from orders.trip_request_advance_payment trap " +
        "         inner join orders.trips t on trap.trip_id = t.id and trap.cancel_advance != true " +
        "where t.trip_status_code in ('removed', 'cancelled', 'confirmed', 'driver_confirmation', 'refused')")
    List<TripRequestAdvancePayment> findRequestAdvancePaymentNeedCancel();

}
