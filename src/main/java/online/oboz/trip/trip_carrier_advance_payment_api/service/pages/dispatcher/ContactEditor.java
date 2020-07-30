package online.oboz.trip.trip_carrier_advance_payment_api.service.pages.dispatcher;

import online.oboz.trip.trip_carrier_advance_payment_api.web.api.controller.AdvanceContactsApiDelegate;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.CarrierContactDTO;
import org.springframework.http.ResponseEntity;

/**
 * Сервис "Авансирование" - карточка контрагента для редактирования контактов
 */
public interface ContactEditor extends AdvanceContactsApiDelegate {

    /**
     * Получить контакт авансирования для контрагента
     *
     * @param contractorId
     * @return CarrierContactDTO - DTO контакта
     */
    @Override
    ResponseEntity<CarrierContactDTO> getContact(Long contractorId);

    /**
     * Создать контакт авансирования
     *
     * @param carrierContactDTO
     * @return
     */
    @Override
    ResponseEntity<Void> addContact(CarrierContactDTO carrierContactDTO);

    /**
     * Обновить контакт авансирования
     *
     * @param carrierContactDTO
     * @return
     */
    @Override
    ResponseEntity<Void> updateContact(CarrierContactDTO carrierContactDTO);
}
