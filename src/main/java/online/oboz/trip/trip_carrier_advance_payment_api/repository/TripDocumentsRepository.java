package online.oboz.trip.trip_carrier_advance_payment_api.repository;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.attachments.TripAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TripDocumentsRepository  extends JpaRepository<TripAttachment, Long> {

    @Query("select at from TripAttachment at where at.tripId = :trip_id")
    List<TripAttachment> findByTripId(@Param("trip_id") Long tripId);

    @Query("select at from TripAttachment at " +
        "inner join Advance ad on at.tripId = ad.advanceTripFields.tripId "+
        "where ad.id = :advance_id")
    List<TripAttachment> findByAdvanceId(@Param("advance_id") Long advanceId);
}
