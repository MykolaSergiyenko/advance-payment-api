package online.oboz.trip.trip_carrier_advance_payment_api.service.integration.tripdocs;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.attachments.TripAttachment;

import java.util.List;
import java.util.UUID;

public interface TripDocumentsService {

    List<TripAttachment> getTripAttachments(Long tripId);

    UUID getRequestUuidOrTripRequestUuid(List<TripAttachment> attachments);

    UUID getRequestUuid(List<TripAttachment> attachments);

    UUID getTripRequestUuid(List<TripAttachment> attachments);

    UUID getAssignmentRequestUuid(List<TripAttachment> attachments);

    UUID saveAssignmentAdvanceRequestUuid(Long tripId, UUID fileUuid);

    Boolean isAllDocumentsLoaded(Advance advance);

    Boolean isAllTripDocumentsLoaded(Long tripId);
}
