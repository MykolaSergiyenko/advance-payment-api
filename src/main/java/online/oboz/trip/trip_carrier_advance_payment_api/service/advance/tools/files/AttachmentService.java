package online.oboz.trip.trip_carrier_advance_payment_api.service.advance.tools.files;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.util.UUID;


/**
 * Сервис для работы с файлами-вложениями
 *
 * @author s‡oodent
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
    ResponseEntity<Resource> downloadTemplate(Long id);


    /**
     * Скачать шаблон "заявки" по авансу
     *
     * @param uuid - аванс
     * @return
     */
    ResponseEntity<Resource> downloadTemplate(UUID uuid);



    /**
     * Загрузить подписанный файл в аванс
     *
     * @param file - файл
     * @param id   - аванс
     * @return
     */
    ResponseEntity<Void> uploadAssignment(MultipartFile file, Long id);


    /**
     * Скачать файл из хранилища по uuid
     *
     * @param uuid
     * @return
     */
    ResponseEntity<Resource> fromBStore(UUID uuid);

    //ResponseEntity<BufferedImage> pdfPreviewFromBStore(UUID uuid, Integer pageNum);
    ResponseEntity<BufferedImage> pdfPreviewFromBStore(UUID uuid, Integer pageNum);

    ResponseEntity<Resource> getPdfPreview(UUID uuid, Integer pageNum);
}
