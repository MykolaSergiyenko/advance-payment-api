package online.oboz.trip.trip_carrier_advance_payment_api.service.fileapps;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface AttachmentService {
    ResponseEntity<Resource> downloadAdvanceRequest(Advance advance);
    ResponseEntity<Resource> downloadRequest(Advance advance);
    ResponseEntity<Resource> downloadAdvanceRequestTemplate(Advance advance);
    ResponseEntity<Void> uploadRequestAdvance(Advance advance, MultipartFile filename);
    ResponseEntity<Advance> updateAttachmentsUuids(Advance advance);
    ResponseEntity<Boolean> isDownloadAllDocuments(Advance advance);
}
