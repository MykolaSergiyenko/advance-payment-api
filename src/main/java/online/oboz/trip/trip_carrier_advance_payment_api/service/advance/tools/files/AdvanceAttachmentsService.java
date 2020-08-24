package online.oboz.trip.trip_carrier_advance_payment_api.service.advance.tools.files;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.service.advance.AdvanceService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.advance.tools.files.bstore.StoreService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.advance.tools.files.reports.ReportService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.advance.tools.files.reports.ReportsTemplateService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.advance.tools.files.tripdocs.TripDocumentsService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.util.ErrorUtils;
import online.oboz.trip.trip_carrier_advance_payment_api.service.util.PdfUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.UUID;

import static org.springframework.http.HttpStatus.*;

/**
 * Сервис для работы с вложениями аванса
 *
 * @author s‡oodent
 */
@Service
public class AdvanceAttachmentsService implements AttachmentService {
    private static final Logger log = LoggerFactory.getLogger(AdvanceAttachmentsService.class);

    private final TripDocumentsService tripDocumentsService;
    private final StoreService bStoreService;
    private final ReportService reportsService;
    private final AdvanceService advanceService;

    @Value("${services.bstore.preview-dpi}")
    private Integer previewDPI;

    public AdvanceAttachmentsService(
        TripDocumentsService tripDocumentsService,
        StoreService bStoreService,
        ReportsTemplateService reportsService,
        AdvanceService advanceService
    ) {
        this.tripDocumentsService = tripDocumentsService;
        this.bStoreService = bStoreService;
        this.reportsService = reportsService;
        this.advanceService = advanceService;
    }


    private Advance findAdvance(Long id) {
        return advanceService.findById(id);
    }

    private Advance findAdvanceByUuid(UUID uuid) {
        return advanceService.findByUuid(uuid);
    }

    @Override
    public ResponseEntity<Resource> fromBStore(UUID uuid) {
        return bStoreService.requestResourceFromBStore(uuid);
    }

    @Override
    public ResponseEntity<BufferedImage> pdfPreviewFromBStore(UUID uuid, Integer pageNum) {
        try {
            Resource resource = bStoreService.requestResourceFromBStore(uuid).getBody();
            if (null != resource) {
                PDDocument pdDoc = loadPdf(resource, pageNum);
                BufferedImage bImage = renderPage(pdDoc, pageNum);
                closePdf(pdDoc);

                //save image file if need
                savePreviewImage(bImage, resource.getFilename(), uuid, pageNum);

                //delete image file if need
                //outPutFile.deleteOnExit();

                //return ResponseEntity.status(HttpStatus.OK)
                //    .header(HttpHeaders.CONTENT_DISPOSITION, "filename=\"image.png")
                //    .contentType(MediaType.IMAGE_PNG)
                //    .body(bImage);

                return ResponseEntity.ok(bImage);
            } else {
                log.info("Русерса нет в B-Store: {}", uuid);
            }
        } catch (Exception e) {
            throw attachmentsError("Ошибка получения превью: " + e.getMessage());
        }
        return null;
    }


    public ResponseEntity<Resource> getPdfPreview(UUID uuid, Integer pageNum) {
        try {
            BufferedImage b = pdfPreviewFromBStore(uuid, pageNum).getBody();
            log.info("[PDF to PNG]: - [{}] - Страница: {}. DPI: {}. Размер превью: {} x {}.",
                uuid, pageNum, previewDPI, b.getHeight(), b.getWidth());
            return PdfUtils.imageToPng(b);
        } catch (BusinessLogicException e) {
            log.error("[PDF to PNG]: - [{}] - Страница: {}. Ошибка: {}.", uuid, pageNum, e.getErrors());
            return ResponseEntity.badRequest().build();
        }
    }

    private String getPreviewFileName(String resource, UUID uuid, Integer pageNum) {
        return PdfUtils.getPreviewFileName(resource, uuid, pageNum);
    }

    private void savePreviewImage(BufferedImage bImage, String resource, UUID uuid, Integer pageNum) {
        PdfUtils.saveImage(getPreviewFileName(resource, uuid, pageNum), bImage);
    }

    private PDDocument loadPdf(Resource resource, Integer pageNum) {
        try {
            return PdfUtils.loadPdf(resource, pageNum);
        } catch (IOException e) {
            throw attachmentsError("[PDF]: Ошибка загрузки файла " + resource.getFilename() + ": " + e.getMessage());
        }
    }

    private void closePdf(PDDocument pdf) {
        PdfUtils.closePdf(pdf);
    }

    private BufferedImage renderPage(PDDocument pdDoc, Integer pageNum) {
        try {
            return PdfUtils.renderToImage(pdDoc, pageNum, previewDPI);
        } catch (IOException e) {
            throw attachmentsError("[PDF]: Ошибка рендеринга страницы " + pageNum +
                ", dpi=" + previewDPI + ": " + e.getMessage());
        }
    }


    @Override
    public ResponseEntity<Resource> downloadAdvanceRequestFromBstore(Advance advance) {
        return fromBStore(getAdvanceRequestUuid(advance));
    }

    @Override
    public ResponseEntity<Resource> downloadRequestFromBstore(Advance advance) {
        return fromBStore(getContractUuid(advance));
    }

    @Override
    public ResponseEntity<Resource> downloadAdvanceTemplate(Advance advance) {
        return reportsService.downloadAdvanceTemplate(advance);
    }


    @Override
    public ResponseEntity<Resource> downloadTemplate(Long id) {
        log.info("[Файлы аванса]: загрузить шаблонную 'заявку на авнас' по авансу: {}.", id);
        Advance advance = findAdvance(id);
        return downloadAdvanceTemplate(advance);
    }


    @Override
    public ResponseEntity<Resource> downloadTemplate(UUID uuid) {
        log.info("[Файлы аванса]: загрузить шаблонную 'заявку на авнас' по авансу: {}.", uuid);
        Advance advance = findAdvanceByUuid(uuid);
        return downloadAdvanceTemplate(advance);
    }

    @Override
    public ResponseEntity<Resource> downloadAdvanceRequest(Long id) {
        log.info("Attachments: download 'Advance-Request' request for advance: {}.", id);
        Advance advance = findAdvance(id);
        return downloadAdvanceRequestFromBstore(advance);
    }

    @Override
    public ResponseEntity<Resource> downloadRequest(Long id) {
        log.info("Attachments: download 'Request' for advance: {}.", id);
        Advance advance = findAdvance(id);
        return downloadRequestFromBstore(advance);
    }


    @Override
    public ResponseEntity<Void> uploadAssignment(MultipartFile file, Long advanceId) {
        log.info("[Файлы аванса]: загрузить подписанную заявку на аванс: {}.", advanceId);
        Advance advance = findAdvance(advanceId);
        uploadRequestAdvance(advance, file);
        return new ResponseEntity<>(OK);
    }

    private ResponseEntity<Void> uploadRequestAdvance(Advance advance, MultipartFile file) {
        try {
            UUID fileUuid = saveToBStore(file);
            saveFromBStore(advance, fileUuid);
            log.info("[Файлы аванса]: Файл сохранен из B-Store. Uuid = {}. Filename = {}. Advance = {}.", fileUuid, file, advance);
        } catch (BusinessLogicException e) {
            log.error("[Файлы аванса]: Ошибка загрузки файла по авансу: {}. Ошибка: {}.", advance.getId(), e.getErrors());
        }
        return new ResponseEntity<>(OK);
    }


    private UUID getContractUuid(Advance advance) {
        return advance.getUuidContractApplicationFile();
    }

    private UUID getAdvanceRequestUuid(Advance advance) {
        return advance.getUuidAdvanceApplicationFile();
    }

    private UUID saveToBStore(MultipartFile file) {
        log.info("[Файлы аванса]: Сохранить файл в B-Store: {}", file.getName());
        return bStoreService.saveFile(file);

    }

    private void saveFromBStore(Advance advance, UUID fileUuid) {
        Long tripId = advance.getAdvanceTripFields().getTripId();
        log.info("Сохранить файл из B-Store для поездки: {}.", tripId);
        saveToTripAttachments(tripId, fileUuid);
        log.info("Сохранить файл из B-Store для аванса: {}.", advance.getId());
        saveToAdvance(advance, fileUuid);
    }

    private void saveToTripAttachments(Long tripId, UUID fileUuid) {
        tripDocumentsService.saveAssignmentAdvanceRequestUuid(tripId, fileUuid);
    }

    private void saveToAdvance(Advance advance, UUID fileUuid) {
        advanceService.setAdvanceApplication(advance, fileUuid);
    }


    private BusinessLogicException attachmentsError(String message) {
        return ErrorUtils.getInternalError(message);
    }

}
