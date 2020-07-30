package online.oboz.trip.trip_carrier_advance_payment_api.service.fileapps.attachments;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.service.AdvanceService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.fileapps.reports.ReportService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.fileapps.reports.ReportsTemplateService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.integration.bstore.StoreService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.integration.tripdocs.TripDocumentsService;
import online.oboz.trip.trip_carrier_advance_payment_api.util.ErrorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

import static org.springframework.http.HttpStatus.*;

@Service
public class FileAttachmentsService implements AttachmentService {
    private static final Logger log = LoggerFactory.getLogger(FileAttachmentsService.class);

    private final TripDocumentsService tripAttachmentService;
    private final StoreService bStoreService;
    private final ReportService reportsService;
    private final AdvanceService advanceService;

    public FileAttachmentsService(
        TripDocumentsService tripAttachmentService,
        StoreService bStoreService,
        ReportsTemplateService reportsService,
        AdvanceService advanceService
    ) {
        this.tripAttachmentService = tripAttachmentService;
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

    public ResponseEntity<Resource> fromBStore(UUID uuid) {
        return bStoreService.requestResourceFromBStore(uuid);
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
        tripAttachmentService.saveAssignmentAdvanceRequestUuid(tripId, fileUuid);

    }

    private void saveToAdvance(Advance advance, UUID fileUuid) {
        advanceService.setAdvanceApplication(advance, fileUuid);
    }


    private BusinessLogicException attachmentsError(String message) {
        return ErrorUtils.getInternalError(message);
    }

}
