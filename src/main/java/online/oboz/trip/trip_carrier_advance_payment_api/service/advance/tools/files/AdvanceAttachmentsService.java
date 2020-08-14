package online.oboz.trip.trip_carrier_advance_payment_api.service.advance.tools.files;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.service.advance.AdvanceService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.advance.tools.files.bstore.StoreService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.advance.tools.files.reports.ReportService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.advance.tools.files.reports.ReportsTemplateService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.advance.tools.files.tripdocs.TripDocumentsService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.util.ErrorUtils;
import org.springframework.http.HttpStatus;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
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
    public ResponseEntity<BufferedImage> previewFromBStore(UUID uuid, Integer pageNum) {


        try {
            File file = bStoreService.requestResourceFromBStore(uuid).getBody().getFile();

            if (file.exists()) {
                log.info("--- File is: {}", file.getName());
                PDDocument document = PdfHelper.loadPdf(file);
                log.info("--- PDDocument is: {}", document.getVersion());
                PDFRenderer pdfRenderer = new PDFRenderer(document);
                int numberOfPages = document.getNumberOfPages();
                log.info("--- numberOfPages is: {}", numberOfPages);
                if (pageNum < numberOfPages) throw attachmentsError("Page number for preview must be less than total PDF page number.");
                else {
                    PDPage page = document.getPage(pageNum - 1);


                    String fileName = file.getName().replace(".pdf", "");
                    String fileExtension= "png";
                    /*
                     * 600 dpi give good image clarity but size of each image is 2x times of 300 dpi.
                     * Ex:  1. For 300dpi 04-Request-Headers_2.png expected size is 797 KB
                     *      2. For 600dpi 04-Request-Headers_2.png expected size is 2.42 MB
                     */
                    int dpi = 300;// use less dpi for to save more space in harddisk. For professional usage you can use more than 300dpi


                    File outPutFile = new File( fileName +"_"+ (pageNum - 1) +"."+ fileExtension);
                    BufferedImage bImage = pdfRenderer.renderImageWithDPI(pageNum - 1, dpi, ImageType.RGB);
                    ImageIO.write(bImage, "png", outPutFile);


                    PdfHelper.closePdf(document);
                      return ResponseEntity.status(HttpStatus.OK)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "filename=\"image.png")
                            .contentType(MediaType.IMAGE_PNG)
                            .body(bImage);
                }

            } else {
                System.err.println(file.getName() +" File not exists");
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        log.info("Attachments: download 'Advance-template-request' for advance: {}.", id);
        Advance advance = findAdvance(id);
        return downloadAdvanceTemplate(advance);
    }


    @Override
    public ResponseEntity<Resource> downloadTemplate(UUID uuid) {
        log.info("Attachments: download 'template' for advance: {}.", uuid);
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
        log.info("Attachments: upload 'Request' for adavance: {}.", advanceId);
        Advance advance = findAdvance(advanceId);
        uploadRequestAdvance(advance, file);
        return new ResponseEntity<>(OK);
    }

    private ResponseEntity<Void> uploadRequestAdvance(Advance advance, MultipartFile file) {
        try {
            UUID fileUuid = saveToBStore(file);
            saveFromBStore(advance, fileUuid);
            log.info("Attachments: File saved from BStore. Uuid = {}. Filename = {}. Advance = {}.", fileUuid, file, advance);
        } catch (BusinessLogicException e) {
            log.error("Upload 'advance-request' file error:" + e.getErrors());
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
        log.info("Try to save file to BStore: {}", file.getName());
        return bStoreService.saveFile(file);

    }

    private void saveFromBStore(Advance advance, UUID fileUuid) {
        Long tripId = advance.getAdvanceTripFields().getTripId();
        log.info("Try to save file-uuid from BStore to Trip: {}.", tripId);
        saveToTripAttachments(tripId, fileUuid);
        log.info("Try to save file-uuid from BStore to Advance: {}.", advance.getId());
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
