package online.oboz.trip.trip_carrier_advance_payment_api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.*;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.AdvancePaymentCostRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.ContractorRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.TripRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.TripRequestAdvancePaymentRepository;
import online.oboz.trip.trip_carrier_advance_payment_api.service.dto.MessageDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static online.oboz.trip.trip_carrier_advance_payment_api.util.DtoUtils.getMessageDto;

@Slf4j
@Service
@EnableScheduling
public class AutoAdvancedService {

    private final AdvancePaymentCostRepository advancePaymentCostRepository;
    private final TripRequestAdvancePaymentRepository tripRequestAdvancePaymentRepository;
    private final TripRepository tripRepository;
    private final ContractorRepository contractorRepository;
    private final ApplicationProperties applicationProperties;
    private final AdvancePaymentContactService advancePaymentContactService;
    private final NotificationService notificationService;
    private final RestService restService;

    @Autowired
    public AutoAdvancedService(AdvancePaymentCostRepository advancePaymentCostRepository,
                               TripRequestAdvancePaymentRepository tripRequestAdvancePaymentRepository,
                               TripRepository tripRepository,
                               ContractorRepository contractorRepository,
                               RestTemplate restTemplate,
                               ApplicationProperties applicationProperties,
                               ObjectMapper objectMapper,
                               AdvancePaymentContactService advancePaymentContactService,
                               NotificationService notificationService, RestService restService) {
        this.advancePaymentCostRepository = advancePaymentCostRepository;
        this.tripRequestAdvancePaymentRepository = tripRequestAdvancePaymentRepository;
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
            AdvancePaymentCost advancePaymentCost = advancePaymentCostRepository.searchAdvancePaymentCost(trip.getCost());
            log.info("start createTripRequestAdvancePayment for tripId: {}, with cost: {}", id, trip.getCost());
            if (advancePaymentCost == null) {
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
            tripRequestAdvancePayment.setAdvancePaymentSum(advancePaymentCost.getAdvancePaymentSum());
            tripRequestAdvancePayment.setRegistrationFee(advancePaymentCost.getRegistrationFee());
            tripRequestAdvancePayment.setLoadingComplete(false);
            tripRequestAdvancePayment.setPageCarrierUrlIsAccess(true);
            tripRequestAdvancePayment.setIs1CSendAllowed(true);
            tripRequestAdvancePayment.setCancelAdvance(false);
            tripRequestAdvancePayment.setIsUnfSend(false);
            tripRequestAdvancePayment.setIsPaid(false);
            tripRequestAdvancePayment.setIsAdvancedPayment(false);
            tripRequestAdvancePayment.setAdvanceUuid(UUID.randomUUID());
            ContractorAdvancePaymentContact contact = advancePaymentContactService.getAdvancePaymentContact(trip.getContractorId());
            tripRequestAdvancePaymentRepository.save(tripRequestAdvancePayment);
            log.info("tripRequestAdvancePayment for id : {}, is auto crated", tripRequestAdvancePayment.getId());

            if (contact != null) {
                String paymentContractor = contractorRepository.getFullNameByPaymentContractorId(trip.getPaymentContractorId());
                Trip motorTrip = tripRepository.getMotorTrip(tripRequestAdvancePayment.getTripId()).orElse(new Trip());
                MessageDto messageDto = getMessageDto(tripRequestAdvancePayment, contact, paymentContractor,
                    applicationProperties.getLkUrl(),
                    motorTrip.getNum()
                );
                if (contact.getEmail() != null && motorTrip.getNum() != null) {
                    log.info("start sendEmail for tripNum: {}, for email: {}", motorTrip.getNum(), contact.getEmail());
                    notificationService.sendEmail(messageDto);
                    }
                    if (contact.getPhone() != null && motorTrip.getNum() != null) {
                        log.info("start sendSmsDelay for tripNum: {}, for Phone: {}", motorTrip.getNum(), contact.getPhone());
                        notificationService.sendSmsDelay(messageDto);
                    }
                }
            }
        );
    }

    @Scheduled(cron = "${cron.update: 0 0/30 * * * *}")
    void updateFileUuid() {
        List<TripRequestAdvancePayment> tripRequestAdvancePayments = tripRequestAdvancePaymentRepository.findRequestAdvancePaymentWithOutUuidFiles();
        tripRequestAdvancePayments.forEach(tripRequestAdvancePayment -> {
                final Long tripId = tripRequestAdvancePayment.getTripId();
                Trip trip = tripRepository.findById(tripId).get();
                Map<String, String> fileUuidMap = restService.findTripRequestDocs(trip);
                if (!fileUuidMap.isEmpty()) {
                    String fileContractRequestUuid = Optional.ofNullable(fileUuidMap.get("request")).orElse(fileUuidMap.get("trip_request"));
                    if (fileContractRequestUuid != null) {
                        tripRequestAdvancePayment.setIsDownloadedContractApplication(true);
                        tripRequestAdvancePayment.setUuidContractApplicationFile(fileContractRequestUuid);
                        log.info("UuidContractApplicationFile is: {}", tripRequestAdvancePayment.getUuidContractApplicationFile());

                    }
                    String fileAdvanceRequestUuid = fileUuidMap.get("assignment_advance_request");
                    if (fileAdvanceRequestUuid != null) {
                        tripRequestAdvancePayment.setIsDownloadedContractApplication(true);
                        tripRequestAdvancePayment.setUuidAdvanceApplicationFile(fileAdvanceRequestUuid);
                        log.info("UuidAdvanceApplicationFile is: {}", tripRequestAdvancePayment.getUuidAdvanceApplicationFile());

                    }
                    if (tripRequestAdvancePayment.getUuidContractApplicationFile() != null &&
                        tripRequestAdvancePayment.getUuidAdvanceApplicationFile() != null) {
                        tripRequestAdvancePayment.setIs1CSendAllowed(true);
                        log.info("Is1CSendAllowed set true for advance: {}", tripRequestAdvancePayment);
                    }
                }
            }
        );
        tripRequestAdvancePaymentRepository.saveAll(tripRequestAdvancePayments);
    }

    @Scheduled(cron = "${cron.update: 0 0/30 * * * *}")
    void updateAutoAdvance() {
        List<Contractor> contractors = contractorRepository.getFullNameByPaymentContractorId(applicationProperties.getMinCountTrip(),
            applicationProperties.getMinDateTrip());
        contractors.forEach(c -> {
            c.setIsAutoAdvancePayment(true);
            log.info("Contractor with id: {} IsAutoAdvancePayment.", c.getId());
        });
        contractorRepository.saveAll(contractors);
    }

    @Scheduled(cron = "${cron.update: 0 0/30 * * * *}")
    void cancelAdvance() {
        List<TripRequestAdvancePayment> tripRequestAdvancePayments = tripRequestAdvancePaymentRepository.findRequestAdvancePaymentNeedCancel();
        tripRequestAdvancePayments.forEach(p -> {
            p.setCancelAdvance(true);
            p.setCancelAdvanceComment("Auto Canceled");
            log.info("tripRequestAdvancePayments with id: {} auto canceled.", p.getId());

        });
        tripRequestAdvancePaymentRepository.saveAll(tripRequestAdvancePayments);
    }
}
