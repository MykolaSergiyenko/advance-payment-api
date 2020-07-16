package online.oboz.trip.trip_carrier_advance_payment_api.service.rest.dispatcher;

import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.CarrierContactDTO;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.IsTripAdvanced;
import org.springframework.http.ResponseEntity;

/**
 * Сервис "Авансирование" для страницы Диспетчера
 */
public interface DispatcherService {
    /**
     * Выдать аванс по трипу
     * @param tripId
     */
    ResponseEntity<Void> giveAdvanceForTrip(Long tripId);

    /**
     * Статус выдачи аванса по трипу
     * @param tripId
     * @return IsTripAdvanced - DTO статуса выдачи
     */
    ResponseEntity<IsTripAdvanced> isAdvanced(Long tripId);

    /**
     * Получить контакт авансирования для контрагента
     * @param contractorId
     * @return CarrierContactDTO - DTO контакта
     */
    ResponseEntity<CarrierContactDTO> getContactCarrier(Long contractorId);

    /**
     * Создать контакт авансирования
     * @param carrierContactDTO
     * @return
     */
    ResponseEntity<Void> addContactCarrier(CarrierContactDTO carrierContactDTO);

    /**
     * Обновить контакт авансирования
     * @param carrierContactDTO
     * @return
     */
    ResponseEntity<Void> updateContactCarrier(CarrierContactDTO carrierContactDTO);
}
