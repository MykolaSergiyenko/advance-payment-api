package online.oboz.trip.trip_carrier_advance_payment_api.service.advance.pages.desktop;

import online.oboz.trip.trip_carrier_advance_payment_api.web.api.controller.AdvancesApiDelegate;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.AdvanceDesktopDTO;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

/**
 * Сервис для управления "Авансами"
 *
 * @author s‡udent
 */
public interface AdvanceDesktop extends AdvancesApiDelegate {
    Logger log = LoggerFactory.getLogger(AdvanceDesktop.class);


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
     * @param id              - аванс
     * @param loadingComplete - значение признака
     */
    ResponseEntity<Void> setLoadingComplete(Long id, Boolean loadingComplete);

    /**
     * Утвердить аванс (отправить в УНФ)
     *
     * @param id - аванс
     */
    ResponseEntity<Void> sendToUnfAdvance(Long id);


    /**
     * Отменить аванс по Трипу
     *
     * @param id      - аванс
     * @param comment - комментарий
     */
    ResponseEntity<Void> cancelAdvance(Long id, String comment);

    /**
     * Изменить комментарий в авансе
     *
     * @param id      - аванс
     * @param comment - комментарий
     */
    ResponseEntity<Void> changeComment(Long id, String comment);


    /**
     * Ссылка на скачивание файлов из B-Store по uuid файла -
     * используется в гриде "Рабочего стола"
     *
     * @param uuid - uuid файла из bStore, который везде хранится:
     *             request, trip_request ... - не важно
     * @return pdf-file
     */
    ResponseEntity<Resource> downloadFile(UUID uuid);

    /**
     * Получить превью PDF-файла
     *
     * @param uuid - uuid файла из bStore
     * @param page - номер страницы в файле
     * @return image\png
     */
    ResponseEntity<Resource> getPdfPreview(UUID uuid, Integer page);


    /**
     * Ограничение доступа к Рабочему столу авансирования
     */
    void checkAccess();
}
