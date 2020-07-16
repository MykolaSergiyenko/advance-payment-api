package online.oboz.trip.trip_carrier_advance_payment_api.service.contacts;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.dicts.contacts.AdvanceContactsBook;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.CarrierContactDTO;
import org.springframework.http.ResponseEntity;

public interface ContactService {

    ResponseEntity<Void> addContactCarrier(CarrierContactDTO carrierContactDTO);

    ResponseEntity<Void> updateContactCarrier(CarrierContactDTO carrierContactDTO);

    ResponseEntity<CarrierContactDTO> getContactCarrier(Long contractorId);

    AdvanceContactsBook findByContractor(Long contractorId);

    Boolean notExistsByContractor(Long contractorId);
}
