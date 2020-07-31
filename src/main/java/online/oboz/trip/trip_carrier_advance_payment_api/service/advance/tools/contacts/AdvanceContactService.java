package online.oboz.trip.trip_carrier_advance_payment_api.service.advance.tools.contacts;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.dicts.contacts.AdvanceContactsBook;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.contacts.FullNamePersonInfo;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.mappers.AdvanceContactMapper;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.AdvanceContactsBookRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.service.util.ErrorUtils;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.CarrierContactDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


@Service
public class AdvanceContactService implements ContactService {
    private static final Logger log = LoggerFactory.getLogger(AdvanceContactService.class);

    private final AdvanceContactsBookRepository contactsBookRepository;
    private final AdvanceContactMapper contactMapper = AdvanceContactMapper.contactMapper;


    public AdvanceContactService(AdvanceContactsBookRepository contactsBookRepository) {
        this.contactsBookRepository = contactsBookRepository;
    }

    @Override
    public ResponseEntity<Void> addContact(CarrierContactDTO carrierContactDTO) {
        createContact(carrierContactDTO);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @Override
    public ResponseEntity<Void> updateContact(CarrierContactDTO carrierContactDTO) {
        setContact(carrierContactDTO);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CarrierContactDTO> getContact(Long contractorId) {
        return new ResponseEntity<>(contactToDto(findByContractor(contractorId)), HttpStatus.OK);
    }

    @Override
    public AdvanceContactsBook findByContractor(Long contractorId) {
        return contactsBookRepository.findByContractorId(contractorId).orElseThrow(() ->
            getContactError("Advance-contact for this Carrier not found:" + contractorId));
    }

    @Override
    public String getPhone(Long contractorId) {
        return findByContractor(contractorId).getInfo().getPhone();
    }


    @Override
    public String getEmail(Long contractorId) {
        return findByContractor(contractorId).getInfo().getEmail();
    }

    @Override
    public Boolean notExistsByContractor(Long contractorId) {
        AdvanceContactsBook contact = contactsBookRepository.findByContractorId(contractorId).orElse(null);
        return (contact == null);
    }


    // *** internal-functions


    private AdvanceContactsBook createContact(CarrierContactDTO contactDTO) {
        return setContactInfo(contactDTO, true);
    }

    private AdvanceContactsBook setContact(CarrierContactDTO contactDTO) {
        return setContactInfo(contactDTO, false);
    }


    private AdvanceContactsBook setContactInfo(CarrierContactDTO contactDTO, Boolean newContact) {
        AdvanceContactsBook contact;
        if (newContact) {
            contact = dtoToContact(contactDTO);
        } else {
            contact = findByContractor(contactDTO.getContractorId());
            contact.setInfo(dtoToPersonInfo(contactDTO));
        }
        contactsBookRepository.save(contact);
        return contact;
    }

    private AdvanceContactsBook dtoToContact(CarrierContactDTO contactDTO) {
        return contactMapper.toContactBook(contactDTO);
    }

    private FullNamePersonInfo dtoToPersonInfo(CarrierContactDTO contactDTO) {
        return contactMapper.toPersonInfo(contactDTO);
    }

    private CarrierContactDTO contactToDto(AdvanceContactsBook contact) {
        return contactMapper.toContactDTO(contact);
    }

    private BusinessLogicException getContactError(String message) {
        return ErrorUtils.getInternalError("Contact-service internal error: " + message);
    }
}