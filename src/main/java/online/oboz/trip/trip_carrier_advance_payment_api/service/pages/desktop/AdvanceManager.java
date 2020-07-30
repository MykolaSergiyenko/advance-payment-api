package online.oboz.trip.trip_carrier_advance_payment_api.service.pages.desktop;

import online.oboz.trip.trip_carrier_advance_payment_api.web.api.controller.AdvancesApiDelegate;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.AdvanceDesktopDTO;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

/**
 * Сервис для управления "Авансами"
 */
public interface AdvanceManager extends AdvancesApiDelegate {
    Logger log = LoggerFactory.getLogger(AdvanceManager.class);


    /**
     * Загрузить вкладку со списком авансов - Рабочий стол авансирвоания
     *
     * @param tab    - фильтр-вкладка
     * @param filter - фильтр-пагинатор
     * @return
     */
    ResponseEntity<AdvanceDesktopDTO> search(String tab, Filter filter);

    /**
     * Обновить факт загрузки водителя
     *
     * @param advanceId       - аванс
     * @param loadingComplete - значение признака
     */
    ResponseEntity<Void> setLoadingComplete(Long advanceId, Boolean loadingComplete);

    /**
     * Утвердить аванс (отправить в УНФ)
     *
     * @param advanceId - аванс
     */
    ResponseEntity<Void> sendToUnfAdvance(Long advanceId);


    /**
     * Отменить аванс по Трипу
     *
     * @param advanceId     - аванс
     * @param cancelComment - комментарий
     */
    ResponseEntity<Void> cancelAdvance(Long id, String comment);

    /**
     * Изменить комментарий в авансе
     *
     * @param id      - аванс
     * @param comment - комментарий
     */
    ResponseEntity<Void> changeComment(Long id, String comment);


    void checkAccess();
}