package online.oboz.trip.trip_carrier_advance_payment_api.service.fileapps;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.Advance;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.service.integration.BStoreService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.integration.OrdersApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Service
public class FileAttachmentsService implements AttachmentService {
    private static final Logger log = LoggerFactory.getLogger(FileAttachmentsService.class);


    private final OrdersApiService ordersApiService;
    private final BStoreService bStoreService;
    private final ReportsTemplateService reportsService;


    public FileAttachmentsService(
        OrdersApiService ordersApiService,
        BStoreService bStoreService,
        ReportsTemplateService reportsService) {
        this.ordersApiService = ordersApiService;
        this.bStoreService = bStoreService;
        this.reportsService = reportsService;
    }

    @Override
    public ResponseEntity<Resource> downloadAdvanceRequest(Advance advance) {
        return fromBStore(getAdvanceRequestUuid(advance));
    }

    @Override
    public ResponseEntity<Resource> downloadRequest(Advance advance) {
        return fromBStore(getContractUuid(advance));
    }

    @Override
    public ResponseEntity<Resource> downloadAdvanceRequestTemplate(Advance advance) {
        return reportsService.downloadAdvanceRequestTemplate(advance);
    }


    //        TODO :  catch   big size file and response
    @Override
    public ResponseEntity<Void> uploadRequestAdvance(Advance advance, MultipartFile filename) {
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

    @Override
    public ResponseEntity<Advance> updateAttachmentsUuids(Advance advance) {
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
        return new ResponseEntity<>(advance, OK);
    }

    public  ResponseEntity<Boolean> isDownloadAllDocuments(Advance advance) {
        //использовать только в confirm
        Map<String, String> fileRequestUuidMap = ordersApiService.findTripRequestDocs(advance);
        Map<String, String> fileAdvanceRequestUuidMap = ordersApiService.findAdvanceRequestDocs(advance);
        String requestFileUuid = Optional.ofNullable(fileRequestUuidMap.get("request"))
            .orElse(fileRequestUuidMap.get("trip_request"));
        String advanceRequestFileUuid = fileAdvanceRequestUuidMap.get("assignment_advance_request");
        boolean isAllDocsUpload = requestFileUuid != null && advanceRequestFileUuid != null;
        if (!isAllDocsUpload) {
            log.info("Не загружены документы. " + advance.getUuid());
        }
        return new ResponseEntity<>(isAllDocsUpload, OK);
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





}
