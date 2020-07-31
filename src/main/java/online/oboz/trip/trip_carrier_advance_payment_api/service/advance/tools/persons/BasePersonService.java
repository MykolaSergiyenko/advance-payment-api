package online.oboz.trip.trip_carrier_advance_payment_api.service.advance.tools.persons;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.Person;

/**
 * Сервис для работы с пользователями в сервисе "Авансирование"
 *
 * @author s‡oodent
 */
public interface BasePersonService {
    /**
     * Получить системного пользователя сервиса "Авансирование"
     *
     * @return Person - пользователь
     */
    Person getAdvanceSystemUser();

    /**
     * Получить пользователя по идентификатору
     *
     * @param id
     * @return Person - пользователь
     */
    Person getPerson(Long id);

    /**
     * Получить ФИО автора
     * в запросе "Статус выдачи аванса"
     *
     * @param
     * @param authorId
     * @return
     */
    String getAuthorFullName(Long authorId);
}
