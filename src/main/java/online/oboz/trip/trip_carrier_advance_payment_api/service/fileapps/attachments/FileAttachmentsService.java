package online.oboz.trip.trip_carrier_advance_payment_api.service.fileapps.attachments;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.base.attachments.TripAttachment;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.service.advance.AdvanceService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.fileapps.reports.ReportService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.fileapps.reports.ReportsTemplateService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.integration.bstore.StoreService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.integration.tripdocs.TripAttachmentService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.integration.tripdocs.TripDocumentsService;
import online.oboz.trip.trip_carrier_advance_payment_api.util.ErrorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@Service
public class FileAttachmentsService implements AttachmentService {
    private static final Logger log = LoggerFactory.getLogger(FileAttachmentsService.class);

    private final TripDocumentsService tripAttachmentService;
    private final StoreService bStoreService;
    private final ReportService reportsService;
    private final AdvanceService advanceService;

    @Autowired
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


    private Advance findAdvanceByNum(String tripNum) {
        return advanceService.findByTripNum(tripNum);
    }

    private Advance saveAdvance(Advance advance) {
        return advanceService.saveAdvance(advance);
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
    public ResponseEntity<Resource> downloadAdvanceRequestTemplate(Advance advance) {
        return reportsService.downloadAdvanceTemplate(advance);
    }

    @Override
    public ResponseEntity downloadAvanceRequestTemplateForCarrier(String tripNum) {
        log.info("Got downloadAvanceRequestTemplateForCarrier request tripNum - " + tripNum);
        return downloadAvanceRequestTemplate(tripNum);
    }

    @Override
    public ResponseEntity downloadAvanceRequestTemplate(String tripNum) {
        log.info("Got downloadAvanceRequestTemplate request tripNum - " + tripNum);
        Advance advance = findAdvanceByNum(tripNum);
        return reportsService.downloadAdvanceTemplate(advance);
    }

    @Override
    public ResponseEntity<Resource> downloadAdvanceRequest(String tripNum) {
        log.info("Got downloadAvanseRequest request tripNum - " + tripNum);
        Advance advance = findAdvanceByNum(tripNum);
        return downloadAdvanceRequestFromBstore(advance);
    }

    @Override
    public ResponseEntity<Resource> downloadRequest(String tripNum) {
        log.info("Got downloadRequest request tripNum - " + tripNum);
        Advance advance = findAdvanceByNum(tripNum);
        return downloadRequestFromBstore(advance);
    }

    @Override
    public ResponseEntity<Void> uploadRequestAvanceForCarrier(MultipartFile file, String tripNum) {
        log.info("Got uploadRequestAvanceForCarrier request tripNum - " + tripNum);
        return uploadRequestAdvance(file, tripNum);
    }


    @Override
    public void updateFileUuids() {
        List<Advance> advances = advanceService.findAdvancesWithoutFiles();
        log.info("Found {} advances without attachments. Try to update it.", advances.size());
        advances.forEach(advance -> {
            try {
                setUuids(tripAttachmentService.getTripAttachments(advance.getAdvanceTripFields().getTripId()),
                    advance);
            } catch (Exception e) {
                log.error("Error while updating file-uuids: {}", e.getMessage());
            }
        });
    }


    @Override
    public ResponseEntity<Void> uploadRequestAdvance(MultipartFile file, String tripNum) {
        log.info("Got uploadRequestAdvance request tripNum - " + tripNum);
        Advance advance = findAdvanceByNum(tripNum);
        if (advance.is1CSendAllowed() == null) {
            throw attachmentsError("Uploading of 'request-advance'-file forbidden. " +
                "Send to UNF after docs uploaded.");
        }
        uploadRequestAdvance(advance, file);
        return new ResponseEntity<>(OK);
    }


    //TODO :  catch   big size file and response

    private ResponseEntity<Void> uploadRequestAdvance(Advance advance, MultipartFile file) {
        try {
            UUID fileUuid = saveToBStore(file);
            saveFromBStore(advance, fileUuid);
            log.info("File was saved from BStore. Uuid = {}. Filename = {}. Advance = {}.", fileUuid, file, advance);
        } catch (BusinessLogicException e) {
            log.error("Upload 'advance-request' file error:" + e.getMessage());
        }
        return new ResponseEntity<>(OK);
    }


    private UUID getContractUuid(Advance advance) {
        return advance.getUuidContractApplicationFile();
    }

    private UUID getAdvanceRequestUuid(Advance advance) {
        return advance.getUuidAdvanceApplicationFile();
    }

    private ResponseEntity<Resource> fromBStore(UUID uuid) {
        return bStoreService.requestResourceFromBStore(uuid);
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
        advanceService.setAdvanceApplicationFromBstore(advance, fileUuid);
    }

    private void setUuids(List<TripAttachment> attachments, Advance advance) {
        int attachSize = attachments.size();
        log.info("Found {} file-attachments for advance {}.", attachSize, advance.getId());
        if (attachSize > 0) {
            setRequestUuid(advance, attachments);
            setAssignmentRequestUuid(advance, attachments);
        }
    }

    private void setRequestUuid(Advance advance, List<TripAttachment> attachments) {
        UUID uuid = tripAttachmentService.getRequestUuidOrTripRequestUuid(attachments);
        advanceService.setContractApplication(advance, uuid);
    }

    private void setAssignmentRequestUuid(Advance advance, List<TripAttachment> attachments) {
        UUID uuid = tripAttachmentService.getAssignmentRequestUuid(attachments);
        advanceService.setAdvanceApplication(advance, uuid);
    }

    private BusinessLogicException attachmentsError(String message) {
        return ErrorUtils.getInternalError(message);
    }

}
