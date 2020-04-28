package online.oboz.trip.trip_carrier_advance_payment_api.repository;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.TripRequestAdvancePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdvanceRequestRepository extends JpaRepository<TripRequestAdvancePayment, Long> {

    @Query(" select ap from TripRequestAdvancePayment ap " +
        " where  ap.tripId = :trip_id " +
        "   and ap.driverId = :driver_id " +
        "   and ap.contractorId = :contractor_id ")
    TripRequestAdvancePayment find(@Param("trip_id") Long tripId,
                                   @Param("driver_id") Long driverId,
                                   @Param("contractor_id") Long contractorId);

    @Query("select pc " +
        " from TripRequestAdvancePayment pc " +
        " where pc.id = :id ")
    Optional<TripRequestAdvancePayment> find(@Param("id") Long id);

    @Query("select pc " +
        " from TripRequestAdvancePayment pc " +
        " where pc.advanceUuid = :uuid ")
    Optional<TripRequestAdvancePayment> find(@Param("uuid") UUID uuid);

    @Query("select pc " +
        " from TripRequestAdvancePayment pc " +
        " where pc.uuidAdvanceApplicationFile is null " +
        "   or pc.uuidContractApplicationFile is null ")
    List<TripRequestAdvancePayment> findRequestsWithoutFiles();

    @Query(nativeQuery = true, value = "select pc.id, " +
        "       pc.driver_id, " +
        "       pc.trip_id, " +
        "       pc.trip_cost, " +
        "       pc.contractor_id, " +
        "       pc.advance_payment_sum, " +
        "       pc.registration_fee, " +
        "       pc.is_cancelled, " +
        "       pc.comment, " +
        "       pc.is_pushed_unf_button, " +
        "       pc.is_unf_send, " +
        "       pc.payment_contractor_id, " +
        "       pc.page_carrier_url_is_access, " +
        "       pc.trip_type_code, " +
        "       pc.created_at, " +
        "       pc.updated_at, " +
        "       pc.author_id, " +
        "       pc.cancelled_comment, " +
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
        "       pc.advance_uuid " +
        "from trip_request_advance_payment pc " +
        "inner join orders.trips t on pc.trip_id = t.id " +
        "where t.num = :tripNum ")
    TripRequestAdvancePayment find(@Param("tripNum") String tripNum);


    @Query(nativeQuery = true,
        value = "select * from trip_request_advance_payment t where " +
            "t.is_email_read = false and t.is_sms_sent = false " +
            "and now() - interval '1 hour' > t.created_at")
        // or (email_read_at is null?)
    List<TripRequestAdvancePayment> findForNotification();

}
