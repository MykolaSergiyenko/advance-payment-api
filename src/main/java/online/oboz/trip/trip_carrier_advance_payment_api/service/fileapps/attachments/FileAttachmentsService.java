package online.oboz.trip.trip_carrier_advance_payment_api.service.fileapps.attachments;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.service.advance.AdvanceService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.fileapps.reports.ReportsTemplateService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.integration.bstore.StoreService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.integration.ordersapi.OrdersFilesService;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.Error;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Service
public class FileAttachmentsService implements AttachmentService {
    private static final Logger log = LoggerFactory.getLogger(FileAttachmentsService.class);

    private final OrdersFilesService ordersApiService;
    private final StoreService bStoreService;
    private final ReportsTemplateService reportsService;
    private final AdvanceService advanceService;

    public FileAttachmentsService(
        OrdersFilesService ordersApiService,
        StoreService bStoreService,
        ReportsTemplateService reportsService, AdvanceService advanceService
    ) {
        this.ordersApiService = ordersApiService;
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
    public ResponseEntity<Void> uploadRequestAvanceForCarrier(MultipartFile filename, String tripNum) {
        log.info("Got uploadRequestAvanceForCarrier request tripNum - " + tripNum);
        return uploadRequestAdvance(filename, tripNum);
    }


    @Override
    public void updateFileUuids() {
        List<Advance> advances = advanceService.findAdvancesWithoutFiles();
        advances.forEach(advance -> {
            if (null != advance.getAdvanceTripFields().getTripId()) {
                Map<String, String> fileUuidMap = ordersApiService.findTripRequestDocs(advance);
                if (!fileUuidMap.isEmpty()) {
                    String fileContractRequestUuid = Optional.ofNullable(fileUuidMap.get("request"))
                        .orElse(fileUuidMap.get("trip_request"));

                    if (fileContractRequestUuid != null) {
                        //advance.setDownloadedContractApplication(true);
                        advance.setUuidContractApplicationFile(fileContractRequestUuid);
                        log.info("UuidContractApplicationFile is: {}", advance.getUuidContractApplicationFile());
                    }

                    String fileAdvanceRequestUuid = fileUuidMap.get("assignment_advance_request");
                    if (fileAdvanceRequestUuid != null) {
                        //advance.setDownloadedContractApplication(true);
                        advance.setUuidAdvanceApplicationFile(fileAdvanceRequestUuid);
                        log.info("UuidAdvanceApplicationFile is: {}", advance.getUuidAdvanceApplicationFile());
                    }

                    if (advance.getUuidContractApplicationFile() != null &&
                        advance.getUuidAdvanceApplicationFile() != null) {
                        advance.setIs1CSendAllowed(true);
                        log.info("Is1CSendAllowed set true for advance: {}", advance);
                    }
                }
            }
            advanceService.saveAdvance(advance);
        });
    }

    @Override
    public ResponseEntity<Void> uploadRequestAdvance(MultipartFile filename, String tripNum) {
        log.info("Got uploadRequestAdvance request tripNum - " + tripNum);
        Advance advance = findAdvanceByNum(tripNum);
        if (advance.isPushedUnfButton()) {
            throw attachmentsError("Uploading of 'request-advance'-file forbidden. " +
                "Send to UNF after docs uploaded.");
        }
        uploadRequestAdvance(advance, filename);
        saveAdvance(advance);
        return new ResponseEntity<>(OK);
    }


    //        TODO :  catch   big size file and response
    private ResponseEntity<Void> uploadRequestAdvance(Advance advance, MultipartFile filename) {
        try {
            String fileUuid = bStoreService.getFileUuid(filename);
            if (fileUuid != null) {
                Long orderId = advance.getAdvanceTripFields().getOrderId();
                Long tripId = advance.getAdvanceTripFields().getTripId();
                if (ordersApiService.saveTripDocuments(orderId, tripId, fileUuid)) {
                    advance.setUuidAdvanceApplicationFile(fileUuid);
                } else {
                    return new ResponseEntity<>(INTERNAL_SERVER_ERROR);
                }
            }
        } catch (BusinessLogicException e) {
            log.error("Upload 'advance-request' file error:" + e.getMessage());
        }
        return new ResponseEntity<>(OK);
    }


    private UUID getContractUuid(Advance advance) {
        return UUID.fromString(advance.getUuidContractApplicationFile());
    }

    private UUID getAdvanceRequestUuid(Advance advance) {
        return UUID.fromString(advance.getUuidAdvanceApplicationFile());
    }

    private ResponseEntity<Resource> fromBStore(UUID uuid) {
        return bStoreService.requestResourceFromBStore(uuid);
    }

    private BusinessLogicException attachmentsError(String message) {
        return getInternalBusinessError(getServiceError(message), INTERNAL_SERVER_ERROR);
    }

    private Error getServiceError(String errorMessage) {
        Error error = new Error();
        error.setErrorMessage("PersonsService - Business Error: " + errorMessage);
        return error;
    }

    private BusinessLogicException getInternalBusinessError(Error error, HttpStatus state) {
        log.error(state.name() + " : " + error.getErrorMessage());
        return new BusinessLogicException(state, error);
    }


}