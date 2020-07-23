package online.oboz.trip.trip_carrier_advance_payment_api.service.advance.auto;

import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;

import online.oboz.trip.trip_carrier_advance_payment_api.service.advance.AdvanceService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.contractors.ContractorService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.fileapps.attachments.FileAttachmentsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


@Service
@EnableScheduling
public class AutoAdvancedService implements AutoService {

    private static final Logger log = LoggerFactory.getLogger(AutoAdvancedService.class);
    private final ContractorService advanceContractorService;
    private final AdvanceService advanceService;



    @Autowired
    public AutoAdvancedService(
        ContractorService advanceContractorService,
        AdvanceService advanceService
    ) {
        this.advanceContractorService = advanceContractorService;
        this.advanceService = advanceService;
    }

    ///@Scheduled(cron = "0 0/3 * * * *")

//    @Override
//    @Scheduled(cron = "${services.auto-advance-service.cron.update}")
//    public void updateFileUuid() {
//        log.info("Auto-advance: update attachment's uuids schedule started.");
//        try {
//                List<Advance> advances = advanceService.findAdvancesWithoutFiles();
//                log.info("Found {} advances without attachments. Try to update it.", advances.size());
//                advances.forEach(advance -> { fileAttachmentsService.setAttachmentsForAdvance(advance);});
//        } catch (BusinessLogicException e) {
//            log.error("Error while update file-uuids:" + e.getErrors());
//        }
//    }


    @Override
    @Scheduled(cron = "${services.auto-advance-service.cron.update}")
    public void updateAutoAdvanceForContractors() {
        try {
            advanceContractorService.updateAutoAdvanceForContractors();
        } catch (BusinessLogicException e) {
            log.error("Auto advance-contractor error:" + e.getErrors());
        }
    }


    @Override
    @Scheduled(cron = "${services.auto-advance-service.cron.create}")
    public void createAutoAdvances() {
        try {
            advanceService.giveAutoAdvances();
        } catch (BusinessLogicException e) {
            log.error("Auto advance error:" + e.getErrors());
        }
    }


    @Override
    @Scheduled(cron = "${services.auto-advance-service.cron.notify}")
    public void notifyAgain() {
        try {
            advanceService.notifyUnread();
        } catch (BusinessLogicException e) {
            log.error("Scheduled Notifier error: " + e.getErrors());
        }
    }

}
