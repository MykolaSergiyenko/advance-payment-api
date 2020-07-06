package online.oboz.trip.trip_carrier_advance_payment_api.service.rest.dispatcher;

import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.CarrierContactDTO;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.IsTripAdvanced;
import org.springframework.http.ResponseEntity;

public interface DispatcherService {
    ResponseEntity<Void> giveAdvanceForTrip(Long tripId);

    ResponseEntity<IsTripAdvanced> isAdvanced(Long tripId);

    ResponseEntity<CarrierContactDTO> getContactCarrier(Long contractorId);

    ResponseEntity<Void> addContactCarrier(CarrierContactDTO carrierContactDTO);

    ResponseEntity<Void> updateContactCarrier(CarrierContactDTO carrierContactDTO);
}
