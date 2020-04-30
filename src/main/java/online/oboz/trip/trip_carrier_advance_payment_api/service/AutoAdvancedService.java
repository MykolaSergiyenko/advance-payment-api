package online.oboz.trip.trip_carrier_advance_payment_api.service;

import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.domain.*;
import online.oboz.trip.trip_carrier_advance_payment_api.repository.*;
import online.oboz.trip.trip_carrier_advance_payment_api.service.dto.MessageDto;
import online.oboz.trip.trip_carrier_advance_payment_api.service.integration.OrdersApiService;
import online.oboz.trip.trip_carrier_advance_payment_api.util.DtoUtils;
import org.apache.commons.lang.StringUtils;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@EnableScheduling
public class AutoAdvancedService {
    public static final String AUTO_ADVANCE_COMMENT = "Auto Created";

    private static final Logger log = LoggerFactory.getLogger(AutoAdvancedService.class);
    private final AdvancePaymentCostRepository advancePaymentCostRepository;
    private final AdvanceRequestRepository advanceRequestRepository;
    private final TripRepository tripRepository;
    private final ContractorRepository contractorRepository;
    private final AdvanceContactRepository advanceContactRepository;
    private final ApplicationProperties applicationProperties;
    private final NotificationService notificationService;
    private final OrdersApiService ordersApiService;
    private final ExecutorService service = Executors.newFixedThreadPool(15);

    @Autowired
    public AutoAdvancedService(
        AdvancePaymentCostRepository advancePaymentCostRepository,
        AdvanceRequestRepository advanceRequestRepository,
        TripRepository tripRepository,
        ContractorRepository contractorRepository,
        AdvanceContactRepository advanceContactRepository,
        ApplicationProperties applicationProperties,
        NotificationService notificationService,
        OrdersApiService ordersApiService
    ) {
        this.advancePaymentCostRepository = advancePaymentCostRepository;
        this.advanceRequestRepository = advanceRequestRepository;
        this.tripRepository = tripRepository;
        this.contractorRepository = contractorRepository;
        this.advanceContactRepository = advanceContactRepository;
        this.applicationProperties = applicationProperties;
        this.notificationService = notificationService;
        this.ordersApiService = ordersApiService;
    }

    @Scheduled(cron = "${cron.creation: 0 0/30 * * * *}")
    void createTripRequestAdvancePayment() {
        tripRepository.getAutoApprovedMotorTrips().forEach(trip -> {
                Double tripCostWithNds = tripRepository.getTripCostWithVat(trip.getId()); // нужно с ндс?
                AdvancePaymentCost advanceCost = advancePaymentCostRepository.getAdvancePaymentCost(tripCostWithNds);

                log.info("start createTripRequestAdvancePayment for tripId: {}, with cost: {}",
                    trip.getId(), trip.getCost()); // ндс?

                if (advanceCost == null) {
                    log.info("not found cost  for tripId: {} from advancePaymentCostRepository", trip.getId());
                    return;
                }

                TripRequestAdvancePayment tripRequestAdvancePayment = newTripRequestAdvancePayment(trip, advanceCost);
                log.info("tripRequestAdvancePayment for id : {}, is auto crated", tripRequestAdvancePayment.getId());

                ContractorAdvancePaymentContact contact = advanceContactRepository.find(trip.getContractorId()).orElse(null);

                if (contact != null) {
                    MessageDto messageDto = DtoUtils.newMessage(tripRequestAdvancePayment, contact, trip.getNum(),
                        contractorRepository, applicationProperties);
                    sendNotifications(messageDto);
                } else {
                    log.info("Contact not found for trip {}.", trip.getNum());
                }
            }
        );
    }


    private TripRequestAdvancePayment newTripRequestAdvancePayment(Trip trip, AdvancePaymentCost advanceCost) {
        TripRequestAdvancePayment tripRequestAdvancePayment = new TripRequestAdvancePayment();
        tripRequestAdvancePayment.setTripId(trip.getId());
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
        tripRequestAdvancePayment.setComment(AUTO_ADVANCE_COMMENT);
        tripRequestAdvancePayment.setIsUnfSend(false);
        tripRequestAdvancePayment.setIsDownloadedAdvanceApplication(false);
        tripRequestAdvancePayment.setIsDownloadedContractApplication(false);
        tripRequestAdvancePayment.setAdvanceUuid(UUID.randomUUID());
        tripRequestAdvancePayment.setIsSmsSent(false);
        tripRequestAdvancePayment.setIsEmailRead(false);
        advanceRequestRepository.save(tripRequestAdvancePayment);
        return tripRequestAdvancePayment;
    }

    private void sendNotifications(MessageDto messageDto) {
        if (StringUtils.isNotBlank(messageDto.getTripNum())) {
            submitEmail(messageDto);
            //submitSms(messageDto);
        } else {
            log.info("Trip-number is empty {}.", messageDto.getTripNum());
        }
    }

    private void submitEmail(MessageDto messageDto) {
        if (StringUtils.isNotBlank(messageDto.getEmail())) {
            log.info("start sendEmail for tripNum: {}, for email: {}", messageDto.getTripNum(), messageDto.getEmail());
            service.submit(() -> notificationService.sendEmail(messageDto));
        } else {
            log.info("E-mail in message is empty for trip-number {}.", messageDto.getTripNum());
        }
    }

    @Deprecated
    private void submitSms(MessageDto messageDto) {
        if (StringUtils.isNotBlank(messageDto.getPhone())) {
            log.info("start sendSms for tripNum: {}, for Phone: {}", messageDto.getTripNum(), messageDto.getPhone());
            //service.submit(() -> notificationService.sendSms(messageDto));
        } else {
            log.info("Phone-number in message is empty for trip-number {}.", messageDto.getTripNum());
        }
    }

    @Scheduled(cron = "${cron.update: 0 0/30 * * * *}")
    void updateFileUuid() {
        List<TripRequestAdvancePayment> advanceRequests = advanceRequestRepository.findRequestsWithoutFiles();
        for (TripRequestAdvancePayment advanceRequest : advanceRequests) {
            Optional<Trip> trip = tripRepository.findById(advanceRequest.getTripId());
            if (!trip.isPresent()) {
                continue;
            }
            Map<String, String> fileUuidMap = ordersApiService.findTripRequestDocs(trip.get());
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

    //    @Scheduled(cron = "${cron.update: 0 0/30 * * * *}")
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
