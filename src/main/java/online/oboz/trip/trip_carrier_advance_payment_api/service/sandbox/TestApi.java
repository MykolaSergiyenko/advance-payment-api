package online.oboz.trip.trip_carrier_advance_payment_api.service.sandbox;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import online.oboz.trip.trip_carrier_advance_payment_api.service.advance.AdvanceService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.advance.tools.files.AttachmentService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.urleditor.UrlService;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.controller.AdvanceTestApiDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.util.UUID;

/**
 * Песочница для тестирования сервисов "Авансирования"
 */
@Service
public class TestApi implements AdvanceTestApiDelegate {
    private static final Logger log = LoggerFactory.getLogger(TestApi.class);

    private final UrlService shortenerService;
    private final AdvanceService service;
    private final AttachmentService attachmentService;

    public TestApi(
        UrlService shortenerService,
        AdvanceService service,
        AttachmentService attachmentService) {
        this.shortenerService = shortenerService;
        this.service = service;
        this.attachmentService = attachmentService;
    }

    @Override
    public ResponseEntity<Void> cutUrl(String stringUrl) {
        shortenerService.editUrl(stringUrl);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @Override
    public ResponseEntity<String> createMessage(Long advanceId) {
        log.info("Make notifications for advance - " + advanceId);
        Advance advance = service.findById(advanceId);
        service.notifyAboutAdvance(advance);
        log.info("Out of notifications.");
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Resource> getFilePreview(UUID uuid){
        log.info("Get first-page preview for pdf-file: {}.", uuid);
        BufferedImage b =  attachmentService.pdfPreviewFromBStore(uuid, 1).getBody();
        log.info("[PDF to PNG]: Preview size: {} x {}.", b.getHeight(), b.getWidth());
        return new ResponseEntity<>(new ByteArrayResource(b.toString().getBytes()), HttpStatus.OK);
    }
}
