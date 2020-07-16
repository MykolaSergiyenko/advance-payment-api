package online.oboz.trip.trip_carrier_advance_payment_api.service.integration.bstore;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Сервис для работы с хранилищем файлов "B-Store"
 */
public interface StoreService {
    /**
     * Загрузить файл из "B-Store"
     * @param uuidFile
     * @return Resource
     */
    ResponseEntity<Resource> requestResourceFromBStore(UUID uuidFile);

    /**
     * Сохранить файл в "B-Store"
     * @param file - файл с аттрибутами
     * @return UUID файла из "B-Store"
     */
    UUID saveFile(MultipartFile file);
}
