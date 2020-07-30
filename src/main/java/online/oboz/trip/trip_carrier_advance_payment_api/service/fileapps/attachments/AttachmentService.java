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
     *
     * @param advance
     * @return
     */
    ResponseEntity<Resource> downloadAdvanceRequestFromBstore(Advance advance);

    /**
     * Скачать "Заявку" из "B-Store"
     *
     * @param advance
     * @return
     */
    ResponseEntity<Resource> downloadRequestFromBstore(Advance advance);


    /**
     * Скачать шаблон "заявки" по авансу
     *
     * @param advance - аванс
     * @return
     */
    ResponseEntity<Resource> downloadAdvanceTemplate(Advance advance);


    /**
     * Скачать шаблон "заявки" по авансу
     *
     * @param id - аванс
     * @return
     */
    ResponseEntity downloadAdvanceTemplate(Long id);

    /**
     * Скачать "Заявку на аванс" по Трипу
     *
     * @param id - id аванса
     * @return
     */
    ResponseEntity<Resource> downloadAdvanceRequest(Long id);

//    /**
//     * Загрузить "Заявку" в Аванс?
//     *
//     * @param file    - файл
//     * @param id -  id аванса
//     * @return
//     */
//    ResponseEntity<Void> uploadRequestAdvance(MultipartFile file, Long id);


    /**
     * Скачать "Заявку" из "Аванса"
     *
     * @param id - id аванса
     * @return
     */
    ResponseEntity<Resource> downloadRequest(Long id);

    /**
     * Загрузить подписанный файл в аванс
     *
     * @param file - файл
     * @param id - аванс
     * @return
     */
    ResponseEntity<Void> uploadAssignment(MultipartFile file, Long id);


}
