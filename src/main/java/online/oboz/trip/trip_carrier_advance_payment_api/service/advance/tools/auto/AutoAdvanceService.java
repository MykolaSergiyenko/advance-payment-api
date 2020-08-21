package online.oboz.trip.trip_carrier_advance_payment_api.service.advance.tools.auto;

import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;

import online.oboz.trip.trip_carrier_advance_payment_api.service.advance.AdvanceService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.advance.tools.contractors.ContractorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


/**
 * Сервис "Авто-авансирование" - выполняет операции "аванса" по расписаниям
 *
 * @author s‡udent
 */
@Service
@EnableScheduling
public class AutoAdvanceService implements AutoService {

    private static final Logger log = LoggerFactory.getLogger(AutoAdvanceService.class);
    private final ContractorService advanceContractorService;
    private final AdvanceService advanceService;


    @Autowired
    public AutoAdvanceService(
        ContractorService advanceContractorService,
        AdvanceService advanceService
    ) {
        this.advanceContractorService = advanceContractorService;
        this.advanceService = advanceService;
    }

    ///@Scheduled(cron = "0 0/3 * * * *")


    @Override
    @Scheduled(cron = "${services.auto-advance-service.cron.update}")
    public void updateAutoAdvanceContractors() {
        try {
            advanceContractorService.updateAutoAdvanceForContractors();
        } catch (BusinessLogicException e) {
            log.error("[Auto-advance]: Contractor error: {}.", e.getErrors());
        }
    }


    @Override
    @Scheduled(cron = "${services.auto-advance-service.cron.create}")
    public void createAutoAdvances() {
        try {
            advanceService.giveAutoAdvances();
        } catch (BusinessLogicException e) {
            log.error("Auto advance error: {}.", e.getErrors());
        }
    }


    @Override
    @Scheduled(cron = "${services.auto-advance-service.cron.notify}")
    public void notifyAgain() {
        try {
            advanceService.notifyUnread();
        } catch (BusinessLogicException e) {
            log.error("Scheduled Notifier error: {}.", e.getErrors());
        }
    }


    //@Scheduled(cron = "${services.auto-advance-service.cron.fix}")
    public void fixSums() {
        try {
            advanceService.fixSums();
        } catch (BusinessLogicException e) {
            log.error("Fix sums error: {}.", e.getErrors());
        }
    }
}
