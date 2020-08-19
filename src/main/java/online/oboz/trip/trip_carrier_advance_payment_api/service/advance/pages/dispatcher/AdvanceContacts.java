package online.oboz.trip.trip_carrier_advance_payment_api.service.advance.pages.dispatcher;

import online.oboz.trip.trip_carrier_advance_payment_api.service.advance.tools.contacts.ContactService;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.CarrierContactDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

/**
 * Контакты авансирования
 *
 * @author s‡udent
 */
public class AdvanceContacts implements ContractorsContacts {
    private static final Logger log = LoggerFactory.getLogger(AdvanceContacts.class);

    private final ContactService contactService;

    @Autowired
    public AdvanceContacts(ContactService contactService) {
        this.contactService = contactService;
    }

    @Override
    public ResponseEntity<CarrierContactDTO> getContact(Long contractorId) {
        return contactService.getContact(contractorId);
    }

    @Override
    public ResponseEntity<Void> addContact(CarrierContactDTO carrierContactDTO) {
        return contactService.addContact(carrierContactDTO);
    }

    @Override
    public ResponseEntity<Void> updateContact(CarrierContactDTO carrierContactDTO) {
        return contactService.updateContact(carrierContactDTO);
    }
}
