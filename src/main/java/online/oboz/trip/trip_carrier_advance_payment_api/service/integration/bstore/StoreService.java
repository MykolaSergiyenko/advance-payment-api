package online.oboz.trip.trip_carrier_advance_payment_api.service.integration.bstore;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface StoreService {
    ResponseEntity<Resource> requestResourceFromBStore(UUID uuidFile);

    UUID saveFile(MultipartFile file);
}
