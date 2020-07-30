package online.oboz.trip.trip_carrier_advance_payment_api.service.pages.dispatcher;

import online.oboz.trip.trip_carrier_advance_payment_api.service.contacts.ContactService;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.CarrierContactDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

public class ContractorContactsEditor implements ContactEditor {
    private static final Logger log = LoggerFactory.getLogger(ContractorContactsEditor.class);

    private final ContactService contactService;

    @Autowired
    public ContractorContactsEditor(ContactService contactService) {
        this.contactService = contactService;
    }

    @Override
    public ResponseEntity<CarrierContactDTO> getContact(Long contractorId) {
        log.info("Get advance-contact request for contractor: {} ", contractorId);
        return contactService.getContact(contractorId);
    }

    @Override
    public ResponseEntity<Void> addContact(CarrierContactDTO carrierContactDTO) {
        log.info("Add advance-contact request: {} ", carrierContactDTO);
        return contactService.addContact(carrierContactDTO);
    }

    @Override
    public ResponseEntity<Void> updateContact(CarrierContactDTO carrierContactDTO) {
        log.info("Update advance-contact request: {} ", carrierContactDTO);
        return contactService.updateContact(carrierContactDTO);
    }
}
