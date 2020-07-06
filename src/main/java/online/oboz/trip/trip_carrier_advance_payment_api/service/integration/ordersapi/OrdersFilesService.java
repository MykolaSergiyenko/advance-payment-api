package online.oboz.trip.trip_carrier_advance_payment_api.service.integration.ordersapi;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.response.dto.TripDocuments;

import java.util.Map;

public interface OrdersFilesService {

    Boolean isDownloadAllDocuments(Advance advance);

    TripDocuments findAllDocuments(Advance advance);

    Boolean saveTripDocuments(Long orderId, Long tripId, String fileUuid);

    Map<String, String> findTripRequestDocs(Advance advance);

    Map<String, String> findAdvanceRequestDocs(Advance advance);
}
