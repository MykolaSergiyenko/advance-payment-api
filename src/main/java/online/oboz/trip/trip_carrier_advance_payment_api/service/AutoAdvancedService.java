package online.oboz.trip.trip_carrier_advance_payment_api.service;

import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.*;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.AdvancePaymentCostRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.ContractorRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.TripRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.AdvanceRequestRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.service.dto.MessageDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static online.oboz.trip.trip_carrier_advance_payment_api.util.DtoUtils.getMessageDto;

@Service
@EnableScheduling
public class AutoAdvancedService {

    private static final Logger log = LoggerFactory.getLogger(AutoAdvancedService.class);
    private final AdvancePaymentCostRepository advancePaymentCostRepository;
    private final AdvanceRequestRepository advanceRequestRepository;
    private final TripRepository tripRepository;
    private final ContractorRepository contractorRepository;
    private final ApplicationProperties applicationProperties;
    private final AdvancePaymentContactService advancePaymentContactService;
    private final NotificationService notificationService;
    private final RestService restService;

    @Autowired
    public AutoAdvancedService(
        AdvancePaymentCostRepository advancePaymentCostRepository,
        AdvanceRequestRepository advanceRequestRepository,
        TripRepository tripRepository,
        ContractorRepository contractorRepository,
        ApplicationProperties applicationProperties,
        AdvancePaymentContactService advancePaymentContactService,
        NotificationService notificationService, RestService restService
    ) {
        this.advancePaymentCostRepository = advancePaymentCostRepository;
        this.advanceRequestRepository = advanceRequestRepository;
        this.tripRepository = tripRepository;
        this.contractorRepository = contractorRepository;
        this.applicationProperties = applicationProperties;
        this.advancePaymentContactService = advancePaymentContactService;
        this.notificationService = notificationService;
        this.restService = restService;
    }

    @Scheduled(cron = "${cron.creation: 0 0/30 * * * *}")
    void createTripRequestAdvancePayment() {
        tripRepository.getAutoApprovedTrips().forEach(trip -> {
                final Long id = trip.getId();
                AdvancePaymentCost advanceCost = advancePaymentCostRepository.getAdvancePaymentCost(trip.getCost());
                log.info("start createTripRequestAdvancePayment for tripId: {}, with cost: {}", id, trip.getCost());
                if (advanceCost == null) {
                    log.info("not found cost  for tripId: {} from advancePaymentCostRepository", id);
                    return;
                }
                TripRequestAdvancePayment tripRequestAdvancePayment = new TripRequestAdvancePayment();
                tripRequestAdvancePayment.setTripId(id);
                tripRequestAdvancePayment.setContractorId(trip.getContractorId());
                tripRequestAdvancePayment.setDriverId(trip.getDriverId());
                tripRequestAdvancePayment.setTripTypeCode(trip.getTripTypeCode());
                tripRequestAdvancePayment.setPaymentContractorId(trip.getPaymentContractorId());
                tripRequestAdvancePayment.setIsAutomationRequest(true);
                tripRequestAdvancePayment.setTripCost(trip.getCost());
                tripRequestAdvancePayment.setAdvancePaymentSum(advanceCost.getAdvancePaymentSum());
                tripRequestAdvancePayment.setRegistrationFee(advanceCost.getRegistrationFee());
                tripRequestAdvancePayment.setLoadingComplete(false);
                tripRequestAdvancePayment.setPageCarrierUrlIsAccess(true);
                tripRequestAdvancePayment.setIs1CSendAllowed(true);
                tripRequestAdvancePayment.setIsCancelled(false);
                tripRequestAdvancePayment.setIsPushedUnfButton(false);
                tripRequestAdvancePayment.setIsPaid(false);
                tripRequestAdvancePayment.setComment("Auto Created");
                tripRequestAdvancePayment.setIsUnfSend(false);
                tripRequestAdvancePayment.setIsDownloadedAdvanceApplication(false);
                tripRequestAdvancePayment.setIsDownloadedContractApplication(false);
                tripRequestAdvancePayment.setAdvanceUuid(UUID.randomUUID());
                ContractorAdvancePaymentContact contact = advancePaymentContactService.getAdvancePaymentContact(
                    trip.getContractorId()
                );
                advanceRequestRepository.save(tripRequestAdvancePayment);
                log.info("tripRequestAdvancePayment for id : {}, is auto crated", tripRequestAdvancePayment.getId());

                if (contact != null) {
                    String paymentContractor = contractorRepository.getFullName(trip.getPaymentContractorId());
                    Trip motorTrip = tripRepository.getMotorTrip(tripRequestAdvancePayment.getTripId())
                        .orElse(new Trip()); // TODO сомневаюсь что нужен пустой трип, лучше Exception или continue;

                    MessageDto messageDto = getMessageDto(tripRequestAdvancePayment, contact, paymentContractor,
                        applicationProperties.getLkUrl(),
                        motorTrip.getNum()
                    );
                    if (contact.getEmail() != null && motorTrip.getNum() != null) {
                        log.info("start sendEmail for tripNum: {}, for email: {}",
                            motorTrip.getNum(), contact.getEmail()
                        );
                        notificationService.sendEmail(messageDto);
                    }
                    if (contact.getPhone() != null && motorTrip.getNum() != null) {
                        log.info("start sendSmsDelay for tripNum: {}, for Phone: {}",
                            motorTrip.getNum(), contact.getPhone()
                        );
                        notificationService.sendSmsDelay(messageDto);
                    }
                }
            }
        );
    }

    @Scheduled(cron = "${cron.update: 0 0/30 * * * *}")
    void updateFileUuid() {
        List<TripRequestAdvancePayment> advanceRequests = advanceRequestRepository.findRequestsWithoutFiles();
        for (TripRequestAdvancePayment advanceRequest : advanceRequests) {
            Optional<Trip> trip = tripRepository.findById(advanceRequest.getTripId());
            if (!trip.isPresent()) {
                continue;
            }
            Map<String, String> fileUuidMap = restService.findTripRequestDocs(trip.get());
            if (!fileUuidMap.isEmpty()) {
                String fileContractRequestUuid = Optional
                    .ofNullable(fileUuidMap.get("request"))
                    .orElse(fileUuidMap.get("trip_request"));

                if (fileContractRequestUuid != null) {
                    advanceRequest.setIsDownloadedContractApplication(true);
                    advanceRequest.setUuidContractApplicationFile(fileContractRequestUuid);
                    log.info("UuidContractApplicationFile is: {}", advanceRequest.getUuidContractApplicationFile());
                }

                String fileAdvanceRequestUuid = fileUuidMap.get("assignment_advance_request");
                if (fileAdvanceRequestUuid != null) {
                    advanceRequest.setIsDownloadedContractApplication(true);
                    advanceRequest.setUuidAdvanceApplicationFile(fileAdvanceRequestUuid);
                    log.info("UuidAdvanceApplicationFile is: {}", advanceRequest.getUuidAdvanceApplicationFile());
                }

                if (advanceRequest.getUuidContractApplicationFile() != null &&
                    advanceRequest.getUuidAdvanceApplicationFile() != null) {
                    advanceRequest.setIs1CSendAllowed(true);
                    log.info("Is1CSendAllowed set true for advance: {}", advanceRequest);
                }
            }
        }
        advanceRequestRepository.saveAll(advanceRequests);
    }

    @Scheduled(cron = "${cron.update: 0 0/30 * * * *}")
    void updateAutoAdvance() {
        List<Contractor> contractors = contractorRepository.getFullName(
            applicationProperties.getMinCountTrip(),
            applicationProperties.getMinDateTrip()
        );
        contractors.forEach(c -> {
            c.setIsAutoAdvancePayment(true);
            log.info("Contractor with id: {} IsAutoAdvancePayment.", c.getId());
        });
        contractorRepository.saveAll(contractors);
    }
}
