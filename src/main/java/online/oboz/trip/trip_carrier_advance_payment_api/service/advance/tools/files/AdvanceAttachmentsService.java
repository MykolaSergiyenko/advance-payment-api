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
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
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
                PDDocument pdDoc = PdfUtils.loadPdf(resource);
                PDFRenderer pdfRenderer = new PDFRenderer(pdDoc);
                int numberOfPages = pdDoc.getNumberOfPages();
                log.info("[PDF] - количество страниц в файле: {}", numberOfPages);
                if (pageNum < numberOfPages)
                    throw attachmentsError("[PDF] - Page number for preview must be less than total PDF page number.");
                else {
                    /*
                     * 600 dpi give good image clarity but size of each image is 2x times of 300 dpi.
                     * Ex:  1. For 300dpi 04-Request-Headers_2.png expected size is 797 KB
                     *      2. For 600dpi 04-Request-Headers_2.png expected size is 2.42 MB
                     */
                    int dpi = 72;// use less dpi for to save more space in harddisk. For professional usage you can use more than 300dpi

                    String fileName = (resource.getFilename()).replace(".pdf", ("_" + pageNum) + ".png");

                    File outPutFile = new File(fileName);
                    BufferedImage bImage = pdfRenderer.renderImageWithDPI(pageNum - 1, dpi, ImageType.RGB);
                    ImageIO.write(bImage, "png", outPutFile);
                    PdfUtils.closePdf(pdDoc);

                    // delete image file if need
                    //outPutFile.deleteOnExit();

//                        return ResponseEntity.status(HttpStatus.OK)
//                            .header(HttpHeaders.CONTENT_DISPOSITION, "filename=\"image.png")
//                            .contentType(MediaType.IMAGE_PNG)
//                            .body(bImage);
                    return ResponseEntity.ok(bImage);
                }
            } else {
                log.info("Русерса нет в B-Store: {}", uuid);
            }
        } catch (Exception e) {
            throw attachmentsError("Get pdf-preview error: " + e.getMessage());

        }
        return null;
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
