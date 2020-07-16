package online.oboz.trip.trip_carrier_advance_payment_api.service.fileapps.attachments;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;


public interface AttachmentService {

    void updateFileUuids();

    ResponseEntity<Resource> downloadAdvanceRequestFromBstore(Advance advance);

    ResponseEntity<Resource> downloadRequestFromBstore(Advance advance);

    ResponseEntity<Resource> downloadAdvanceRequestTemplate(Advance advance);

    ResponseEntity<Void> uploadRequestAdvance(MultipartFile file, String tripNum);

    //ResponseEntity<Boolean> isDownloadAllDocuments(Advance advance);

    ResponseEntity downloadAvanceRequestTemplate(String tripNum);

    ResponseEntity downloadAvanceRequestTemplateForCarrier(String tripNum);

    ResponseEntity<Resource> downloadAdvanceRequest(String tripNum);

    ResponseEntity<Resource> downloadRequest(String tripNum);

    ResponseEntity<Void> uploadRequestAvanceForCarrier(MultipartFile file, String tripNum);


}
