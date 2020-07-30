package online.oboz.trip.trip_carrier_advance_payment_api.service.contacts;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.dicts.contacts.AdvanceContactsBook;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.controller.AdvanceContactsApi;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.CarrierContactDTO;
import org.springframework.http.ResponseEntity;

/**
 * Сервис для работы с контактами авансирования контрагента
 */
public interface ContactService extends AdvanceContactsApi {

    /**
     * Создать контакт авансирования
     * @param carrierContactDTO
     */
    ResponseEntity<Void> addContact(CarrierContactDTO carrierContactDTO);

    /**
     * Обновить контакт авансирования
     * @param carrierContactDTO
     * @return
     */
    ResponseEntity<Void> updateContact(CarrierContactDTO carrierContactDTO);

    /**
     * Получить контакт авансирования для контрагента
     * @param contractorId - контрагент
     * @return CarrierContactDTO - страница контакта
     */
    ResponseEntity<CarrierContactDTO> getContact(Long contractorId);

    /**
     * Получить контакт авансирования для контрагента
     * @param contractorId - контрагент
     * @return AdvanceContactsBook - контакт
     */
    AdvanceContactsBook findByContractor(Long contractorId);

    /**
     * Контакт для контрагента НЕ существует?
     * @param contractorId - контрагент
     * @return true\false
     */
    Boolean notExistsByContractor(Long contractorId);
}
