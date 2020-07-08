package online.oboz.trip.trip_carrier_advance_payment_api.service.contacts;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.dicts.contacts.AdvanceContactsBook;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.mappers.AdvanceContactMapper;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.AdvanceContactsBookRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.util.ErrorUtils;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.CarrierContactDTO;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.Error;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;


@Service
public class AdvanceContactService implements ContactService {
    private static final Logger log = LoggerFactory.getLogger(AdvanceContactService.class);


    private final AdvanceContactsBookRepository contactsBookRepository;
    private final AdvanceContactMapper contactMapper = AdvanceContactMapper.contactMapper;


    public AdvanceContactService(AdvanceContactsBookRepository contactsBookRepository) {
        this.contactsBookRepository = contactsBookRepository;
    }

    public ResponseEntity<Void> addContactCarrier(CarrierContactDTO carrierContactDTO) {
        createContact(carrierContactDTO);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public ResponseEntity<Void> updateContactCarrier(CarrierContactDTO carrierContactDTO) {
        updateContact(carrierContactDTO);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public ResponseEntity<CarrierContactDTO> getContactCarrier(Long contractorId) {
        return new ResponseEntity<>(contactToDto(findByContractor(contractorId)), HttpStatus.OK);
    }


    public AdvanceContactsBook findByContractor(Long contractorId) {
        return contactsBookRepository.findByContractorId(contractorId).orElseThrow(() ->
            getContactError("Advance-contact for this Carrier not found:" + contractorId));
    }


    // *** internal-functions


    private AdvanceContactsBook createContact(CarrierContactDTO contactDTO) {
        Long contractorId = contactDTO.getContractorId();
        AdvanceContactsBook contact = findByContractor(contractorId);
        if (contact != null) {
            log.info("Advance contact for this Carrier is already exists: " + contractorId);
        }
        return setContactInfo(contactDTO, contact);
    }

    private AdvanceContactsBook updateContact(CarrierContactDTO contactDTO) {
        AdvanceContactsBook contact = findByContractor(contactDTO.getContractorId());
        return setContactInfo(contactDTO, contact);
    }


    private AdvanceContactsBook setContactInfo(CarrierContactDTO contactDTO, AdvanceContactsBook contact) {
        contact = dtoToContact(contactDTO);
        contactsBookRepository.save(contact);
        return contact;
    }

    private AdvanceContactsBook dtoToContact(CarrierContactDTO contactDTO) {
        return contactMapper.toContactBook(contactDTO);
    }

    private CarrierContactDTO contactToDto(AdvanceContactsBook contact) {
        return contactMapper.toContactDTO(contact);
    }

    private BusinessLogicException getContactError(String message) {
        return ErrorUtils.getInternalError("Contact-service internal error: " + message);
    }
}
