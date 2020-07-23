package online.oboz.trip.trip_carrier_advance_payment_api.service.fileapps.attachments;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;


/**
 * Сервис для работы с файлами-вложениями
 */
public interface AttachmentService {


    /**
     * Загрузить "Заявку на аванс" из "B-Store"
     * @param advance
     * @return
     */
    ResponseEntity<Resource> downloadAdvanceRequestFromBstore(Advance advance);

    /**
     * Загрузить "Заявку" из "B-Store"
     * @param advance
     * @return
     */
    ResponseEntity<Resource> downloadRequestFromBstore(Advance advance);

    ResponseEntity<Resource> downloadAdvanceRequestTemplate(Advance advance);

    /**
     * Загрузить "Заявку" в Трип
     * @param file - файл
     * @param tripNum - номер трипа
     * @return
     */
    ResponseEntity<Void> uploadRequestAdvance(MultipartFile file, String tripNum);


    /**
     * Загрузить шаблон "заявки" по трипу
     * (со страницы перевозчика или нет - не важно)
     * @param tripNum
     * @return
     */
    ResponseEntity downloadAvanceRequestTemplate(String tripNum);

    /**
     * Скачать "Заявку на аванс" по Трипу
     * @param tripNum
     * @return
     */
    ResponseEntity<Resource> downloadAdvanceRequest(String tripNum);

    /**
     * Скачать "Заявку" по Трипу
     * @param tripNum
     * @return
     */
    ResponseEntity<Resource> downloadRequest(String tripNum);

    /**
     * Загрузить "Заявку на аванс" со страницы перевозчика
     * @param file
     * @param tripNum
     * @return
     */
    ResponseEntity<Void> uploadRequestAvanceForCarrier(MultipartFile file, String tripNum);


}
