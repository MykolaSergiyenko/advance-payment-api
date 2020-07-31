package online.oboz.trip.trip_carrier_advance_payment_api.service.advance.pages.carrier;

import online.oboz.trip.trip_carrier_advance_payment_api.web.api.controller.AdvanceCarrierApiDelegate;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.CarrierPageDTO;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Сервис "Авансирование" - страница перевозчика
 */
public interface CarrierScreen extends AdvanceCarrierApiDelegate {


    /**
     * Запрос страницы "Аванса" - экран Перевозчика
     *
     * @param uuid - аванс
     * @return CarrierPageDTO - страница Перевозчика
     */
    @Override
    ResponseEntity<CarrierPageDTO> getAdvance(UUID uuid);

    /**
     * Кнопка 'Хочу аванс' - экран Перевозчика
     *
     * @param uuid - аванс
     * @return
     */
    @Override
    ResponseEntity<Void> carrierWantsAdvance(UUID uuid);

    /**
     * Кнопка 'Скачать шаблон' - экран Перевозчика
     * выполняется запрос в ReportService за щаблонной заявкой на аванс.
     * <p>
     * // TODO: использовать ReportService напрямую с фронта ? - все данные для шаблона есть, ссылка на сервис - постоянная.
     *
     * @param id - аванс
     * @return PDF-файл щаблонной заявки
     */
    @Override
    ResponseEntity<Resource> downloadTemplate(Long id);


    /**
     * Кнопка 'Загрузить подписанную заявку' - экран Перевозчика
     *
     * @param filename - файл
     * @param id       - аванс
     * @return
     */
    ResponseEntity<Void> uploadAssignment(MultipartFile filename, Long id);


}
