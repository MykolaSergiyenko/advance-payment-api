package online.oboz.trip.trip_carrier_advance_payment_api.service.advance;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.Trip;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.AdvanceDesktopDTO;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.TripAdvanceState;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.Filter;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

/**
 * Основной сервис "Авансирования"
 */
public interface AdvanceService {

    /**
     * Получить Поездку
     *
     * @param tripId - id
     * @return Trip - Поездка
     */
    Trip findTrip(Long tripId);

    /**
     * Раздать авто-авансы
     */
    void giveAutoAdvances();

    /**
     * Создать аванс для поездки
     *
     * @param tripId   - поездка
     * @param authorId - автор
     * @return Advance - аванс
     */
    Advance createAdvanceForTripAndAuthorId(Long tripId, Long authorId);

    /**
     * Аванс по трипу еще не создан?
     *
     * @param trip - поездка
     * @return true\false
     */
    Boolean advancesNotExistsForTrip(Trip trip);

    /**
     * Получить полный список авансов
     *
     * @return List<Advance> - список авансов
     */
    List<Advance> getAllAdvances();


    /**
     * Получить полный список авансов для вкладки и фильтра
     *
     * @return List<Advance> - список авансов
     */
    AdvanceDesktopDTO getAdvances(String tab, Filter filter);


    /**
     * Получить аванс по id
     *
     * @param id - id
     * @return Advance - аванс
     */
    Advance findById(Long id);

    /**
     * Получить аванс по uuid
     *
     * @param uuid - uuid
     * @return Advance - аванс
     */
    Advance findByUuid(UUID uuid);

//    /**
//     * Получить аванс по номеру поездки
//     *
//     * @param tripNum - номер поездки
//     * @return Advance - аванс
//     */
//    Advance findByTripNum(String tripNum);

    /**
     * Получить аванс по id поездки
     *
     * @param tripId - id поездки
     * @return Advance - аванс
     */
    Advance findByTripId(Long tripId);

    /**
     * Утвердить аванс (отправить в УНФ)
     *
     * @param advanceId - аванс
     */
    ResponseEntity<Void> sendToUnfAdvance(Long advanceId);

    /**
     * Отменить аванс
     *
     * @param advanceId   - аванс
     * @param withComment - комментарий
     */
    ResponseEntity<Void> cancelAdvance(Long advanceId, String withComment);

    /**
     * Изменить комментарий в авансе
     *
     * @param advanceId - аванс
     * @param comment   - комментарий
     */
    ResponseEntity<Void> changeComment(Long advanceId, String comment);

    /**
     * Обновить факт загрузки водителя
     *
     * @param advanceId       - аванс
     * @param loadingComplete - значение признака
     */
    ResponseEntity<Void> setLoadingComplete(Long advanceId, Boolean loadingComplete);

    /**
     * Обновить факт нажатия кнопки "Хочу аванс"
     *
     * @param advanceUuid - аванс
     */
    ResponseEntity<Void> setWantsAdvance(UUID advanceUuid);

    /**
     * Установка uuid вложений "Заявка" \ "Договор-заявка"
     *
     * @param advance - аванс
     * @param uuid    - uuid вложения
     * @return Advance - аванс
     */
    Advance setContractApplication(Advance advance, UUID uuid);

    /**
     * Установка uuid вложения "Заявка на авансирование"
     *
     * @param advance - аванс
     * @param uuid    - uuid вложения
     * @return Advance - аванс
     */
    Advance setAdvanceApplication(Advance advance, UUID uuid);

    /**
     * Сохранить аванс
     *
     * @param advance - аванс
     * @return Advance - аванс
     */
    Advance saveAdvance(Advance advance);

    /**
     * Сохранить список авансов
     *
     * @param advances - авансы
     * @return List<Advance> - авансы
     */
    List<Advance> saveAll(List<Advance> advances);

    /**
     * Уведомить о создании аванса по трипу
     *
     * @param advance - аванс
     */
    void notifyAboutAdvance(Advance advance);

    /**
     * Уведомить о создании аванса по трипу с задержкой
     *
     * @param advance - аванс
     */
    void notifyAboutAdvanceScheduled(Advance advance);

    /**
     * Уведомить о создании аванса тех, кто не прочитал
     * отправленные сообщения
     */
    void notifyUnread();

    /**
     * Установить признак того, что совершен
     * переход в ЛК по ссылке из сообщения -
     * сообщение прочитано
     *
     * @param advance - аванс
     */
    void setRead(Advance advance);

    /**
     * Статус выдачи аванса для поездки, если аванс выдан
     *
     * @param advance
     * @return tooltip - подсказка
     */
    TripAdvanceState checkAdvanceState(Advance advance);

}
