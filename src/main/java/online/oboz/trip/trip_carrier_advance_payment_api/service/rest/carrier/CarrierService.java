package online.oboz.trip.trip_carrier_advance_payment_api.service.rest.carrier;

import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.CarrierPage;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

public interface CarrierService {
    ResponseEntity<CarrierPage> searchAdvancePaymentRequestByUuid(UUID uuid);

    ResponseEntity<Void> carrierWantsAdvance(UUID uuid);
}
