package online.oboz.trip.trip_carrier_advance_payment_api.service.advance;

import online.oboz.trip.trip_carrier_advance_payment_api.domain.advance.trip.people.Person;
import online.oboz.trip.trip_carrier_advance_payment_api.error.BusinessLogicException;

import online.oboz.trip.trip_carrier_advance_payment_api.service.contractors.ContractorService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.fileapps.attachments.FileAttachmentsService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.persons.PersonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


@Service
@EnableScheduling
public class AutoAdvancedService implements BaseAutoAdvanceService {

    private static final Logger log = LoggerFactory.getLogger(AutoAdvancedService.class);
    private final ContractorService advanceContractorService;
    private final AdvanceService advanceService;

    private final FileAttachmentsService fileAttachmentsService;
    private final Person autoUser;


    @Autowired
    public AutoAdvancedService(
        ContractorService advanceContractorService,
        AdvanceService advanceService,
        PersonService personService,
        FileAttachmentsService fileAttachmentsService
    ) {
        this.advanceContractorService = advanceContractorService;
        this.advanceService = advanceService;
        this.fileAttachmentsService = fileAttachmentsService;

        this.autoUser = personService.getAdvanceSystemUser();
        log.info("Auto-advance system user is: " + autoUser);
    }

    @Override
    @Scheduled(cron = "${services.auto-advance-service.cron.update}")
    public void updateFileUuid() {
        try {
            fileAttachmentsService.updateFileUuids();
        } catch (BusinessLogicException e) {
            log.error("Error while update file-uuids:" + e.getErrors());
        }
    }


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
    @Scheduled(cron = "${services.auto-advance-service.cron.creation}")
    public void createAutoAdvances() {
        try {
            advanceService.giveAutoAdvances(autoUser);
        } catch (BusinessLogicException e) {
            log.error("Auto advance error:" + e.getErrors());
        }
    }


    @Override
    @Scheduled(cron = "${services.notifications.scheduler.notify}")
    public void notifyAgain() {
        try {
            advanceService.notifyUnread();
        } catch (BusinessLogicException e) {
            log.error("Scheduled Notifier error: " + e.getErrors());
        }
    }

}