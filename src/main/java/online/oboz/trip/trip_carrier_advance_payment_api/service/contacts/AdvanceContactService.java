package online.oboz.trip.trip_carrier_advance_payment_api.service.contacts;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.dicts.contacts.AdvanceContactsBook;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.mappers.AdvanceContactMapper;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.AdvanceContactsBookRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.service.AdvanceService;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.CarrierContactDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


@Service
public class AdvanceContactService implements ContactService {
    private static final Logger log = LoggerFactory.getLogger(AdvanceContactService.class);

    private final AdvanceService advanceService;
    private final AdvanceContactsBookRepository contactsBookRepository;
    private final AdvanceContactMapper contactMapper = AdvanceContactMapper.contactMapper;


    public AdvanceContactService(AdvanceService advanceService, AdvanceContactsBookRepository contactsBookRepository) {
        this.advanceService = advanceService;
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
        return new ResponseEntity<>(contactToDto(
            getByContractorId(contractorId)), HttpStatus.OK);
    }


    // *** internal-functions


    private AdvanceContactsBook createContact(CarrierContactDTO contactDTO) {
        Long contractorId = contactDTO.getContractorId();
        AdvanceContactsBook contact = getByContractorId(contractorId);
        if (contact != null) {
            log.info("Advance contact for this Carrier is already exists: " + contractorId);
        }
        return setContactInfo(contactDTO, contact);
    }

    private AdvanceContactsBook updateContact(CarrierContactDTO contactDTO) {
        AdvanceContactsBook contact = getByContractorId(contactDTO.getContractorId());
        return setContactInfo(contactDTO, contact);
    }

    private AdvanceContactsBook getByContractorId(Long contractorId) {
        return contactsBookRepository.findByContractorId(contractorId).orElseThrow(() ->
            getContactError("Advance contact for this Carrier is not exists:" + contractorId));
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

    private BusinessLogicException getContactError(String s) {
        return advanceService.getEntityServiceError(s, AdvanceContactService.class);
    }
}
