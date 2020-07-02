package online.oboz.trip.trip_carrier_advance_payment_api.service;

import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;
import online.oboz.trip.trip_carrier_advance_payment_api.service.contractors.AdvanceContractorService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.fileapps.FileAttachmentsService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


@Service
@EnableScheduling
public class AutoAdvancedService {

    public static final String AUTO_ADVANCE_COMMENT = "Auto Created";

    private static final Logger log = LoggerFactory.getLogger(AutoAdvancedService.class);
    private final FileAttachmentsService fileService;
    private final AdvanceContractorService advanceContractorService;
    private final AdvanceService advanceService;


    @Autowired
    public AutoAdvancedService(
        FileAttachmentsService fileService,
        AdvanceContractorService advanceContractorService,
        AdvanceService advanceService) {
        this.advanceContractorService = advanceContractorService;
        this.advanceService = advanceService;
        this.fileService = fileService;
    }


    @Scheduled(cron = "${services.auto-advance-service.cron.update}")
    void updateFileUuid() {
        try {
            advanceService.updateFileUuids();
        } catch (BusinessLogicException e) {
            log.error("Error while update file-uuids:" + e.getErrors());
        }

    }

    // Update contractor's "is auto advance"
    //@Scheduled(cron = "${services.auto-advance-service.cron.update}")
    @Scheduled(cron = "0 0/1 * * * *")
    void updateAutoAdvanceForContractors() {
        try {
            advanceContractorService.updateAutoAdvanceForContractors();
        } catch (BusinessLogicException e) {
            log.error("Auto advance-contractor error:" + e.getErrors());
        }
    }

    //@Scheduled(cron = "${services.auto-advance-service.cron.creation}")
    @Scheduled(cron = "0 0/1 * * * *")
    void createAutoAdvance() {
        try {
            advanceService.giveAutoAdvances();
        } catch (BusinessLogicException e) {
            log.error("Auto advance error:" + e.getErrors());
        }
    }


}
