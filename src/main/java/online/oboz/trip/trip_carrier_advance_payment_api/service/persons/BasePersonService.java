package online.oboz.trip.trip_carrier_advance_payment_api.service.persons;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.Person;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.IsTripAdvanced;

/**
 * Сервис для работы с пользователями в сервисе "Авансирование"
 */
public interface BasePersonService {
    /**
     * Получить системного пользователя сервиса "Авансирование"
     * @return Person - пользователь
     */
    Person getAdvanceSystemUser();

    /**
     * Получить пользователя по идентификатору
     * @param id
     * @return Person - пользователь
     */
    Person getPerson(Long id);

    /**
     * Заполнить персональную информацию автора аванса
     * в запросе "Статус выдачи аванса"
     * @param page
     * @param authorId
     * @return
     */
    IsTripAdvanced setAuthorInfo(IsTripAdvanced page, Long authorId);
}
