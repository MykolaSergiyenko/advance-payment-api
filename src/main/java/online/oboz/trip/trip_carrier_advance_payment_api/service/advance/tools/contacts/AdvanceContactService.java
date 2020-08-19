package online.oboz.trip.trip_carrier_advance_payment_api.service.advance.tools.contacts;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.dicts.contacts.AdvanceContactsBook;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.contacts.FullNamePersonInfo;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.contractor.AdvanceContractor;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.mappers.AdvanceContactMapper;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.AdvanceContactsBookRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.service.advance.tools.contractors.ContractorService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.util.ErrorUtils;
import online.oboz.trip.trip_carrier_advance_payment_api.service.util.StringUtils;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.CarrierContactDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


/**
 * Сервис для работы с контактами "аванса"
 *
 * @author s‡udent
 */
@Service
public class AdvanceContactService implements ContactService {
    private static final Logger log = LoggerFactory.getLogger(AdvanceContactService.class);

    private final AdvanceContactsBookRepository contactsBookRepository;
    private final ContractorService contractorService;
    private final AdvanceContactMapper contactMapper = AdvanceContactMapper.contactMapper;


    public AdvanceContactService(AdvanceContactsBookRepository contactsBookRepository, ContractorService contractorService) {
        this.contactsBookRepository = contactsBookRepository;
        this.contractorService = contractorService;
    }

    @Override
    public ResponseEntity<Void> addContact(CarrierContactDTO carrierContactDTO) {
        log.info("[Контакты]: создать контакт авансирования - {}.", contactInline(carrierContactDTO));
        try {
            createContact(carrierContactDTO);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            throw getContactError("[Advance-contacts]: add contact error:" + e.getMessage());
        }
    }


    @Override
    public ResponseEntity<Void> updateContact(CarrierContactDTO carrierContactDTO) {
        log.info("[Контакты]: обновить контакт авансирования - {}.", contactInline(carrierContactDTO));
        setContact(carrierContactDTO);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CarrierContactDTO> getContact(Long contractorId) {
        log.info("[Контакты]: просмотреть контакт авансирования - {}.", contractorId);
        CarrierContactDTO contactToDto = contactToDto(findByContractor(contractorId));
        log.info(contactInline(contactToDto));
        return new ResponseEntity<>(contactToDto, HttpStatus.OK);
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

        setContactContractor(contact.getContractorId(), contactDTO.getIsAuto());
        return contact;
    }

    private void setContactContractor(Long contractorId, Boolean isAuto) {
        AdvanceContractor contractor = contractorService.findContractor(contractorId);
        contractorService.setAuto(contractor, isAuto);
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

    private String contactInline(CarrierContactDTO carrierContactDTO) {
        return StringUtils.inlineContact(carrierContactDTO);
    }

    private BusinessLogicException getContactError(String message) {
        return ErrorUtils.getInternalError("Contact-service internal error: " + message);
    }
}
